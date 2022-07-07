package l2s.gameserver.skills.effects;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectCurseOfLifeFlow extends Abnormal
{
	private CurseOfLifeFlowListener _listener;
	private final TObjectIntHashMap<HardReference<? extends Creature>> _damageList;

	public EffectCurseOfLifeFlow(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_damageList = new TObjectIntHashMap<>();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_listener = new CurseOfLifeFlowListener();
		_effected.addListener(_listener);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.removeListener(_listener);
		_listener = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		TObjectIntIterator<HardReference<? extends Creature>> iterator = _damageList.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			Creature damager = (Creature) ((HardReference) iterator.key()).get();
			if(damager != null && !damager.isDead())
			{
				if(damager.isCurrentHpFull())
					continue;
				int damage = iterator.value();
				if(damage <= 0)
					continue;
				double max_heal = calc();
				double heal = Math.min(damage, max_heal);
				double newHp = Math.min(damager.getCurrentHp() + heal, damager.getMaxHp());
				if(damager != getEffector())
					damager.sendPacket(new SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(getEffector()).addNumber((long) (newHp - damager.getCurrentHp())));
				else
					damager.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber((long) (newHp - damager.getCurrentHp())));
				damager.setCurrentHp(newHp, false);
			}
		}
		_damageList.clear();
		return true;
	}

	private class CurseOfLifeFlowListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill, boolean sharedDamage)
		{
			if(attacker == actor || attacker == _effected)
				return;
			int old_damage = _damageList.get(attacker.getRef());
			_damageList.put(attacker.getRef(), old_damage == 0 ? (int) damage : old_damage + (int) damage);
		}
	}
}
