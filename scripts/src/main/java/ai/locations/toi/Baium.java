package ai.locations.toi;

import bosses.BaiumManager;
import events.BossSpawnEvent;
import gnu.trove.map.TIntObjectMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.HashMap;
import java.util.Map;

public class Baium extends DefaultAI
{

	// Боевые скилы байума
	private final Skill baium_normal_attack, energy_wave, earth_quake, thunderbolt, group_hold;

	public Baium(NpcInstance actor)
	{
		super(actor);
		TIntObjectMap<Skill> skills = getActor().getTemplate().getSkills();
		baium_normal_attack = skills.get(4127);
		energy_wave = skills.get(4128);
		earth_quake = skills.get(4129);
		thunderbolt = skills.get(4130);
		group_hold = skills.get(4131);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(actor != null && attacker.isPlayable()) {
			BossSpawnEvent event = actor.getEvent(BossSpawnEvent.class);
			if (event == null) {
				BaiumManager.setLastAttackTime();
			}
		}
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected boolean createNewTask()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		BossSpawnEvent event = actor.getEvent(BossSpawnEvent.class);
		if (event == null) {
			if (!BaiumManager.getZone().checkIfInZone(actor)) {
				teleportHome();
				return false;
			}
		}
		else {
			Zone zone = event.getZone(actor.getReflection(), "[baium_epic]");
			if (zone != null && zone.checkIfInZone(actor)) {
				teleportHome();
				return false;
			}
		}

		clearTasks();

		Creature target = prepareTarget();
		if(target == null)
			return false;

		if (event != null) {
			Zone zone = event.getZone(actor.getReflection(), "[baium_epic]");
			if (zone != null && zone.checkIfInZone(actor)) {
				actor.getAggroList().remove(target, false);
				return false;
			}
		} else if (!BaiumManager.getZone().checkIfInZone(target)) {
			actor.getAggroList().remove(target, false);
			return false;
		}

		Skill r_skill;

		if(actor.isMovementDisabled()) // Если в руте, то использовать массовый скилл дальнего боя
		{
			r_skill = thunderbolt;
		}
		else
		{
			Map<Skill, Integer> d_skill = new HashMap<>();
			double distance = actor.getDistance(target);

			if(actor.getCurrentHp() > ((actor.getMaxHp() * 3) / 4))
			{
				if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, energy_wave);
				else if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, earth_quake);
				else
					addDesiredSkill(d_skill, target, distance, baium_normal_attack);
			}
			else if(actor.getCurrentHp() > ((actor.getMaxHp() * 2) / 4))
			{
				if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, group_hold);
				else if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, energy_wave);
				else if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, earth_quake);
				else
					addDesiredSkill(d_skill, target, distance, baium_normal_attack);
			}
			else if(actor.getCurrentHp() > ((actor.getMaxHp() * 1) / 4))
			{
				if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, thunderbolt);
				else if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, group_hold);
				else if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, energy_wave);
				else if(Rnd.get(100) < 10)
					addDesiredSkill(d_skill, target, distance, earth_quake);
				else
					addDesiredSkill(d_skill, target, distance, baium_normal_attack);
			}
			else if(Rnd.get(100) < 10)
				addDesiredSkill(d_skill, target, distance, thunderbolt);
			else if(Rnd.get(100) < 10)
				addDesiredSkill(d_skill, target, distance, group_hold);
			else if(Rnd.get(100) < 10)
				addDesiredSkill(d_skill, target, distance, energy_wave);
			else if(Rnd.get(100) < 10)
				addDesiredSkill(d_skill, target, distance, earth_quake);
			else
				addDesiredSkill(d_skill, target, distance, baium_normal_attack);

			r_skill = selectTopSkill(d_skill);
		}

		if(r_skill != null && r_skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF)
		{
			target = actor;
		}
		// Добавить новое задание
		addTaskCast(target, r_skill);
		return true;
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		/*NpcInstance actor = getActor();
		if(actor != null && !BaiumManager.getZone().checkIfInZone(actor))
		{
			teleportHome();
		}*/
		return false;
	}

}