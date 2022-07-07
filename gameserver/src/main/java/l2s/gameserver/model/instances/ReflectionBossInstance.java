package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ReflectionBossInstance extends RaidBossInstance
{
	private static final long serialVersionUID = 1L;
	private static final long COLLAPSE_AFTER_DEATH_TIME = 300000L;

	public ReflectionBossInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		getMinionList().unspawnMinions();
		super.onDeath(killer);
		clearReflection();
	}

	protected void clearReflection()
	{
		Reflection reflection = getReflection();
		if(!reflection.isDefault())
			reflection.startCollapseTimer(COLLAPSE_AFTER_DEATH_TIME);
	}
}
