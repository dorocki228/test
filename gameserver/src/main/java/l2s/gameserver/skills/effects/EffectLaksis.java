package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectLaksis extends Abnormal
{
	public EffectLaksis(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		Player caster = (Player) getEffector();
		for(Creature cha : caster.getAroundCharacters(getSkill().getAffectRange(), 200))
			if(cha != null && cha.isPlayer())
			{
                if(!cha.isPlayer())
					continue;
				Player player = (Player) getEffector();
				Player target = (Player) cha;
                boolean heal = false;
                if(player.getParty() != null && (player.isInSameParty(target) || player.isInSameChannel(target)))
					heal = true;
				if(player.getClan() != null && !player.isInPeaceZone() && (player.isInSameClan(target) || player.isInSameAlly(target)))
					heal = true;
				if(heal)
				{
					if(target == null)
						continue;
					if(target.isDead())
						continue;
					double powerCP = calc();
					double powerHP = calc();
					powerCP = Math.min(powerCP, target.getMaxCp() - target.getCurrentCp());
					powerHP = Math.min(powerHP, target.getMaxHp() - target.getCurrentHp());
					if(powerCP < 0.0)
						powerCP = 0.0;
					if(powerHP < 0.0)
						powerHP = 0.0;
					if(target.getCurrentCp() < target.getMaxCp())
					{
						target.setCurrentCp(powerCP + target.getCurrentCp());
						target.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addNumber((long) powerCP));
					}
					else
					{
						target.setCurrentHp(powerHP + target.getCurrentHp(), false);
						target.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber((long) powerHP));
					}
				}
				else
				{
					if(target == null)
						continue;
					if(target.isDead())
						continue;
					if(target.getPvpFlag() <= 0 && !target.isAutoAttackable(player) || target.isInPeaceZone())
						continue;
					if(player.getPvpFlag() == 0)
						player.startPvPFlag(null);
					double damage = calc();
					target.reduceCurrentHp(damage, player, getSkill(), true, true, false, true, false, false, true);
				}
			}
			else
			{
				if(cha == null || !cha.isMonster() || cha.isInPeaceZone())
					continue;
				double damage2 = calc();
				cha.reduceCurrentHp(damage2, caster, getSkill(), true, true, false, true, false, false, true);
				cha.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster, getSkill(), damage2);
			}
	}
}
