package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.LostItems;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.skills.targets.TargetType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

/**
 * AI боса Байума.<br>
 * - Мгновенно убивает первого ударившего<br>
 * - Для атаки использует только скилы по следующей схеме:
 * <li>Стандартный набор: 80% - 4127, 10% - 4128, 10% - 4129
 * <li>если хп < 50%: 70% - 4127, 10% - 4128, 10% - 4129, 10% - 4131
 * <li>если хп < 25%: 60% - 4127, 10% - 4128, 10% - 4129, 10% - 4131, 10% - 4130
 *
 * @author SYS
 * @reworked by Bonux
 */
public abstract class AbstractBaiumAI extends DefaultAI<NpcInstance>
{
	private final int ACTION_1_TIMER_ID = 100001;
	private final int ACTION_2_TIMER_ID = 100002;
	private final int ACTION_3_TIMER_ID = 100003;
	private final int ACTION_4_TIMER_ID = 100004;
	private final int ACTION_5_TIMER_ID = 100005;
	private final int ACTION_6_TIMER_ID = 100006;

	// Боевые скилы байума
	private final SkillEntry baium_normal_attack = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4127, 1);
	private final SkillEntry energy_wave = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4128, 1);
	private final SkillEntry earth_quake = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4129, 1);
	private final SkillEntry thunderbolt = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4130, 1);
	private final SkillEntry group_hold = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4131, 1);
	// TODO use this skills
	private final SkillEntry SPEAR_ATTACK = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4132, 1);
	private final SkillEntry ANGEL_HEAL = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4133, 1);
	private final SkillEntry HEAL_OF_BAIUM = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4135, 1);
	private final SkillEntry BAIUM_PRESENT = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4136, 1);
	private final SkillEntry ANTI_STRIDER = SkillEntry.makeSkillEntry(SkillEntryType.NONE,4258, 1);

	private Creature awakener = null;

	private AtomicBoolean firstTimeAttacked = new AtomicBoolean(true);

	public AbstractBaiumAI(NpcInstance actor)
	{
		super(actor);
	}

	public void setAwakener(Creature awakener) {
		this.awakener = awakener;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void onEvtSpawn() {
		super.onEvtSpawn();

		NpcInstance npc = getActor();
		npc.getFlags().getImmobilized().start();
		npc.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_A", 1, 0, npc.getLoc()));
		npc.broadcastPacket(new SocialActionPacket(npc.getObjectId(), 2));

		addTimer(ACTION_1_TIMER_ID, 15000);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2) {
		super.onEvtTimer(timerId, arg1, arg2);

		NpcInstance actor = getActor();
		if(timerId == ACTION_1_TIMER_ID) {
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 3));
			addTimer(ACTION_2_TIMER_ID, 10000);
		}
		else if(timerId == ACTION_2_TIMER_ID) {
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 1));
			actor.broadcastPacket(new EarthQuakePacket(actor.getLoc(), 40, 5));
			addTimer(ACTION_3_TIMER_ID, 1000);
		}
		else if(timerId == ACTION_3_TIMER_ID) {
			SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4136, 1);
			if (awakener != null && skillEntry != null) {
				actor.setTarget(awakener);
				actor.doCast(skillEntry, awakener, false);
			}
			addTimer(ACTION_4_TIMER_ID, 9000);
		}
		else if(timerId == ACTION_4_TIMER_ID) {
			createOnePrivateEx(getArchangelId(), "ai_boss06_angel", 0, 0, 115792, 16608, 10136, 0, 1, 0, 0);
			createOnePrivateEx(getArchangelId(), "ai_boss06_angel", 0, 0, 115168, 17200, 10136, 0, 2, 0, 0);
			createOnePrivateEx(getArchangelId(), "ai_boss06_angel", 0, 0, 115780, 15564, 10136, 13620, 3, 0, 0);
			createOnePrivateEx(getArchangelId(), "ai_boss06_angel", 0, 0, 114880, 16236, 10136, 5400, 4, 0, 0);
			createOnePrivateEx(getArchangelId(), "ai_boss06_angel", 0, 0, 114239, 17168, 10136, -1992, 5, 0, 0);
			addTimer(ACTION_5_TIMER_ID, 500);
		}
		else if(timerId == ACTION_5_TIMER_ID) {
			actor.getFlags().getImmobilized().stop();
			addTimer(ACTION_6_TIMER_ID, 500);
		}
		else if(timerId == ACTION_6_TIMER_ID) {
			if(actor.getAI().getIntention() == AI_INTENTION_ACTIVE) {
				actor.setRunning();
				actor.getMovement().moveToLocation(new Location(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0), 0, false);
			}
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
			return;

		if(firstTimeAttacked.compareAndSet(true, false))
		{
			if(attacker.isPlayer())
			{
				for(Servitor s : attacker.getServitors())
					s.doDie(actor);
			}
			else if(attacker.isServitor() && attacker.getPlayer() != null)
				attacker.getPlayer().doDie(actor);

			attacker.doDie(actor);
		}

		if (attacker.isPlayer()) {
			if (attacker.getPlayer().getMountType() == MountType.STRIDER && !attacker.getAbnormalList().contains(ANTI_STRIDER)) {
				if (!actor.isSkillDisabled(ANTI_STRIDER.getTemplate())) {
					addTaskCast(attacker, ANTI_STRIDER);
				}
			}
		}

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected boolean createNewTask()
	{
		NpcInstance actor = getActor();
		if(!checkIfInLairZone(actor))
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				return maybeMoveToHome(true);
			}
			return false;
		}

		clearTasks();

		Creature target;
		if((target = prepareTarget()) == null)
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				//return maybeMoveToHome(true);
			}
			return false;
		}

		if(!checkIfInLairZone(target))
		{
			actor.getAggroList().remove(target, false);
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				//return maybeMoveToHome(true);
			}
			return false;
		}

		SkillEntry r_skill;

		if(actor.isMovementDisabled()) // Если в руте, то использовать массовый скилл дальнего боя
			r_skill = thunderbolt;
		else // Выбираем скилл атаки
		{
			r_skill = selectSkillToUse(actor, target);
		}

		// Использовать скилл если можно, иначе атаковать скилом baium_normal_attack
		if(r_skill == null)
			r_skill = baium_normal_attack;
		else if(r_skill.getTemplate().getTargetTypeNew() == TargetType.SELF)
			target = actor;
		else if(r_skill.getTemplate().getTargetType() == Skill.SkillTargetType.TARGET_SELF)
			target = actor;

		// Добавить новое задание
		addTaskCast(target, r_skill);
		return true;
	}

	private SkillEntry selectSkillToUse(NpcInstance actor, Creature target) {
		Map<SkillEntry, Integer> d_skill = new HashMap<SkillEntry, Integer>(); //TODO class field ?
		double distance = actor.getDistance(target);

		if (actor.getCurrentHpRatio() > 75) {
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, energy_wave);
			}
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, earth_quake);
			}
		} else if (actor.getCurrentHpRatio() > 50) {
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, group_hold);
			}
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, energy_wave);
			}
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, earth_quake);
			}
		} else {
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, thunderbolt);
			}
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, group_hold);
			}
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, energy_wave);
			}
			if (Rnd.chance(10)) {
				addDesiredSkill(d_skill, target, distance, earth_quake);
			}
		}

		return selectTopSkill(d_skill);
	}

	@Override
	protected boolean maybeMoveToHome(boolean force)
	{
		NpcInstance actor = getActor();
		if(actor != null && !checkIfInLairZone(actor))
			teleportHome();
		return false;
	}

	@Override
	protected void onEvtDead(Creature killer, LostItems lostItems)
	{
		firstTimeAttacked.set(true);

		NpcInstance actor = getActor();
		actor.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_D", 1, 0, actor.getLoc()));
		super.onEvtDead(killer, lostItems);
	}

	protected abstract boolean checkIfInLairZone(Creature target);

	protected abstract int getArchangelId();
}