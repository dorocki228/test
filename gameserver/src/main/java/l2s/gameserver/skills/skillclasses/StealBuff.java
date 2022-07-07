package l2s.gameserver.skills.skillclasses;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.EffectsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StealBuff extends Skill
{
	private final int _stealCount;

	public StealBuff(StatsSet set)
	{
		super(set);
		_stealCount = set.getInteger("stealCount", 1);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		return true;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!target.isPlayer())
			return;
		if(calcStealChance(target, activeChar))
		{
			int stealCount = Rnd.get(1, _stealCount);
			TIntSet stelledSkillIds = new TIntHashSet();
			List<Abnormal> effects = new ArrayList<>(target.getAbnormalList().getEffects());
			effects.sort(EffectsComparator.getInstance());
			Collections.reverse(effects);
			for(Abnormal effect : effects)
			{
				if(effect.isOffensive())
					continue;
				if(!effect.isOfUseType(EffectUseType.NORMAL))
					continue;
				if(!effect.isCancelable())
					continue;
				Skill effectSkill = effect.getSkill();
				if(effectSkill == null)
					continue;
				if(!stelledSkillIds.contains(effectSkill.getId()) && stelledSkillIds.size() < stealCount)
					continue;
				if(effectSkill.isToggle())
					continue;
				if(effectSkill.isPassive())
					continue;
				if(target.isSpecialEffect(effectSkill))
					continue;
				stealEffect(activeChar, effect);
				effect.exit();
				stelledSkillIds.add(effectSkill.getId());
			}
		}
		else
		{
			ExMagicAttackInfo.packet(activeChar, target, MagicAttackType.RESISTED);
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(target).addSkillName(getId(), getLevel()));
		}
	}

	private boolean calcStealChance(Creature effected, Creature effector)
	{
		double cancel_res_multiplier = effected.calcStat(Stats.CANCEL_RESIST, 1.0, null, null);
		int dml = effector.getLevel() - effected.getLevel();
		double prelimChance = (dml + 50) * (1.0 - cancel_res_multiplier * 0.01);
		return Rnd.chance(prelimChance);
	}

	private void stealEffect(Creature character, Abnormal effect)
	{
		Abnormal e = effect.getTemplate().getEffect(character, character, effect.getSkill());
		if(e == null)
			return;
		e.setDuration(effect.getDuration());
		e.setTimeLeft(effect.getTimeLeft());
		character.getAbnormalList().addEffect(e);
	}
}
