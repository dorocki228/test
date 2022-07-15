package bosses;

import static com.google.common.flogger.LazyArgs.lazy;

import ai.AbstractBaiumAI;
import bosses.EpicBossState.State;
import com.google.common.flogger.FluentLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.BossInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.TimeUtils;

public class BaiumManager implements OnInitScriptListener
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	public static class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self))
				checkAnnihilated();
			else if(self.isNpc() && self.getNpcId() == BAIUM)
				onBaiumDie(self);
		}
	}

	public static class CheckLastAttack implements Runnable
	{
		@Override
		public void run()
		{
			if(_state.getState().equals(EpicBossState.State.ALIVE))
				if(_lastAttackTime + (BossesConfig.BAIUM_SLEEP_TIME * 60000) < System.currentTimeMillis())
					sleepBaium();
				else
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
		}
	}

	// do spawn teleport cube.
	public static class CubeSpawn implements Runnable
	{
		@Override
		public void run()
		{
			_teleportCube = NpcUtils.spawnSingle(TELEPORT_CUBE, CUBE_LOCATION);
		}
	}

	// at end of interval.
	public static class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.save();

			// statue of Baium respawn.
			_statueSpawn.init();
		}
	}

	// tasks.
	private static ScheduledFuture<?> _cubeSpawnTask = null;
	private static ScheduledFuture<?> _intervalEndTask = null;
	private static ScheduledFuture<?> _sleepCheckTask = null;
	private static ScheduledFuture<?> _onAnnihilatedTask = null;

	private static EpicBossState _state;
	private static long _lastAttackTime = 0;

	private static SimpleSpawner _statueSpawn = null;

	private static NpcInstance _teleportCube = null;

	private static List<NpcInstance> _monsters = new ArrayList<NpcInstance>();

	private static List<NpcInstance> _angels = new ArrayList<NpcInstance>();

	private static Zone _zone;

	private final static int BAIUM = 29020;
	private final static int BAIUM_NPC = 29025;

	private static final OnDeathListener DEATH_LISTENER = new DeathListener();

	private static final AtomicBoolean DYING = new AtomicBoolean();

	// location of teleport cube.
	private final static Location CUBE_LOCATION = new Location(115203, 16620, 10078, 0);
	private final static Location STATUE_LOCATION = new Location(115996, 17417, 10106, 41740);
	private final static int TELEPORT_CUBE = 31842;

	public static EpicBossState.State getState()
	{
		return _state.getState();
	}

	private static void banishForeigners()
	{
		for(Player player : getPlayersInside())
			player.teleToClosestTown();
	}

	public static class onAnnihilated implements Runnable
	{
		@Override
		public void run()
		{
			sleepBaium();
		}
	}

	private synchronized static void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), 5000);
	}
	// Archangel ascension.
	private static void deleteArchangels()
	{
		for(NpcInstance angel : _angels)
			if(angel != null && angel.getSpawn() != null)
			{
				angel.getSpawn().stopRespawn();
				angel.deleteMe();
			}
		_angels.clear();
	}

	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	public static Zone getZone()
	{
		return _zone;
	}

	private void init()
	{
		_state = new EpicBossState(BAIUM);
		_zone = ReflectionUtils.getZone("[baium_epic]");

		CharListenerList.addGlobal(DEATH_LISTENER);
		try
		{
			SimpleSpawner tempSpawn;

			// Statue of Baium
			_statueSpawn = new SimpleSpawner(BAIUM_NPC);
			_statueSpawn.setAmount(1);
			_statueSpawn.setSpawnRange(STATUE_LOCATION);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		_log.atInfo().log( "BaiumManager: State of Baium is %s.", lazy(() -> _state.getState()) );

		if(_state.getState().equals(EpicBossState.State.NOTSPAWN))
			_statueSpawn.init();
		else if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.save();
			_statueSpawn.init();
		}
		else if(_state.getState().equals(EpicBossState.State.INTERVAL) || _state.getState().equals(EpicBossState.State.DEAD))
			setIntervalEndTask();

		_log.atInfo().log( "BaiumManager: Next spawn date: %s", lazy(() -> TimeUtils.toSimpleFormat(_state.getRespawnDate())) );
	}

	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	public static void onBaiumDie(Creature self)
	{
		if(!DYING.compareAndSet(false, true))
			return;

		_state.setNextRespawnDate(getRespawnTime());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.save();

		Log.add("Baium died", "bosses");

		deleteArchangels();

		_cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new CubeSpawn(), 10000);
	}

	private static long getRespawnTime()
	{
		return BossesConfig.BAIUM_RESPAWN_TIME_PATTERN.next(System.currentTimeMillis());
	}

	// start interval.
	private static void setIntervalEndTask()
	{
		setUnspawn();

		//init state of Baium's lair.
		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setNextRespawnDate(getRespawnTime());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.save();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
	}

	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}

	// clean Baium's lair.
	public static void setUnspawn()
	{
		// eliminate players.
		banishForeigners();

		// delete monsters.
		deleteArchangels();

		for(NpcInstance mob : _monsters)
		{
			Spawner spawn = mob.getSpawn();
			if(spawn != null)
				spawn.stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		// delete teleport cube.
		if(_teleportCube != null)
		{
			_teleportCube.deleteMe();
			_teleportCube = null;
		}

		// not executed tasks is canceled.
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(false);
			_cubeSpawnTask = null;
		}

		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_sleepCheckTask != null)
		{
			_sleepCheckTask.cancel(false);
			_sleepCheckTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
	}

	// Baium sleeps if not attacked for 30 minutes.
	private static void sleepBaium()
	{
		setUnspawn();
		Log.add("Baium going to sleep, spawning statue", "bosses");
		_state.setState(EpicBossState.State.NOTSPAWN);
		_state.save();

		// statue of Baium respawn.
		_statueSpawn.init();
	}

	// do spawn Baium.
	public static void spawnBaium(NpcInstance baiumStatue, Player awake_by)
	{
		if(getState() != State.NOTSPAWN)
			return;

		DYING.set(false);

		Location spawnLoc = baiumStatue.getLoc();

		baiumStatue.getSpawn().deleteAll();

		final BossInstance baium = (BossInstance) NpcUtils.spawnSingle(BAIUM, spawnLoc);

		NpcAI ai = baium.getAI();
		if(ai instanceof AbstractBaiumAI){
			((AbstractBaiumAI) ai).setAwakener(awake_by);
		}

		_monsters.add(baium);

		_state.setNextRespawnDate(getRespawnTime());
		_state.setState(EpicBossState.State.ALIVE);
		_state.save();

		Log.add("Spawned Baium, awake by: " + awake_by, "bosses");

		_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
	}

	public static boolean checkRequiredItems(Player player)
	{
		for(int[] item : BossesConfig.BAIUM_ENTERANCE_NECESSARY_ITEMS)
		{
			int itemId = item.length > 0 ? item[0] : 0;
			int itemCount = item.length > 1 ? item[1] : 0;
			if(itemId > 0 && itemCount > 0 && !ItemFunctions.haveItem(player, itemId, itemCount))
				return false;
		}
		return true;
	}

	public static boolean consumeRequiredItems(Player player)
	{
		if(BossesConfig.BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS)
		{
			for(int[] item : BossesConfig.BAIUM_ENTERANCE_NECESSARY_ITEMS)
			{
				int itemId = item.length > 0 ? item[0] : 0;
				int itemCount = item.length > 1 ? item[1] : 0;
				if(itemId > 0 && itemCount > 0 && !ItemFunctions.deleteItem(player, itemId, itemCount, true))
					return false;
			}
		}
		return true;
	}

	@Override
	public void onInit()
	{
		init();
	}
}