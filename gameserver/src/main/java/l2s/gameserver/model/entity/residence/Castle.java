package l2s.gameserver.model.entity.residence;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.math.SafeMath;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.component.fraction.FractionBoostBalancer;
import l2s.gameserver.dao.CastleDAO;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExCastleState;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.support.MerchantGuard;
import l2s.gameserver.templates.manor.CropProcure;
import l2s.gameserver.templates.manor.SeedProduction;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class Castle extends Residence {
    private static final Logger _log = LoggerFactory.getLogger(Castle.class);

    private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
    private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
    private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
    private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
    private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
    private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";

    private final IntObjectMap<MerchantGuard> _merchantGuards;
    private List<CropProcure> _procure;
    private List<SeedProduction> _production;
    private List<CropProcure> _procureNext;
    private List<SeedProduction> _productionNext;
    private boolean _isNextPeriodApproved;
    private long _treasury;
    private long _collectedShops;
    private long _collectedSeed;
    private final NpcString _npcStringName;
    private ResidenceSide _residenceSide;
    private static final Skill LIGHT_SIDE_SKILL = SkillHolder.getInstance().getSkill(19032, 1);
    private static final Skill DARK_SIDE_SKILL = SkillHolder.getInstance().getSkill(19033, 1);
    private final Set<ItemInstance> _spawnMerchantTickets;

    public Castle(StatsSet set) {
        super(set);
        _merchantGuards = new HashIntObjectMap();
        _residenceSide = ResidenceSide.NEUTRAL;
        _spawnMerchantTickets = new CopyOnWriteArraySet<>();
        _npcStringName = NpcString.valueOf(1001000 + getId());
    }

    @Override
    protected void initZone() {
        super.initZone();

        Arrays.stream(Config.GVE_CASTLE_REWARD_EFFECTS.split(";"))
                .map(data -> SkillHolder.getInstance().getSkillEntry(Integer.parseInt(data.split(",")[0]),
                        Integer.parseInt(data.split(",")[1]))).forEach(this::addZoneSkill);
    }

    @Override
    public ResidenceType getType() {
        return ResidenceType.CASTLE;
    }

    @Override
    public void changeOwner(Clan newOwner) {
        stopClanReputationIncreaseTask();

        if (newOwner != null && newOwner.getCastle() != 0) {
            Castle oldCastle = ResidenceHolder.getInstance().getResidence(Castle.class, newOwner.getCastle());
            if (oldCastle != null)
                oldCastle.changeOwner(null);
        }

        if (getOwnerId() > 0 && (newOwner == null || newOwner.getClanId() != getOwnerId())) {
            removeSkills();
            cancelCycleTask();
            Clan oldOwner = getOwner();
            if (oldOwner != null) {
                long amount = getTreasury();
                if (amount > 0L) {
                    Warehouse warehouse = oldOwner.getWarehouse();
                    if (warehouse != null) {
                        warehouse.addItem(57, amount);
                        addToTreasuryNoTax(-amount, false, false);

                        String messagePattern = "{}|{}|Castle:changeOwner";
                        ParameterizedMessage message = new ParameterizedMessage(messagePattern, this, -amount);
                        LogService.getInstance().log(LoggerType.TREASURY, message);
                    }
                }
                for (Player clanMember : oldOwner.getOnlineMembers(0))
                    if (clanMember != null && clanMember.getInventory() != null)
                        clanMember.getInventory().validateItems();
                oldOwner.setHasCastle(0);
                oldOwner.broadcastClanStatus(true, false, false);
            }
        }
        setOwner(newOwner);
        removeFunctions();
        if (newOwner != null) {
            newOwner.setHasCastle(getId());
            newOwner.broadcastClanStatus(true, false, false);
        }
        rewardSkills();

        startClanReputationIncreaseTask();

        if (getFraction() != Fraction.NONE)
            FractionBoostBalancer.getInstance().updateList(getFraction(), this);

        setJdbcState(JdbcEntityState.UPDATED);
        update();
    }

    @Override
    protected void loadData() {
        _treasury = 0L;
        _procure = new ArrayList<>();
        _production = new ArrayList<>();
        _procureNext = new ArrayList<>();
        _productionNext = new ArrayList<>();
        _isNextPeriodApproved = false;
        CastleDAO.getInstance().select(this);
        CastleHiredGuardDAO.getInstance().load(this);
        if (getFraction() != null && getFraction() != Fraction.NONE)
            FractionBoostBalancer.getInstance().updateList(getFraction(), this);
    }

    public void setTreasury(long t) {
        _treasury = t;
    }

    public long getCollectedShops() {
        return _collectedShops;
    }

    public long getCollectedSeed() {
        return _collectedSeed;
    }

    public void setCollectedShops(long value) {
        _collectedShops = value;
    }

    public void setCollectedSeed(long value) {
        _collectedSeed = value;
    }

    public void addToTreasury(long amount, boolean shop, boolean seed) {
//		amount = (long) Math.max(0.0, amount - amount * deleteAmount);
//		if(amount > 1L && getId() != 5 && getId() != 8)
//		{
//			final Castle royal = ResidenceHolder.getInstance().getResidence(Castle.class, 5);
//			if(royal != null)
//			{
//				double royalTaxRate = 0.25;
//				if(getId() == 3)r
//					royalTaxRate = 0.5;
//				final long royalTax = (long) (amount * royalTaxRate);
//				if(royal.getOwnerId() > 0)
//				{
//					royal.addToTreasury(royalTax, shop, seed);
//					if(getId() == 5)
//						LogService.add("Aden|" + royalTax + "|Castle:adenTax", "treasury");
//				}
//				amount -= royalTax;
//			}
//		}
//		addToTreasuryNoTax(amount, shop, seed);
    }

    public void addToTreasuryNoTax(long amount, boolean shop, boolean seed) {
        if (getFraction() == Fraction.NONE)
            return;
        if (amount == 0L)
            return;
        _treasury = SafeMath.addAndLimit(_treasury, amount);
        if (shop)
            _collectedShops += amount;
        if (seed)
            _collectedSeed += amount;
        setJdbcState(JdbcEntityState.UPDATED);
        update();
    }

    public int getCropRewardType(int crop) {
        int rw = 0;
        for (CropProcure cp : _procure)
            if (cp.getId() == crop)
                rw = cp.getReward();
        return rw;
    }

    public int getSellTaxPercent() {
        if (getResidenceSide() == ResidenceSide.LIGHT)
            return Config.LIGHT_CASTLE_SELL_TAX_PERCENT;
        if (getResidenceSide() == ResidenceSide.DARK)
            return Config.DARK_CASTLE_SELL_TAX_PERCENT;
        return 0;
    }

    public double getSellTaxRate(Player player) {
        if (getFraction().canAttack(player.getFraction()))
            return getSellTaxPercent() / 100.0;
        return 0.0;
    }

    public int getBuyTaxPercent() {
        if (getResidenceSide() == ResidenceSide.LIGHT)
            return Config.LIGHT_CASTLE_BUY_TAX_PERCENT;
        if (getResidenceSide() == ResidenceSide.DARK)
            return Config.DARK_CASTLE_BUY_TAX_PERCENT;
        return 0;
    }

    public double getBuyTaxRate(Player player) {
        if (getFraction().canAttack(player.getFraction()))
            return getBuyTaxPercent() / 100.0;
        return 0.0;
    }

    public long getTreasury() {
        return _treasury;
    }

    public List<SeedProduction> getSeedProduction(int period) {
        return period == 0 ? _production : _productionNext;
    }

    public List<CropProcure> getCropProcure(int period) {
        return period == 0 ? _procure : _procureNext;
    }

    public void setSeedProduction(List<SeedProduction> seed, int period) {
        if (period == 0)
            _production = seed;
        else
            _productionNext = seed;
    }

    public void setCropProcure(List<CropProcure> crop, int period) {
        if (period == 0)
            _procure = crop;
        else
            _procureNext = crop;
    }

    public synchronized SeedProduction getSeed(int seedId, int period) {
        for (SeedProduction seed : getSeedProduction(period))
            if (seed.getId() == seedId)
                return seed;
        return null;
    }

    public synchronized CropProcure getCrop(int cropId, int period) {
        for (CropProcure crop : getCropProcure(period))
            if (crop.getId() == cropId)
                return crop;
        return null;
    }

    public long getManorCost(int period) {
        List<CropProcure> procure;
        List<SeedProduction> production;
        if (period == 0) {
            procure = _procure;
            production = _production;
        } else {
            procure = _procureNext;
            production = _productionNext;
        }
        long total = 0L;
        if (production != null)
            for (SeedProduction seed : production)
                total += Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
        if (procure != null)
            for (CropProcure crop : procure)
                total += crop.getPrice() * crop.getStartAmount();
        return total;
    }

    public void saveSeedData() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            if (_production != null) {
                int count = 0;
                String[] values = new String[_production.size()];
                for (SeedProduction s : _production) {
                    values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    String query = "INSERT INTO castle_manor_production VALUES ";
                    query += values[0];
                    for (int i = 1; i < values.length; ++i)
                        query = query + "," + values[i];
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
            if (_productionNext != null) {
                int count = 0;
                String[] values = new String[_productionNext.size()];
                for (SeedProduction s : _productionNext) {
                    values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    String query = "INSERT INTO castle_manor_production VALUES ";
                    query += values[0];
                    for (int i = 1; i < values.length; ++i)
                        query = query + "," + values[i];
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            _log.error("Error adding seed production data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void saveSeedData(int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
            statement.setInt(1, getId());
            statement.setInt(2, period);
            statement.execute();
            DbUtils.close(statement);
            List<SeedProduction> prod = getSeedProduction(period);
            if (prod != null) {
                int count = 0;
                String[] values = new String[prod.size()];
                for (SeedProduction s : prod) {
                    values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
                    ++count;
                }
                if (values.length > 0) {
                    String query = "INSERT INTO castle_manor_production VALUES ";
                    query += values[0];
                    for (int i = 1; i < values.length; ++i)
                        query = query + "," + values[i];
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            _log.error("Error adding seed production data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void saveCropData() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            if (_procure != null) {
                int count = 0;
                String[] values = new String[_procure.size()];
                for (CropProcure cp : _procure) {
                    values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    String query = "REPLACE INTO castle_manor_procure VALUES ";
                    query += values[0];
                    for (int i = 1; i < values.length; ++i)
                        query = query + "," + values[i];
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
            if (_procureNext != null) {
                int count = 0;
                String[] values = new String[_procureNext.size()];
                for (CropProcure cp : _procureNext) {
                    values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    String query = "REPLACE INTO castle_manor_procure VALUES ";
                    query += values[0];
                    for (int i = 1; i < values.length; ++i)
                        query = query + "," + values[i];
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            _log.error("Error adding crop data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void saveCropData(int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
            statement.setInt(1, getId());
            statement.setInt(2, period);
            statement.execute();
            DbUtils.close(statement);
            List<CropProcure> proc = getCropProcure(period);
            if (proc != null) {
                int count = 0;
                String[] values = new String[proc.size()];
                for (CropProcure cp : proc) {
                    values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
                    ++count;
                }
                if (values.length > 0) {
                    String query = "INSERT INTO castle_manor_procure VALUES ";
                    query += values[0];
                    for (int i = 1; i < values.length; ++i)
                        query = query + "," + values[i];
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            _log.error("Error adding crop data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void updateCrop(int cropId, long amount, int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
            statement.setLong(1, amount);
            statement.setInt(2, cropId);
            statement.setInt(3, getId());
            statement.setInt(4, period);
            statement.execute();
        } catch (Exception e) {
            _log.error("Error adding crop data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void updateSeed(int seedId, long amount, int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
            statement.setLong(1, amount);
            statement.setInt(2, seedId);
            statement.setInt(3, getId());
            statement.setInt(4, period);
            statement.execute();
        } catch (Exception e) {
            _log.error("Error adding seed production data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public boolean isNextPeriodApproved() {
        return _isNextPeriodApproved;
    }

    public void setNextPeriodApproved(boolean val) {
        _isNextPeriodApproved = val;
    }

    @Override
    public void update() {
        CastleDAO.getInstance().update(this);
    }

    public NpcString getNpcStringName() {
        return _npcStringName;
    }

    public void addMerchantGuard(MerchantGuard merchantGuard) {
        _merchantGuards.put(merchantGuard.getItemId(), merchantGuard);
    }

    public MerchantGuard getMerchantGuard(int itemId) {
        return _merchantGuards.get(itemId);
    }

    public IntObjectMap<MerchantGuard> getMerchantGuards() {
        return _merchantGuards;
    }

    public Set<ItemInstance> getSpawnMerchantTickets() {
        return _spawnMerchantTickets;
    }

    @Override
    public void startCycleTask() {
    }

    @Override
    public void setResidenceSide(ResidenceSide side, boolean onRestore) {
        if (!onRestore && _residenceSide == side)
            return;
        _residenceSide = side;
        removeSkills();
        switch (_residenceSide) {
            case LIGHT: {
                removeSkill(DARK_SIDE_SKILL);
                addSkill(LIGHT_SIDE_SKILL);
                break;
            }
            case DARK: {
                removeSkill(LIGHT_SIDE_SKILL);
                addSkill(DARK_SIDE_SKILL);
                break;
            }
            default: {
                removeSkill(LIGHT_SIDE_SKILL);
                removeSkill(DARK_SIDE_SKILL);
                break;
            }
        }
        rewardSkills();
        if (!onRestore) {
            setJdbcState(JdbcEntityState.UPDATED);
            update();
        }
    }

    @Override
    public ResidenceSide getResidenceSide() {
        return _residenceSide;
    }

    @Override
    public void broadcastResidenceState() {
        Announcements.announceToAll(new ExCastleState(this));
    }

    @Override
    public boolean isCastle() {
        return true;
    }

    public Location getRestartPoint(Player player) {
        Fraction f = player.getFraction();
        Location loc = null;
        if (f == getFraction())
            loc = getOwnerRestartPoint();
        else {
            CastleSiegeEvent siege = getSiegeEvent();

            if (siege != null && siege.isInProgress()) {
                List<SpawnExObject> blockpost = siege.getObjects("blockpost");
                if (blockpost != null && !blockpost.isEmpty()) {
                    SpawnExObject bp = blockpost.get(0);

                    NpcInstance npc = bp.getFirstSpawned();
                    if (npc != null)
                        loc = Location.coordsRandomize(npc.getSpawnedLoc(), 100, 500);
                }
            }
        }
        return loc;
    }

    @Override
    protected void startClanReputationIncreaseTask() {
        if (_owner == null)
            return;

        long period = TimeUnit.MINUTES.toMillis(30);
        clanReputationIncreaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
        {
            _owner.incReputation(Config.GVE_CASTLE_OWNER_REPUTATION_INCREASE_AMOUNT, false, "castle_owner");
        }, period, period);
    }

    @Override
    public Fraction getFraction() {
        return getResidenceSide() == ResidenceSide.DARK ? Fraction.FIRE : getResidenceSide() == ResidenceSide.LIGHT ? Fraction.WATER : Fraction.NONE;
    }

    @Override
    public String toString() {
        return "Castle{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }
}
