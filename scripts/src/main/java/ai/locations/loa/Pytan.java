package ai.locations.loa;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

public class Pytan extends Mystic
{
	//Monster ID's
	private static final int KNORIKS = 20405;

	private static final double SPAWN_CHANCE = 2.; // TODO: Проверить шанс.
	private static final int DESPAWN_TIME = 300000;

	public Pytan(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		// TODO: Нужна ли тут задержка?
		if(Rnd.chance(SPAWN_CHANCE))
		{
			NpcInstance actor = getActor();

			NpcUtils.spawnSingle(KNORIKS, actor.getSpawnedLoc(), DESPAWN_TIME);
		}
	}
}
