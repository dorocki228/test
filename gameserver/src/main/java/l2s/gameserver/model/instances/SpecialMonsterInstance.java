package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * Это алиас L2MonsterInstance используемый для монстров, у которых нестандартные статы
 */
public class SpecialMonsterInstance extends MonsterInstance
{
	public SpecialMonsterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	@Override
	public boolean isLethalImmune() {
		return false;
	}

	@Override
	public boolean isFearImmune() {
		return true;
	}

	@Override
	public boolean isParalyzeImmune() {
		return true;
	}

	/*@Override
	public boolean isAMutedImmune()
	{
		return true;
	}*/
}