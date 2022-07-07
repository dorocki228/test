package l2s.gameserver.model.entity.residence;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceFunctionsHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.ResidenceFunctionType;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacket.Color;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.residence.ResidenceFunctionTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.TeleportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Residence implements JdbcEntity
{
	protected static final Logger _log = LoggerFactory.getLogger(Residence.class);

    private static final L2GameServerPacket REMOVE_PACKET = AAScreenStringPacketPresets.SIEGE_ZONE.remove();
    private static final L2GameServerPacket ENEMY_PACKET = AAScreenStringPacketPresets.SIEGE_ZONE
            .addOrUpdate("ENEMY ZONE (No Bonus Adena +0%)", new Color(255, 0, 0));
    private static final L2GameServerPacket FRIENDLY_PACKET = AAScreenStringPacketPresets.SIEGE_ZONE
            .addOrUpdate("FRIENDLY ZONE (Bonus Adena +30%)", new Color(0, 255, 0));

	public static final long CYCLE_TIME = 3600000L;

	private final int _id;
	private final String _name;
	protected Clan _owner;
	protected Zone _zone;
    private final ZoneEnterLeaveListener _zoneListener = new ZoneEnterLeaveListener();

	private final TIntObjectMap<Skill> _skills = new TIntObjectHashMap<>();
	private final List<SkillEntry> zoneSkills = new ArrayList<>();

    protected SiegeEvent<?, ?> _siegeEvent;
	protected Calendar _siegeDate = Calendar.getInstance();
	protected Calendar _lastSiegeDate = Calendar.getInstance();
	protected Calendar _ownDate = Calendar.getInstance();
	protected ScheduledFuture<?> _cycleTask;
	private int _cycle;
	private int _paidCycle;
	protected JdbcEntityState _jdbcEntityState = JdbcEntityState.CREATED;
	protected List<Location> _banishPoints = new ArrayList<>();
	protected List<Location> _ownerRestartPoints = new ArrayList<>();
	protected List<Location> _otherRestartPoints = new ArrayList<>();
	protected List<Location> _chaosRestartPoints = new ArrayList<>();
	private final Map<ResidenceFunctionType, ResidenceFunction> _activeFunctions = new HashMap<>();
	private final TIntSet _availableFunctions = new TIntHashSet();
	protected ScheduledFuture<?> clanReputationIncreaseTask;

	public Residence(StatsSet set)
	{
		_id = set.getInteger("id");
		_name = set.getString("name");
		initZone();
	}

	public abstract ResidenceType getType();

	public void init()
	{
		initEvent();
		loadData();
		loadFunctions();
		rewardSkills();
		startCycleTask();
		startClanReputationIncreaseTask();
	}

	protected void initZone()
	{
		_zone = ReflectionUtils.getZone("residence_" + getId());
		_zone.setParam("residence", this);
        _zone.addListener(_zoneListener);
	}

	protected void initEvent()
	{
		_siegeEvent = EventHolder.getInstance().getEvent(EventType.SIEGE_EVENT, getId());
	}

	@SuppressWarnings("unchecked")
	public <E extends SiegeEvent<?, ?>> E getSiegeEvent()
	{
		return (E) _siegeEvent;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public int getOwnerId()
	{
		return _owner == null ? 0 : _owner.getClanId();
	}

	public void setOwner(Clan owner)
	{
		_owner = owner;
	}

	public Clan getOwner()
	{
		return _owner;
	}

	public String getOwnerName()
	{
		if(getOwner() != null)
			return getOwner().getName();
		else
			return getFraction() == Fraction.FIRE ? "Fire" : getFraction() == Fraction.WATER ? "Water" : "None";
	}

	public boolean isOwner(int clanId)
	{
		return _owner != null && _owner.getClanId() == clanId;
	}

	public Zone getZone()
	{
		return _zone;
	}

	public boolean isInstant()
	{
		return false;
	}

	public boolean isCastle()
	{
		return false;
	}

	protected abstract void loadData();

	public abstract void changeOwner(Clan p0);

	public Calendar getOwnDate()
	{
		return _ownDate;
	}

	public Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public Calendar getLastSiegeDate()
	{
		return _lastSiegeDate;
	}

	public void addAvailableFunction(int id)
	{
		_availableFunctions.add(id);
	}

	public void addSkill(Skill skill)
	{
		_skills.put(skill.getId(), skill);
	}

	public void removeSkill(Skill skill)
	{
		_skills.remove(skill.getId());
	}

	public void addZoneSkill(SkillEntry skill)
	{
		zoneSkills.add(skill);
	}

	public void removeZoneSkill(SkillEntry skill)
	{
        zoneSkills.remove(skill.getId());
	}

	public boolean checkIfInZone(Location loc, Reflection ref)
	{
		return checkIfInZone(loc.x, loc.y, loc.z, ref);
	}

	public boolean checkIfInZone(int x, int y, int z, Reflection ref)
	{
		return getZone() != null && getZone().checkIfInZone(x, y, z, ref);
	}

	public void banishForeigner(int clanId)
	{
		for(Player player : _zone.getInsidePlayers())
		{
			if(player.getClanId() == getOwnerId())
				continue;
			player.teleToLocation(getBanishPoint());
		}
	}

	public void rewardSkills()
	{
		Clan owner = getOwner();
		if(owner != null)
			for(Skill skill : getSkills())
				if(owner.addSkill(skill.getEntry(), false) == null)
					owner.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));
	}

	public void removeSkills()
	{
		Clan owner = getOwner();
		if(owner != null)
			for(Skill skill : getSkills())
				owner.removeSkill(skill.getId());
	}

    public void manageZoneBonuses(Player actor, boolean add)
    {
        if(getFraction().canAttack(actor.getFraction()))
        {
            return;
        }

        if(add)
        {
            zoneSkills.forEach(skillEntry -> skillEntry.getEffects(actor, actor));
        }
        else
        {
            zoneSkills.forEach(skillEntry -> actor.getAbnormalList().stopEffects(skillEntry));
        }
    }

    public void manageZoneStatus(Player actor, boolean add)
    {
        if(add)
        {
            if(getFraction().canAttack(actor.getFraction()))
                actor.sendPacket(ENEMY_PACKET);
            else
                actor.sendPacket(FRIENDLY_PACKET);
        }
        else
            actor.sendPacket(REMOVE_PACKET);
    }

	protected void loadFunctions()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM residence_functions WHERE residence_id=?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				ResidenceFunctionType type = ResidenceFunctionType.VALUES[rs.getInt("type")];
				ResidenceFunctionTemplate functionTemplate = ResidenceFunctionsHolder.getInstance().getTemplate(type, rs.getInt("level"));
				if(functionTemplate == null || !isFunctionAvailable(functionTemplate))
					removeFunction(type);
				else
				{
					ResidenceFunction function = new ResidenceFunction(functionTemplate, getId());
					function.setEndTimeInMillis(rs.getInt("end_time") * 1000L);
					function.setInDebt(rs.getBoolean("in_debt"));
					addActiveFunction(function);
					startAutoTaskForFunction(function);
				}
			}
		}
		catch(Exception e)
		{
			_log.warn("Residence: loadFunctions(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public void addActiveFunction(ResidenceFunction function)
	{
		_activeFunctions.put(function.getType(), function);
	}

	public boolean isFunctionActive(ResidenceFunctionType type)
	{
		return _activeFunctions.containsKey(type);
	}

	public ResidenceFunction getActiveFunction(ResidenceFunctionType type)
	{
		return _activeFunctions.get(type);
	}

	public boolean updateFunctions(ResidenceFunctionType type, int level)
	{
		Clan clan = getOwner();
		if(clan == null)
			return false;
		ResidenceFunction activeFunction = getActiveFunction(type);
		if(activeFunction != null && activeFunction.getLevel() == level)
			return true;
		if(level == 0)
		{
			if(activeFunction != null)
			{
				removeFunction(type);
				return true;
			}
			return false;
		}
		else
		{
			ResidenceFunctionTemplate functionTemplate = ResidenceFunctionsHolder.getInstance().getTemplate(type, level);
			if(!isFunctionAvailable(functionTemplate))
				return false;
			long clanAdenaCount = clan.getAdenaCount();
			long lease = functionTemplate.getCost();
			if(activeFunction == null)
			{
				if(clanAdenaCount < lease)
					return false;
				clan.getWarehouse().destroyItemByItemId(57, lease);
			}
			else
			{
				long activeFunctionLease = activeFunction.getTemplate().getCost() / activeFunction.getTemplate().getPeriod() * functionTemplate.getPeriod();
				if(clanAdenaCount < lease - activeFunctionLease)
					return false;
				if(lease > activeFunctionLease)
					clan.getWarehouse().destroyItemByItemId(57, lease - activeFunctionLease);
			}
			long time = Calendar.getInstance().getTimeInMillis() + functionTemplate.getPeriod() * 24 * 60 * 60 * 1000L;
			ResidenceFunction function = new ResidenceFunction(functionTemplate, getId());
			function.setEndTimeInMillis(time);
			_activeFunctions.put(function.getType(), function);
			startAutoTaskForFunction(function);
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("REPLACE residence_functions SET residence_id=?, type=?, level=?, end_time=?");
				statement.setInt(1, getId());
				statement.setInt(2, type.ordinal());
				statement.setInt(3, level);
				statement.setInt(4, (int) (time / 1000L));
				statement.execute();
			}
			catch(Exception e)
			{
				_log.warn("Exception: updateFunctions(ResidenceFunctionType,int): " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			return true;
		}
	}

	public void removeFunction(ResidenceFunctionType type)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM residence_functions WHERE residence_id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, type.ordinal());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Exception: removeFunction(int type): " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_activeFunctions.remove(type);
	}

	public void removeFunctions()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM residence_functions WHERE residence_id=?");
			statement.setInt(1, getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("Exception: removeFunctions(): " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_activeFunctions.clear();
	}

	private void startAutoTaskForFunction(ResidenceFunction function)
	{
		Clan clan = getOwner();
		if(clan == null)
			return;
		if(function.getEndTimeInMillis() > System.currentTimeMillis())
			ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
		else if(function.isInDebt() && clan.getAdenaCount() >= function.getTemplate().getCost())
		{
			clan.getWarehouse().destroyItemByItemId(57, function.getTemplate().getCost());
			function.updateRentTime(false);
			ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
		}
		else if(!function.isInDebt())
		{
			function.setInDebt(true);
			function.updateRentTime(true);
			ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
		}
		else
			removeFunction(function.getType());
	}

	@Override
	public void setJdbcState(JdbcEntityState state)
	{
		_jdbcEntityState = state;
	}

	@Override
	public JdbcEntityState getJdbcState()
	{
		return _jdbcEntityState;
	}

	@Override
	public void save()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete()
	{
		throw new UnsupportedOperationException();
	}

	public void cancelCycleTask()
	{
		_cycle = 0;
		_paidCycle = 0;
		if(_cycleTask != null)
		{
			_cycleTask.cancel(false);
			_cycleTask = null;
		}
		setJdbcState(JdbcEntityState.UPDATED);
	}

	public void startCycleTask()
	{
		if(_owner == null)
			return;
		long ownedTime = getOwnDate().getTimeInMillis();
		if(ownedTime == 0L)
			return;
		long diff;
		for(diff = System.currentTimeMillis() - ownedTime; diff >= CYCLE_TIME; diff -= CYCLE_TIME)
		{}
		_cycleTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ResidenceCycleTask(), diff, CYCLE_TIME);
	}

	public void chanceCycle()
	{
		setCycle(getCycle() + 1);
		setJdbcState(JdbcEntityState.UPDATED);
	}

	public List<Skill> getSkills()
	{
		return new ArrayList<>(_skills.valueCollection());
	}

	public void addBanishPoint(Location loc)
	{
		_banishPoints.add(loc);
	}

	public void addOwnerRestartPoint(Location loc)
	{
		_ownerRestartPoints.add(loc);
	}

	public void addOtherRestartPoint(Location loc)
	{
		_otherRestartPoints.add(loc);
	}

	public void addChaosRestartPoint(Location loc)
	{
		_chaosRestartPoints.add(loc);
	}

	public Location getBanishPoint()
	{
		if(_banishPoints.isEmpty())
			return null;
		return _banishPoints.get(Rnd.get(_banishPoints.size()));
	}

	public Location getOwnerRestartPoint()
	{
		if(_ownerRestartPoints.isEmpty())
			return null;
		return _ownerRestartPoints.get(Rnd.get(_ownerRestartPoints.size()));
	}

	public Location getOtherRestartPoint()
	{
		if(_otherRestartPoints.isEmpty())
			return null;
		return _otherRestartPoints.get(Rnd.get(_otherRestartPoints.size()));
	}

	public Location getChaosRestartPoint()
	{
		if(_chaosRestartPoints.isEmpty())
			return null;
		return _chaosRestartPoints.get(Rnd.get(_chaosRestartPoints.size()));
	}

	public Location getNotOwnerRestartPoint(Player player)
	{
		// TODO: сделать нормально
		return TeleportUtils.getRestartPoint(player, RestartType.TO_VILLAGE).getLoc();
		//return player.isPK() ? getChaosRestartPoint() : getOtherRestartPoint();
	}

	public int getCycle()
	{
		return _cycle;
	}

	public long getCycleDelay()
	{
		if(_cycleTask == null)
			return 0L;
		return _cycleTask.getDelay(TimeUnit.SECONDS);
	}

	public void setCycle(int cycle)
	{
		_cycle = cycle;
	}

	public int getPaidCycle()
	{
		return _paidCycle;
	}

	public void setPaidCycle(int paidCycle)
	{
		_paidCycle = paidCycle;
	}

	public void setResidenceSide(ResidenceSide side, boolean onRestore)
	{}

	public ResidenceSide getResidenceSide()
	{
		return ResidenceSide.NEUTRAL;
	}

	public void broadcastResidenceState()
	{}

	public int getVisibleFunctionLevel(int level)
	{
		return level < 11 ? level : level - 10;
	}

	public boolean isFunctionAvailable(ResidenceFunctionTemplate template)
	{
		return getVisibleFunctionLevel(template.getLevel()) > 0 && _availableFunctions.contains(template.getId());
	}

	public List<ResidenceFunctionTemplate> getAvailableFunctions(ResidenceFunctionType type)
	{
		List<ResidenceFunctionTemplate> functions = new ArrayList<>();
		for(ResidenceFunctionTemplate template : ResidenceFunctionsHolder.getInstance().getTemplates(type))
			if(isFunctionAvailable(template))
				functions.add(template);
		return functions;
	}

	protected void startClanReputationIncreaseTask()
	{
	}

	protected void stopClanReputationIncreaseTask()
	{
		if(clanReputationIncreaseTask == null || clanReputationIncreaseTask.isCancelled())
			return;

		clanReputationIncreaseTask.cancel(true);
	}

	public Reflection getReflection(int clanId)
	{
		return ReflectionManager.MAIN;
	}

	public Fraction getFraction()
	{
		return Fraction.NONE;
	}

	@Override
	public String toString()
	{
		return "Residence{" +
				"id=" + _id +
				", name='" + _name + '\'' +
				'}';
	}

	public class ResidenceCycleTask implements Runnable
	{
		@Override
		public void run()
		{
			chanceCycle();
			update();
		}
	}

	private class AutoTaskForFunctions implements Runnable
	{
		ResidenceFunction _function;

		public AutoTaskForFunctions(ResidenceFunction function)
		{
			_function = function;
		}

		@Override
		public void run()
		{
			startAutoTaskForFunction(_function);
		}
	}

    private class ZoneEnterLeaveListener implements OnZoneEnterLeaveListener
    {
        @Override
        public void onZoneEnter(Zone zone, Creature creature)
        {
            if(!creature.isPlayer())
            {
                return;
            }

            Player player = creature.getPlayer();

            manageZoneBonuses(player, true);
            manageZoneStatus(player, true);
        }

        @Override
        public void onZoneLeave(Zone zone, Creature creature)
        {
            if(!creature.isPlayer())
            {
                return;
            }

            Player player = creature.getPlayer();

            manageZoneBonuses(player, false);
            manageZoneStatus(player, false);
        }
    }
}
