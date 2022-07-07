package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.ArrayList;
import java.util.List;

public class EffectDiscord extends Abnormal
{
	public EffectDiscord(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		boolean multitargets = _skill.isAoE();
		if(!_effected.isMonster())
		{
			if(!multitargets)
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		if(_effected.isFearImmune() || _effected.isRaid())
		{
			if(!multitargets)
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		Player player = _effected.getPlayer();
		if(player != null)
		{
			SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
			if(_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((Servitor) _effected))
			{
				if(!multitargets)
					getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}
		if(_effected.isInPeaceZone())
		{
			if(!multitargets)
				getEffector().sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return false;
		}
		int skilldiff = _effected.getLevel() - _skill.getMagicLevel();
		int lvldiff = _effected.getLevel() - _effector.getLevel();
		if(skilldiff > 10 || skilldiff > 5 && Rnd.chance(30) || Rnd.chance(Math.abs(lvldiff) * 2))
		{
			if(!multitargets)
			{
				ExMagicAttackInfo.packet(getEffector(), getEffected(), MagicAttackType.RESISTED);
				getEffector().sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(getSkill()));
			}
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startConfused();
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(!_effected.stopConfused())
		{
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
			_effected.stopMove();
			_effected.getAI().setAttackTarget(null);
			_effected.setWalking();
			_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime()
	{
		List<Creature> targetList = new ArrayList<>();
		for(Creature character : _effected.getAroundCharacters(900, 200))
			if(character.isNpc() && character != getEffected())
				targetList.add(character);
		if(targetList.isEmpty())
			return true;
		Creature target = targetList.get(Rnd.get(targetList.size()));
		_effected.setRunning();
		_effected.getAI().Attack(target, true, false);
		return false;
	}
}
