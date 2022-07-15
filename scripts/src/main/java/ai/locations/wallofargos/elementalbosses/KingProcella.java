package ai.locations.wallofargos.elementalbosses;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.LostItems;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * @author Java-man
 */
public class KingProcella extends Fighter<NpcInstance>
{
	private final List<Action> actions = List.of(
			new TornadoSummonAction(),
			new GuardsSummonAction(80),
			new GuardsSummonAction(50),
			new GuardsSummonAction(25));

	public KingProcella(NpcInstance actor)
	{
		super(actor);		
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		actions.forEach(action -> {
			if (action.check(getActor(), attacker, skill, damage)) {
				action.execute(getActor(), attacker, skill, damage);
			}
		});

		super.onEvtAttacked(attacker, skill, damage);
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
		private final AtomicBoolean executed = new AtomicBoolean(false);

		void execute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			if (executed.compareAndSet(false, true)) {
				doExecute(actor, attacker, skill, damage);
			}
		}

		boolean check(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			if (!executed.get()) {
				return canExecute(actor, attacker, skill, damage);
			}

			return false;
		}

		abstract boolean canExecute(NpcInstance actor, Creature attacker, Skill skill, int damage);

		abstract void doExecute(NpcInstance actor, Creature attacker, Skill skill, int damage);
	}

	private class TornadoSummonAction extends Action {
		@Override
		boolean canExecute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			return actor.getCurrentHpPercents() <= 90;
		}

		@Override
		void doExecute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			Zone zone = actor.getZone(Zone.ZoneType.CHANGED_ZONE);
			NpcUtils.spawnSimple(29115, zone.getTerritory(), actor.getReflection(), 30, 0, 0);
		}
	}

	private class GuardsSummonAction extends Action {
		private int hpPercent;

		private GuardsSummonAction(int hpPercent) {
			this.hpPercent = hpPercent;
		}

		@Override
		boolean canExecute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			return actor.getCurrentHpPercents() <= hpPercent;
		}

		@Override
		void doExecute(NpcInstance actor, Creature attacker, Skill skill, int damage) {
			Zone zone = actor.getZone(Zone.ZoneType.CHANGED_ZONE);
			NpcUtils.spawnSingle(29112, zone.getTerritory(), actor.getReflection());
			NpcUtils.spawnSingle(29113, zone.getTerritory(), actor.getReflection());
			NpcUtils.spawnSingle(29114, zone.getTerritory(), actor.getReflection());
		}
	}
}