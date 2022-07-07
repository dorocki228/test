package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class p_block_debuff extends Abnormal
{
	private final int _maxDebuffsDisabled;
	private int _disabledDebuffs;

	public p_block_debuff(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_disabledDebuffs = 0;
		_maxDebuffsDisabled = getTemplate().getParam().getInteger("max_disabled_debuffs", -1);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startDebuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopDebuffImmunity();
	}

	@Override
	public boolean checkDebuffImmunity()
	{
		if(_maxDebuffsDisabled > 0)
		{
			++_disabledDebuffs;
			if(getEffected().isPlayer() && getEffected().getPlayer().isGM())
				getEffected().sendMessage("DebuffImmunity: disabled_debuffs: " + _disabledDebuffs + " max_disabled_debuffs: " + _maxDebuffsDisabled);
			if(_disabledDebuffs >= _maxDebuffsDisabled)
			{
				getEffected().getAbnormalList().stopEffects(getSkill());
				if(getEffected().isPlayer() && getEffected().getPlayer().isGM())
					getEffected().sendMessage("DebuffImmunity: All disabled. Abnormal canceled.");
			}
		}
		return true;
	}
}
