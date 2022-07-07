package ai.locations.loa;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

public class MalukKnight extends Fighter
{
	//Monster ID's
	private static final int BANSHEE = 20406;

	private static final double SPAWN_CHANCE = 7.; // TODO: Проверить шанс.
	private static final int DESPAWN_TIME = 300000;

	public MalukKnight(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if(Rnd.chance(SPAWN_CHANCE))
		{
			NpcInstance actor = getActor();
			NpcUtils.spawnSingle(BANSHEE, actor.getSpawnedLoc(), DESPAWN_TIME);
		}
	}
}
