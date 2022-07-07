package l2s.gameserver.skills.effects;

import l2s.gameserver.listener.actor.OnAttackListener;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDispelOnHit extends Abnormal
{
	private final int _maxHitCount;
	private AttackListener _listener;
	private int _hitCount;

	public EffectDispelOnHit(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_hitCount = 0;
		_maxHitCount = getTemplate().getParam().getInteger("max_hits", 0);
	}

	private void onAttack()
	{
		++_hitCount;
		if(_hitCount >= _maxHitCount)
			getEffected().getAbnormalList().stopEffects(getSkill());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_listener = new AttackListener();
		getEffected().addListener(_listener);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().removeListener(_listener);
	}

	private class AttackListener implements OnAttackListener, OnMagicUseListener
	{
		@Override
		public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
		{
			if(!skill.isOffensive())
				return;
			EffectDispelOnHit.this.onAttack();
		}

		@Override
		public void onAttack(Creature actor, Creature target)
		{
			EffectDispelOnHit.this.onAttack();
		}
	}
}
