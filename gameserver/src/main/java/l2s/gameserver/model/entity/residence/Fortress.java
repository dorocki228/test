package l2s.gameserver.model.entity.residence;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.FortressDAO;
import l2s.gameserver.dao.FortressUpgradeDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.service.MoraleBoostService;
import l2s.gameserver.templates.StatsSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Fortress extends Residence {
    private Fraction _fraction;
    private Map<UpgradeType, Integer> upgrades;

    public Fortress(StatsSet set) {
        super(set);
        upgrades = new HashMap<>();
    }

    @Override
    protected void initZone() {
        super.initZone();

        Arrays.stream(Config.GVE_FORTRESS_REWARD_EFFECTS.split(";"))
                .map(data -> SkillHolder.getInstance().getSkillEntry(Integer.parseInt(data.split(",")[0]),
                        Integer.parseInt(data.split(",")[1]))).forEach(this::addZoneSkill);
    }

    public int getUpgrade(UpgradeType type) {
        return upgrades.get(type);
    }

    public void updateUpgrades(UpgradeType type, int level) {
        upgrades.put(type, level);
    }

    @Override
    public void update() {
        FortressDAO.getInstance().update(this);
    }

    @Override
    public ResidenceType getType() {
        return ResidenceType.FORTRESS;
    }

    @Override
    protected void loadData() {
        FortressDAO.getInstance().select(this);
        FortressUpgradeDAO.getInstance().select(this);
    }

    @Override
    public void changeOwner(Clan clan) {
        stopClanReputationIncreaseTask();

        if (clan != null) {
            if (clan.getHasFortress() != 0) {
                Fortress oldFortress = ResidenceHolder.getInstance().getResidence(Fortress.class, clan.getHasFortress());
                if (oldFortress != null)
                    oldFortress.changeOwner(null);
            }
        }

        if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId())) {
            removeSkills();
            Clan oldOwner = getOwner();
            if (oldOwner != null)
                oldOwner.setHasFortress(0);
        }

        setOwner(clan);

        if (clan != null) {
            clan.setHasFortress(getId());
            clan.broadcastClanStatus(true, false, false);
            MoraleBoostService.getInstance().fortressSuccessAttack(this);
        }

        rewardSkills();

        startClanReputationIncreaseTask();

        setJdbcState(JdbcEntityState.UPDATED);
        update();
    }

    @Override
    public Fraction getFraction() {
        return _fraction;
    }

    public void setFraction(Fraction f) {
        _fraction = f;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FortressSiegeEvent getSiegeEvent() {
        return super.getSiegeEvent();
    }

    @Override
    protected void startClanReputationIncreaseTask() {
        if (_owner == null)
            return;

        long period = TimeUnit.MINUTES.toMillis(30);
        clanReputationIncreaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
        {
            _owner.incReputation(Config.GVE_FORTRESS_OWNER_REPUTATION_INCREASE_AMOUNT, false, "fortress_owner");
        }, period, period);
    }

    @Override
    public ResidenceSide getResidenceSide() {
        if (_fraction == Fraction.FIRE)
            return ResidenceSide.DARK;

        if (_fraction == Fraction.WATER)
            return ResidenceSide.LIGHT;

        return ResidenceSide.NEUTRAL;
    }
}
