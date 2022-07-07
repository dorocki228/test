package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.StatsSet;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaidBossSpawnManager
{
	private static final Logger _log;
	private static RaidBossSpawnManager _instance;
	protected static final IntObjectMap<Spawner> _spawntable;
	protected static IntObjectMap<StatsSet> _storedInfo;

	private RaidBossSpawnManager()
	{
		_instance = this;
		if(!Config.DONTLOADSPAWN)
			reloadBosses();
	}

	public void reloadBosses()
	{
		loadStatus();
	}

	public void cleanUp()
	{
		updateAllStatusDb();
		_storedInfo.clear();
		_spawntable.clear();
	}

	public static RaidBossSpawnManager getInstance()
	{
		if(_instance == null)
			new RaidBossSpawnManager();
		return _instance;
	}

	private void loadStatus()
	{
		_storedInfo = new CHashIntObjectMap();
		Connection con = null;
        ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
			while(rset.next())
			{
				int id = rset.getInt("id");
				StatsSet info = new StatsSet();
				info.set("current_hp", rset.getDouble("current_hp"));
				info.set("current_mp", rset.getDouble("current_mp"));
				info.set("respawn_delay", rset.getInt("respawn_delay"));
				_storedInfo.put(id, info);
			}
		}
		catch(Exception e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt load raidboss statuses");
		}
		finally
		{
            Statement statement = null;
            DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("RaidBossSpawnManager: Loaded " + _storedInfo.size() + " Statuses");
	}

	private void updateAllStatusDb()
	{
		for(int id : _storedInfo.keySet().toArray())
			updateStatusDb(id, false);
	}

	private void updateStatusDb(int id, boolean isDead)
	{
		Spawner spawner = _spawntable.get(id);
		if(spawner == null)
			return;
		StatsSet info = _storedInfo.get(id);
		if(info == null)
			_storedInfo.put(id, info = new StatsSet());
		NpcInstance raidboss = spawner.getFirstSpawned();
		if(raidboss instanceof ReflectionBossInstance)
			return;
		if(raidboss != null && !isDead)
		{
			info.set("current_hp", raidboss.getCurrentHp());
			info.set("current_mp", raidboss.getCurrentMp());
			info.set("respawn_delay", 0);
		}
		else
		{
			info.set("current_hp", 0);
			info.set("current_mp", 0);
			info.set("respawn_delay", spawner.getRespawnTime());
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
			statement.setInt(1, id);
			statement.setDouble(2, info.getDouble("current_hp"));
			statement.setDouble(3, info.getDouble("current_mp"));
			statement.setInt(4, info.getInteger("respawn_delay", 0));
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt update raidboss_status table");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void addNewSpawn(int npcId, Spawner spawnDat)
	{
		if(_spawntable.containsKey(npcId))
		{
			_log.warn("DUBLICATE RAID BOSS SPAWN DATA:" + npcId);
			return;
		}
		_spawntable.put(npcId, spawnDat);
		StatsSet info = _storedInfo.get(npcId);
		if(info != null)
			spawnDat.setRespawnTime(info.getInteger("respawn_delay", 0));
	}

	public void deleteSpawn(int npcId)
	{
		_spawntable.remove(npcId);
	}

	public void onBossSpawned(RaidBossInstance raidboss)
	{
		int bossId = raidboss.getNpcId();
		if(!_spawntable.containsKey(bossId))
			return;
		StatsSet info = _storedInfo.get(bossId);
		if(info != null && info.getDouble("current_hp") > 1.0)
		{
			raidboss.setCurrentHp(info.getDouble("current_hp"), false);
			raidboss.setCurrentMp(info.getDouble("current_mp"));
		}
		GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + raidboss.getName());
		if(Config.ALT_ANNONCE_RAID_BOSSES_REVIVAL)
			Announcements.announceToAllFromStringHolder("l2s.gameserver.instancemanager.RaidBossSpawnManager." + (raidboss.isBoss() ? "onBossSpawned" : "onRaidBossSpawned"), raidboss.getName(), raidboss.getTitle());
	}

	public void onBossDespawned(RaidBossInstance raidboss, boolean isDead)
	{
		updateStatusDb(raidboss.getNpcId(), isDead);
	}

	public Status getRaidBossStatusId(int bossId)
	{
		Spawner spawner = _spawntable.get(bossId);
		if(spawner == null)
			return Status.UNDEFINED;
		NpcInstance npc = spawner.getFirstSpawned();
		return npc == null ? Status.DEAD : Status.ALIVE;
	}

	public boolean isDefined(int bossId)
	{
		return _spawntable.containsKey(bossId);
	}

	public IntObjectMap<Spawner> getSpawnTable()
	{
		return _spawntable;
	}

	static
	{
		_log = LoggerFactory.getLogger(RaidBossSpawnManager.class);
		_spawntable = new CHashIntObjectMap();
	}

	public enum Status
	{
		ALIVE,
		DEAD,
		UNDEFINED
    }
}
