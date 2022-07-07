package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.templates.StatsSet;
import org.napile.pair.primitive.IntObjectPair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Resurrect extends Skill
{
	public static List<Event> GLOBAL = new ArrayList<>();
	private final boolean _canPet;

	public Resurrect(StatsSet set)
	{
		super(set);
		_canPet = set.getBool("canPet", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(!activeChar.isPlayer())
			return false;
		if(target == null || target != activeChar && !target.isDead())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		Player player = (Player) activeChar;
		Player pcTarget = target.getPlayer();
		if(pcTarget == null)
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		if(player.isInOlympiadMode() || pcTarget.isInOlympiadMode())
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		if(oneTarget())
		{
			if(target.getAbnormalList().containsEffects(EffectType.ResurrectBlock))
			{
				player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
			List<Event> events = new ArrayList<>(GLOBAL.size() + 2);
			events.addAll(GLOBAL);
			events.addAll(target.getZoneEvents());
			for(Event e : events)
				if(!e.canResurrect(activeChar, target, forceUse, false))
				{
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
			if(target.isPet())
			{
				IntObjectPair<OnAnswerListener> ask = pcTarget.getAskListener(false);
				ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
						activeChar.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
					else
						activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING);
					return false;
				}
				if(!_canPet && getTargetType() != SkillTargetType.TARGET_ONE_SERVITOR)
				{
					player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					return false;
				}
			}
			else if(target.isPlayer())
			{
				IntObjectPair<OnAnswerListener> ask = pcTarget.getAskListener(false);
				ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
						activeChar.sendPacket(SystemMsg.WHILE_A_PET_IS_BEING_RESURRECTED_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
					else
						activeChar.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
					return false;
				}
				if(getTargetType() == SkillTargetType.TARGET_ONE_SERVITOR)
				{
					player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!activeChar.isPlayer())
			return;
		Player targetPlayer = target.getPlayer();
		if(targetPlayer == null)
			return;
		double percent = getPower();
		if(percent < 100.0 && !isHandler())
		{
			double wit_bonus = getPower() * (BaseStats.WIT.calcBonus(activeChar) - 1.0);
			percent += wit_bonus > 20.0 ? 20.0 : wit_bonus;
			if(percent > 90.0)
				percent = 90.0;
		}
		if(target.isPet() && _canPet)
		{
			if(targetPlayer == activeChar)
				((PetInstance) target).doRevive(percent);
			else
				targetPlayer.reviveRequest((Player) activeChar, percent, true);
		}
		else if(target.isPlayer())
		{
			if(getTargetType() == SkillTargetType.TARGET_ONE_SERVITOR)
				return;
			IntObjectPair<OnAnswerListener> ask = targetPlayer.getAskListener(false);
			ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
			if(reviveAsk != null)
				return;
			targetPlayer.reviveRequest(activeChar.getPlayer(), percent, false);
		}
	}

	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		if(oneTarget())
			return super.getTargets(activeChar, aimingTarget, forceUse);
		List<Creature> list = super.getTargets(activeChar, aimingTarget, forceUse);
		Iterator<Creature> iterator = list.iterator();
		while(iterator.hasNext())
		{
			Creature target = iterator.next();
			List<Event> events = new ArrayList<>(GLOBAL.size() + 2);
			events.addAll(GLOBAL);
			events.addAll(target.getZoneEvents());
			for(Event e : events)
				if(!e.canResurrect(activeChar, target, true, true))
					iterator.remove();
		}
		return list;
	}

}
