package ai;

import l2s.commons.text.PrintfFormat;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Location;
import npc.model.OrfenInstance;

public class Orfen extends Fighter
{
	public static final PrintfFormat[] MsgOnRecall = {
			new PrintfFormat("%s. Stop kidding yourself about your own powerlessness!"),
			new PrintfFormat("%s. I'll make you feel what true fear is!"),
			new PrintfFormat("You're really stupid to have challenged me. %s! Get ready!"),
			new PrintfFormat("%s. Do you think that's going to work?!") };

	public final Skill[] _paralyze;

	public Orfen(NpcInstance actor)
	{
		super(actor);
		_paralyze = getActor().getTemplate().getDebuffSkills();
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;
		OrfenInstance actor = getActor();

		if(actor.isTeleported() && actor.getCurrentHpPercents() > 95)
		{
			actor.setTeleported(false);
			return true;
		}

		return false;
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultNewTask();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);
		OrfenInstance actor = getActor();
		if(actor.isCastingNow())
			return;

		double distance = actor.getDistance(attacker);

		if(distance > 300 && distance < 1000 && Rnd.chance(5))
		{
			Functions.npcSay(actor, MsgOnRecall[Rnd.get(MsgOnRecall.length - 1)].sprintf(attacker.getName()));
			teleToLocation(attacker, Location.findFrontPosition(actor, attacker, 0, 50));
		}
		else if(_paralyze.length > 0 && Rnd.chance(5))
		{
			Skill r_skill = _paralyze[Rnd.get(_paralyze.length)];
			if(canUseSkill(r_skill, attacker, -1))
				addTaskAttack(attacker, r_skill, 1000000);
		}
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster, Creature target)
	{
		super.onEvtSeeSpell(skill, caster, target);
		OrfenInstance actor = getActor();
		if(actor.isCastingNow())
			return;

		double distance = actor.getDistance(caster);
		if(skill.getEffectPoint() > 0 && distance < 1000 && Rnd.chance(5))
		{
			Functions.npcSay(actor, MsgOnRecall[Rnd.get(MsgOnRecall.length)].sprintf(caster.getName()));
			teleToLocation(caster, Location.findFrontPosition(actor, caster, 0, 50));
		}
	}

	@Override
	public OrfenInstance getActor()
	{
		return (OrfenInstance) super.getActor();
	}

	private void teleToLocation(Creature attacker, Location loc)
	{
		attacker.teleToLocation(loc);
	}

	protected boolean maybeMoveToHome()
	{
		OrfenInstance actor = getActor();
		if(actor.isDead() || actor.isTeleported())
			return false;

		Location sloc = actor.getSpawnedLoc();
		if(!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE))
			return false;

		Location pos = Location.findPointToStay(actor, actor.getLoc(), 0, Config.MAX_DRIFT_RANGE);
		actor.setWalking();
		if(!actor.moveToLocation(pos, 0, true))
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0L));
			actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getGeoIndex()));
		}
		return true;
	}

	@Override
	public int getRateDEBUFF()
	{
		return 4;
	}

	@Override
	public int getRateDAM()
	{
		return 4;
	}
}