package ai.locations.wallofargos.elementalbosses;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.LostItems;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * @author Java-man
 */
public class QueenNebula extends Mystic<NpcInstance>
{
	private final List<Action> actions = List.of(new WaterSlimSummonAction());

	public QueenNebula(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		executeTasks();
	}

	private void executeTasks() {
		actions.forEach(action -> {
			if (action.check(getActor())) {
				action.execute(getActor());
			}
		});

		ThreadPoolManager.getInstance().schedule(() -> executeTasks(),
				60, TimeUnit.SECONDS);
	}

	@Override
	protected void onEvtDead(Creature killer, LostItems lostItems)
	{
		cleanUp(getActor());
		super.onEvtDead(killer, lostItems);
	}

	private void cleanUp(NpcInstance actor)
	{
		for(NpcInstance n : actor.getReflection().getNpcs())
			n.deleteMe();
	}

	private abstract class Action {
		void execute(NpcInstance actor) {
			doExecute(actor);
		}

		boolean check(NpcInstance actor) {
			return canExecute(actor);
		}

		abstract boolean canExecute(NpcInstance actor);

		abstract void doExecute(NpcInstance actor);
	}

	private class WaterSlimSummonAction extends Action {
		@Override
		boolean canExecute(NpcInstance actor) {
			return Rnd.chance(50);
		}

		@Override
		void doExecute(NpcInstance actor) {
			Zone zone = actor.getZone(Zone.ZoneType.CHANGED_ZONE);
			NpcUtils.spawnSimple(29111, zone.getTerritory(), actor.getReflection(), 6, 0, 0);
		}
	}
}