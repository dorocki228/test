package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class ArchangelInstance extends MonsterInstance
{

	private static final int Baium = 29020;

	public ArchangelInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if(attacker.getNpcId() == Baium)
			return true;

		return super.isAutoAttackable(attacker);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		if(attacker.getNpcId() == Baium)
			return true;

		return super.isAttackable(attacker);
	}
}