package ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.templates.npc.WalkerRouteType;

/**
 * @author Bonux
**/
public abstract class AbstractAntharasAI extends DefaultAI<NpcInstance>
{
	private static final WalkerRoute WALKER_ROUTE_1 = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	private static final WalkerRoute WALKER_ROUTE_2 = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	static
	{
		WALKER_ROUTE_1.addPoint(new WalkerRoutePoint(new Location(181911, 114835, -7678), new NpcString[0], -1, 0, true, false));
		WALKER_ROUTE_2.addPoint(new WalkerRoutePoint(new Location(179112, 114888, -7712), new NpcString[0], -1, 0, true, false));
	}

	protected static final int MOVIE_MAX_DISTANCE = 2750;

	protected static final int PREPARE_MOVIE_TIMER_ID = 5000;
	protected static final int MOVIE_1_TIMER_ID = 5001;
	protected static final int MOVIE_2_TIMER_ID = 5002;
	protected static final int MOVIE_3_TIMER_ID = 5003;
	protected static final int MOVIE_4_TIMER_ID = 5004;
	protected static final int MOVIE_5_TIMER_ID = 5005;
	protected static final int MOVIE_6_TIMER_ID = 5006;

	// Debuff skills
	protected final SkillEntry s_fear = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4108, 1);
	protected final SkillEntry s_fear2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5092, 1);
	protected final SkillEntry s_curse = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4109, 1);
	protected final SkillEntry s_paralyze = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4111, 1);

	// Damage skills
	protected final SkillEntry s_shock = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4106, 1);
	protected final SkillEntry s_shock2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4107, 1);
	protected final SkillEntry s_antharas_ordinary_attack = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4112, 1);
	protected final SkillEntry s_antharas_ordinary_attack2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4113, 1);
	protected final SkillEntry s_meteor = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5093, 1);
	protected final SkillEntry s_breath = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4110, 1);

	// Regen skills
	protected final SkillEntry s_regen1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4239, 1);
	protected final SkillEntry s_regen2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4240, 1);
	protected final SkillEntry s_regen3 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4241, 1);

	protected final List<NpcInstance> _minions = new ArrayList<NpcInstance>();

	protected int _hpStage = 0;
	protected long _minionsSpawnTime = 0;
	protected int _damageCounter = 0;

	public AbstractAntharasAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public final boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void onEvtFinishWalkerRoute(int routeId)
	{
		super.onEvtFinishWalkerRoute(routeId);

		if(routeId == WALKER_ROUTE_1.getId())
		{
			addTimer(MOVIE_1_TIMER_ID, 2000);
		}
		else if(routeId == WALKER_ROUTE_2.getId())
		{
			NpcInstance actor = getActor();
			actor.setAggroRange(actor.getTemplate().aggroRange);
			_minionsSpawnTime = System.currentTimeMillis() + 120000L;
		}
	}

	@Override
	public void onEvtDeSpawn()
	{
		for(NpcInstance minion : _minions)
			minion.deleteMe();

		super.onEvtDeSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();

		if(_damageCounter == 0)
			actor.getAI().startAITask();

		for(Player player : getPlayersInsideLair())
		{
			notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 1);

			for(Servitor servitor : player.getServitors())
				notifyEvent(CtrlEvent.EVT_AGGRESSION, servitor, 1);
		}

		_damageCounter++;	

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(timerId == PREPARE_MOVIE_TIMER_ID)
		{
			actor.getAI().setWalkerRoute(WALKER_ROUTE_1);
		}
		else if(timerId == MOVIE_1_TIMER_ID)
		{
			actor.getFlags().getImmobilized().start(this);

			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 700, 13, -19, 0, 20000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_2_TIMER_ID, 3000);
		}
		else if(timerId == MOVIE_2_TIMER_ID)
		{
			// do social.
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 1));

			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 700, 13, 0, 6000, 20000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_3_TIMER_ID, 10000);
		}
		else if(timerId == MOVIE_3_TIMER_ID)
		{
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 2));
			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 3700, 0, -3, 0, 10000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_4_TIMER_ID, 200);
		}
		else if(timerId == MOVIE_4_TIMER_ID)
		{
			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 1100, 0, -3, 22000, 30000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_5_TIMER_ID, 10800);
		}
		else if(timerId == MOVIE_5_TIMER_ID)
		{
			// set camera.
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 1100, 0, -3, 300, 7000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_6_TIMER_ID, 7000);
		}
		else if(timerId == MOVIE_6_TIMER_ID)
		{
			// reset camera.
			for(Player player : getPlayersInsideLair())
			{
				player.leaveMovieMode();
				//player.sendPacket(new ExShowScreenMessage(NpcString.ANTHARAS_YOU_CANNOT_HOPE_TO_DEFEAT_ME, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
				player.sendPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_A", 1, actor.getObjectId(), actor.getLoc()));
			}
			actor.getFlags().getImmobilized().stop(this);
			actor.getFlags().getInvulnerable().stop(this);
			actor.getAI().setWalkerRoute(WALKER_ROUTE_2);
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target;
		if((target = prepareTarget()) == null)
			return false;

		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;

		double distance = actor.getDistance(target);

		// Buffs and stats
		double chp = actor.getCurrentHpPercents();
		if(_hpStage == 0)
		{
			actor.altOnMagicUse(actor, s_regen1);
			_hpStage = 1;
		}
		else if(chp < 75 && _hpStage == 1)
		{
			actor.altOnMagicUse(actor, s_regen2);
			_hpStage = 2;
		}
		else if(chp < 50 && _hpStage == 2)
		{
			actor.altOnMagicUse(actor, s_regen3);
			_hpStage = 3;
		}
		else if(chp < 30 && _hpStage == 3)
		{
			actor.altOnMagicUse(actor, s_regen3);
			_hpStage = 4;
		}

		// Minions spawn
		if(_minionsSpawnTime < System.currentTimeMillis() && getAliveMinionsCount() < 30)
		{
			double chance = (100. - actor.getCurrentHpPercents()) / 10. + 1.;
			if(Rnd.chance(chance))
			{
				NpcInstance minion = spawnMinion();
				if(minion != null)
					_minions.add(minion);  // Antharas Minions
			}
		}

		// Basic Attack
		if(Rnd.chance(50))
			return chooseTaskAndTargets(Rnd.chance(50) ? s_antharas_ordinary_attack : s_antharas_ordinary_attack2, target, distance);

		// Stage based skill attacks
		Map<SkillEntry, Integer> d_skill = new HashMap<SkillEntry, Integer>();
		switch(_hpStage)
		{
			case 1:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				break;
			case 2:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				break;
			case 3:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			case 4:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_fear);
				addDesiredSkill(d_skill, target, distance, s_shock);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			default:
				break;
		}

		SkillEntry r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.getTemplate().isBad())
			target = actor;

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	private int getAliveMinionsCount()
	{
		int count = 0;
		for(NpcInstance n : _minions)
		{
			if(!n.isDead())
				count++;
		}
		return count;
	}

	protected abstract Collection<Player> getPlayersInsideLair();

	protected abstract NpcInstance spawnMinion();
}