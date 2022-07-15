package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ProcellasStormInstance extends MonsterInstance
{
	public ProcellasStormInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public boolean isInvulnerable()
	{
		return true;
	}

	public boolean isTargetable(Creature creature)
	{
		return false;
	}
}