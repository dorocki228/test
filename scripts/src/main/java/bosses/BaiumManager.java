package bosses;

import bosses.EpicBossState.State;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.BossInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.TimeUtils;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class BaiumManager implements OnInitScriptListener
{
	private static final Logger _log = LoggerFactory.getLogger(BaiumManager.class);

	public static class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature victim, Creature killer)
		{
			if(victim.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(victim))
			{
				checkAnnihilated();
			}
			else if(victim.isNpc() && victim.getNpcId() == BAIUM)
			{
				onBaiumDie(victim);
			}
		}
	}

	// call Arcangels
	public static class CallArchAngel implements Runnable
	{
		@Override
		public void run()
		{
			for(SimpleSpawner spawn : _angelSpawns)
			{
				_angels.add(spawn.doSpawn(true));
			}
		}
	}

	public static class CheckLastAttack implements Runnable
	{
		@Override
		public void run()
		{
			if(_state.getState() == State.ALIVE)
			{
				if(_lastAttackTime + Config.BAIUM_LIMIT_UNTIL_SLEEP < System.currentTimeMillis())
				{
					sleepBaium();
				}
				else
				{
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
				}
			}
		}
	}

	// at end of interval.
	public static class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();

			// statue of Baium respawn.
			_statueSpawn.doSpawn(true);
		}
	}

	// kill pc
	public static class KillPc implements Runnable
	{
		private final BossInstance _boss;
		private final Player _target;

		public KillPc(Player target, BossInstance boss)
		{
			_target = target;
			_boss = boss;
		}

		@Override
		public void run()
		{
			SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(4136, 1);
			if(_target != null && skillEntry != null)
			{
				_boss.setTarget(_target);
				_boss.doCast(skillEntry, _target, false);
			}
		}
	}

	// Move at random on after Baium appears.
	public static class MoveAtRandom implements Runnable
	{
		private final NpcInstance _npc;
		private final Location _pos;

		public MoveAtRandom(NpcInstance npc, Location pos)
		{
			_npc = npc;
			_pos = pos;
		}

		@Override
		public void run()
		{
			if(_npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			{
				_npc.moveToLocation(_pos, 0, false);
			}
		}
	}

	public static class SetMobilised implements Runnable
	{
		private final BossInstance _boss;

		public SetMobilised(BossInstance boss)
		{
			_boss = boss;
		}

		@Override
		public void run()
		{
			_boss.stopImmobilized();
		}
	}

	// do social.
	public static class Social implements Runnable
	{
		private final int _action;
		private final NpcInstance _npc;

		public Social(NpcInstance npc, int actionId)
		{
			_npc = npc;
			_action = actionId;
		}

		@Override
		public void run()
		{
			SocialActionPacket sa = new SocialActionPacket(_npc.getObjectId(), _action);
			_npc.broadcastPacket(sa);
		}
	}

	// tasks.
	private static ScheduledFuture<?> _callAngelTask;
	private static ScheduledFuture<?> _intervalEndTask;
	private static ScheduledFuture<?> _killPcTask;
	private static ScheduledFuture<?> _mobiliseTask;
	private static ScheduledFuture<?> _moveAtRandomTask;
	private static ScheduledFuture<?> _sleepCheckTask;
	private static ScheduledFuture<?> _socialTask;
	private static ScheduledFuture<?> _socialTask2;
	private static ScheduledFuture<?> _onAnnihilatedTask;

	private static EpicBossState _state;
	private static long _lastAttackTime;

	private static NpcInstance _npcBaium;
	private static SimpleSpawner _statueSpawn;

	private static final List<NpcInstance> _monsters = new ArrayList<>();
	private static final Map<Integer, SimpleSpawner> _monsterSpawn = new ConcurrentHashMap<>();

	private static final List<NpcInstance> _angels = new ArrayList<>();
	private static final List<SimpleSpawner> _angelSpawns = new ArrayList<>();

	private static Zone _zone;

	private static final int ARCHANGEL = 29021;
	private static final int BAIUM = 29020;
	private static final int BAIUM_NPC = 29025;

	private static final OnDeathListener DEATH_LISTENER = new DeathListener();

	private static boolean Dying;

	// location of arcangels.
	private static final Location[] ANGEL_LOCATION = {
			new Location(113004, 16209, 10076, 60242),
			new Location(114053, 16642, 10076, 4411),
			new Location(114563, 17184, 10076, 49241),
			new Location(116356, 16402, 10076, 31109),
			new Location(115015, 16393, 10076, 32760),
			new Location(115481, 15335, 10076, 16241),
			new Location(114680, 15407, 10051, 32485),
			new Location(114886, 14437, 10076, 16868),
			new Location(115391, 17593, 10076, 55346),
			new Location(115245, 17558, 10076, 35536) };

//	private final static Location STATUE_LOCATION = new Location(115996, 17417, 10106, 41740);
private static final Location STATUE_LOCATION = new Location(-112959, -251147, -2992, 41740);

	private static void banishForeigners()
	{
		for(Player player : getPlayersInside())
		{
			player.teleToClosestTown();
		}
	}

	public static class onAnnihilated implements Runnable
	{
		@Override
		public void run()
		{
			sleepBaium();
		}
	}

	private static synchronized void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
		{
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.BAIUM_CLEAR_ZONE_IF_ALL_DIE);
		}
	}

	// Archangel ascension.
	private static void deleteArchangels()
	{
		for(NpcInstance angel : _angels)
		{
			if(angel != null && angel.getSpawn() != null)
			{
				angel.getSpawn().stopRespawn();
				angel.deleteMe();
			}
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

			// Statue of Baium
			_statueSpawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(BAIUM_NPC));
			_statueSpawn.setAmount(1);
			_statueSpawn.setLoc(STATUE_LOCATION);
			_statueSpawn.stopRespawn();

			// Baium
			SimpleSpawner tempSpawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(BAIUM));
			tempSpawn.setAmount(1);
			_monsterSpawn.put(BAIUM, tempSpawn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Archangels
		try
		{
			NpcTemplate angel = NpcHolder.getInstance().getTemplate(ARCHANGEL);
			_angelSpawns.clear();

			// 5 random numbers of 10, no duplicates
			List<Integer> random = new ArrayList<>();
			for(int i = 0; i < 5; i++)
			{
				int r = -1;
				while(r == -1 || random.contains(r))
				{
					r = Rnd.get(10);
				}
				random.add(r);
			}

			for(int i : random)
			{
				SimpleSpawner spawnDat = new SimpleSpawner(angel);
				spawnDat.setAmount(1);
				spawnDat.setLoc(ANGEL_LOCATION[i]);
				spawnDat.setRespawnDelay(300000);
				_angelSpawns.add(spawnDat);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		_log.info("BaiumManager: State of Baium is " + _state.getState() + ".");
		if(_state.getState() == State.NOTSPAWN)
		{
			_statueSpawn.doSpawn(true);
		}
		else if(_state.getState() == State.ALIVE)
		{
			System.out.println("hz");

			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			_statueSpawn.doSpawn(true);
		}
		else if(_state.getState() == State.INTERVAL || _state.getState() == State.DEAD)
		{
			setIntervalEndTask();
		}

		_log.info("BaiumManager: Next spawn date: " + TimeUtils.toSimpleFormat(_state.getRespawnDate()));
	}

	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
		{
			if(!pc.isDead())
				return false;
		}
		return true;
	}

	public static void onBaiumDie(Creature self)
	{
		if(Dying)
			return;

		Dying = true;
		self.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_D", 1, 0, self.getLoc()));
		_state.setNextRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		SimpleMessage message = new SimpleMessage("Baium died");
		LogService.getInstance().log(LoggerType.BOSSES, message);

		deleteArchangels();
	}

	// start interval.
	private static void setIntervalEndTask()
	{
		setUnspawn();

		//init state of Baium's lair.
		if(_state.getState() != State.INTERVAL)
		{
			_state.setNextRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
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
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(false);
			_socialTask = null;
		}
		if(_mobiliseTask != null)
		{
			_mobiliseTask.cancel(false);
			_mobiliseTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(false);
			_moveAtRandomTask = null;
		}
		if(_socialTask2 != null)
		{
			_socialTask2.cancel(false);
			_socialTask2 = null;
		}
		if(_killPcTask != null)
		{
			_killPcTask.cancel(false);
			_killPcTask = null;
		}
		if(_callAngelTask != null)
		{
			_callAngelTask.cancel(false);
			_callAngelTask = null;
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

		for(Player pl : GameObjectsStorage.getPlayers())
			pl.unsetVar("baiumPermission");
		CharacterVariablesDAO.getInstance().delete("baiumPermission");
	}

	// Baium sleeps if not attacked for 30 minutes.
	private static void sleepBaium()
	{
		setUnspawn();

		SimpleMessage message = new SimpleMessage("Baium going to sleep, spawning statue");
		LogService.getInstance().log(LoggerType.BOSSES, message);

		_state.setState(EpicBossState.State.NOTSPAWN);
		_state.update();

		// statue of Baium respawn.
		_statueSpawn.doSpawn(true);
	}

	public static class EarthquakeTask implements Runnable
	{
		private final BossInstance baium;

		public EarthquakeTask(BossInstance _baium)
		{
			baium = _baium;
		}

		@Override
		public void run()
		{
			EarthQuakePacket eq = new EarthQuakePacket(baium.getLoc(), 40, 5);
			baium.broadcastPacket(eq);
		}
	}

	// do spawn Baium.
	public static void spawnBaium(NpcInstance NpcBaium, Player awake_by)
	{
		Dying = false;
		_npcBaium = NpcBaium;

		// do spawn.
		SimpleSpawner baiumSpawn = _monsterSpawn.get(BAIUM);
		baiumSpawn.setLoc(_npcBaium.getLoc());

		// delete statue
		_npcBaium.getSpawn().stopRespawn();
		_npcBaium.deleteMe();

		BossInstance baium = (BossInstance) baiumSpawn.doSpawn(true);
		_monsters.add(baium);

		_state.setNextRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.ALIVE);
		_state.update();

		String messagePattern = "Spawned Baium, awake by: {}";
		ParameterizedMessage message = new ParameterizedMessage(messagePattern, awake_by);
		LogService.getInstance().log(LoggerType.BOSSES, message);

		// set last attack time.
		setLastAttackTime();

		baium.startImmobilized();
		baium.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_A", 1, 0, baium.getLoc()));
		baium.broadcastPacket(new SocialActionPacket(baium.getObjectId(), 2));

		_socialTask = ThreadPoolManager.getInstance().schedule(new Social(baium, 3), 15000);

		ThreadPoolManager.getInstance().schedule(new EarthquakeTask(baium), 25000);

		_socialTask2 = ThreadPoolManager.getInstance().schedule(new Social(baium, 1), 25000);
		_killPcTask = ThreadPoolManager.getInstance().schedule(new KillPc(awake_by, baium), 26000);
		_callAngelTask = ThreadPoolManager.getInstance().schedule(new CallArchAngel(), 35000);
		_mobiliseTask = ThreadPoolManager.getInstance().schedule(new SetMobilised(baium), 35500);

		// move at random.
		Location pos = new Location(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0);
		_moveAtRandomTask = ThreadPoolManager.getInstance().schedule(new MoveAtRandom(baium, pos), 36000);

		_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
	}

	private static long getRespawnInterval()
	{
		if(!Config.BAIUM_FIX_TIME_PATTERN.isEmpty())
		{
			long now = System.currentTimeMillis();
			try
			{
				SchedulingPattern timePattern = new SchedulingPattern(Config.BAIUM_FIX_TIME_PATTERN);
				long delay = timePattern.next(now) - now;
				return Math.max(60000, delay);
			}
			catch(IllegalArgumentException e)
			{
				throw new RuntimeException("Invalid respawn data \"" + Config.BAIUM_FIX_TIME_PATTERN + "\" in " + BaiumManager.class.getSimpleName(), e);
			}
		}
		return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.BAIUM_FIX_INTERVAL + Rnd.get(0, Config.BAIUM_RANDOM_INTERVAL)));
	}

	@Override
	public void onInit()
	{
		// init();
	}

	public static State getState()
	{
		return _state.getState();
	}
}