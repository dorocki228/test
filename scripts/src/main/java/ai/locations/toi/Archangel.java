package ai.locations.toi;

import bosses.BaiumManager;
import events.BossSpawnEvent;
import gnu.trove.map.TIntObjectMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.HashMap;
import java.util.Map;

public class Archangel extends Fighter
{
	private final Skill rapid_spear_attack;
	private final Skill angel_heal;

	public Archangel(NpcInstance actor)
	{
		super(actor);

		TIntObjectMap<Skill> skills = getActor().getTemplate().getSkills();
		rapid_spear_attack = skills.get(4132);
		angel_heal = skills.get(4133);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		setGlobalAggro(0);
		addTimer(2001, 5000);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == 2001)
		{
			MonsterInstance mob = getActor();
			Creature mostHated = mob.getAggroList().getMostHated(mob.getAI().getMaxHateRange());
			NpcInstance leader = mob.getLeader();
			Zone zone;
			if (leader != null) {
				BossSpawnEvent event = leader.getEvent(BossSpawnEvent.class);
				if (event != null) {
					zone = event.getZone(leader.getReflection(), "[baium_epic]");
				} else {
					zone = BaiumManager.getZone();
				}
			} else {
				zone = BaiumManager.getZone();
			}
			if((mostHated != null) && mostHated.isPlayer() && zone != null && zone.checkIfInZone(mostHated))
			{
				if(mob.getTarget() != mostHated)
					mob.getAggroList().clear(true);

				mob.getAggroList().addDamageHate(mostHated, 0, 10);
			}
			else
			{
				boolean found = false;
				for(Creature creature : mob.getAroundCharacters(mob.getAggroRange(), 100))
				{
					if(!creature.isPlayable())
						continue;

					if(zone != null && zone.checkIfInZone(creature) && !creature.isDead() && !creature.isFakeDeath())
					{
						if(mob.getTarget() != creature)
							mob.getAggroList().clear(true);

						mob.getAggroList().addDamageHate(creature, 0, 10);
						found = true;
						break;
					}
				}

				if(!found)
				{
					if (leader != null) {
						mob.getAggroList().addDamageHate(leader, 0, 10);
						mob.setRunning();
						addTaskAttack(leader);
					}
				}
			}
			addTimer(2001, 5000);
		}

		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected boolean createNewTask()
	{
		MonsterInstance actor = getActor();
		if(actor == null)
			return true;

		NpcInstance leader = actor.getLeader();
		if(leader != null) {
			BossSpawnEvent event = leader.getEvent(BossSpawnEvent.class);
			if(event != null) {
				Zone zone = event.getZone(leader.getReflection(), "[baium_epic]");
				if (zone != null && zone.checkIfInZone(actor)) {
					teleportHome();
					return false;
				}
			} else if (!BaiumManager.getZone().checkIfInZone(actor)) {
				teleportHome();
				return false;
			}
		}
		else {
			if (!BaiumManager.getZone().checkIfInZone(actor)) {
				teleportHome();
				return false;
			}
		}

		clearTasks();

		Creature target = prepareTarget();
		if(target == null)
			return false;


		if (leader != null) {
			BossSpawnEvent event = leader.getEvent(BossSpawnEvent.class);
			if (event != null) {
				Zone zone = event.getZone(leader.getReflection(), "[baium_epic]");
				if (zone != null && zone.checkIfInZone(actor)) {
					actor.getAggroList().remove(target, false);
					return false;
				}
			} else if (!BaiumManager.getZone().checkIfInZone(actor)) {
				teleportHome();
				return false;
			}
		} else {
			if (!BaiumManager.getZone().checkIfInZone(target)) {
				actor.getAggroList().remove(target, false);
				return false;
			}
		}

        Map<Skill, Integer> d_skill = new HashMap<>();
		double distance = actor.getDistance(target);

		if(Rnd.get(100) < 10)
			addDesiredSkill(d_skill, target, distance, rapid_spear_attack);

		if(Rnd.get(100) < 5 && ((actor.getCurrentHp() / actor.getMaxHp()) * 100) < 50)
			addDesiredSkill(d_skill, target, distance, angel_heal);

        Skill r_skill = selectTopSkill(d_skill);

        if(r_skill != null)
		{
			if(r_skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF)
				target = actor;

			addTaskCast(target, r_skill);
		}
		else
			addTaskAttack(target);
		return true;
	}

	@Override
	public MonsterInstance getActor()
	{
		return (MonsterInstance) super.getActor();
	}
}
