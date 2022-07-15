package l2s.gameserver.ai;

import kotlin.sequences.Sequence;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.entity.events.impl.PvPEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.taskmanager.AiTaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

public class PlayerAI extends PlayableAI<Player>
{
	private volatile ScheduledFuture<?> autoplayTask;
	private final long autoPlayTaskDelay = 1_000;

	private volatile AutoplaySettings autoplaySettings;
	private HardReference<? extends Creature> autoPlayAttackTarget = HardReferences.emptyRef();
	private volatile List<ItemInstance> autoPickUpItems = Collections.emptyList();

	public PlayerAI(Player actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttack(Creature target, Skill skill, int damage)
	{
		super.onEvtAttack(target, skill, damage);

		Player actor = getActor();
		if(target == null || actor.isDead())
			return;

		if(damage > 0)
		{
			for(Servitor servitor : actor.getServitors())
				servitor.onOwnerOfAttacks(target);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);

		Player actor = getActor();
		if(attacker == null || actor.isDead())
			return;

		if(damage > 0)
		{
			for(Servitor servitor : actor.getServitors())
				servitor.onOwnerGotAttacked(attacker);
		}
	}

	@Override
	protected void onIntentionRest()
	{
		changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		setAttackTarget(null);
		clientStopMoving();
	}

	@Override
	protected void onIntentionActive()
	{
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
	}

	@Override
	public void onIntentionInteract(GameObject object)
	{
		Player actor = getActor();

		if(actor.getSittingTask())
		{
			setNextAction(AINextAction.INTERACT, object, null, false, false);
			return;
		}
		else if(actor.isSitting())
		{
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionInteract(object);
	}

	@Override
	public void onIntentionPickUp(GameObject object)
	{
		Player actor = getActor();

		if(actor.getSittingTask())
		{
			setNextAction(AINextAction.PICKUP, object, null, false, false);
			return;
		}
		else if(actor.isSitting())
		{
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionPickUp(object);
	}

	@Override
	protected void thinkAttack(boolean arrived)
	{
		Player actor = getActor();

		if(actor.isInFlyingTransform())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}

		FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
		if(attachment != null && !attachment.canAttack(actor))
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		if(actor.isControlBlocked())
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFailPacket.STATIC);
			return;
		}

		super.thinkAttack(arrived);
	}

	@Override
	protected boolean thinkCast(boolean arrived)
	{
		Player actor = getActor();

		FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
		if(attachment != null && !attachment.canCast(actor, _skillEntry.getTemplate()))
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return false;
		}

		if(actor.isControlBlocked())
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFailPacket.STATIC);
			return false;
		}

		return super.thinkCast(arrived);
	}

	@Override
	protected void thinkCoupleAction(Player target, Integer socialId, boolean cancel)
	{
		Player actor = getActor();
		if(target == null || !target.isOnline())
		{
			actor.sendPacket(SystemMsg.THE_COUPLE_ACTION_WAS_CANCELLED);
			return;
		}

		if(cancel || !actor.isInRange(target, 50) || actor.isInRange(target, 20) || actor.getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(actor, target))
		{
			target.sendPacket(SystemMsg.THE_COUPLE_ACTION_WAS_CANCELLED);
			actor.sendPacket(SystemMsg.THE_COUPLE_ACTION_WAS_CANCELLED);
			return;
		}
		if(_forceUse) // служит только для флага что б активировать у другого игрока социалку
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_COUPLE_ACTION, actor, socialId);

		ThreadPoolManager.getInstance().schedule(() -> // Костыль, иначе через раз ВИЗУАЛЬНО начинало парные действия у одного из игроков.
		{
			int heading = actor.calcHeading(target.getX(), target.getY());
			actor.setHeading(heading);
			actor.broadcastPacket(new ExRotation(actor.getObjectId(), heading));
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), socialId));
		}, 500L);
	}

	@Override
	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		Player actor = getActor();

		if(actor.getSittingTask())
		{
			setNextAction(AINextAction.ATTACK, target, null, forceUse, false);
			return;
		}
		else if(actor.isSitting())
		{
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}

		// TODO Может нужно в другое место ? Проблема в том что на автоатаку через ctrl не работают все эти проверки
		if(target instanceof Playable)
		{
			for(PvPEvent event : actor.getEvents(PvPEvent.class))
			{
				if(event.checkForAttack((Creature) target, actor, null, forceUse) != null)
				{
					clientActionFailed();
					return;
				}
			}
		}

		super.Attack(target, forceUse, dontMove);
	}

	@Override
	public boolean Cast(SkillEntry skillEntry, Creature target, boolean forceUse, boolean dontMove)
	{
		Player actor = getActor();

		if(actor == null)
		{
			clientActionFailed();
			return false;
		}

		SkillEntry castingSkillEntry = actor.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();
		if(castingSkillEntry != null)
		{
			Skill castingSkill = castingSkillEntry.getTemplate();
			if(castingSkill.hasEffect(EffectUseType.NORMAL, "Transformation") || castingSkill.isToggle())
			{
				clientActionFailed();
				return false;
			}
		}

		castingSkillEntry = actor.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry();
		if(castingSkillEntry != null)
		{
			Skill castingSkill = castingSkillEntry.getTemplate();
			if(castingSkill.hasEffect(EffectUseType.NORMAL, "Transformation") || castingSkill.isToggle())
			{
				clientActionFailed();
				return false;
			}
		}

		Skill skill = skillEntry.getTemplate();

		if(!skillEntry.isAltUse() && !(skill.isToggle() && skill.getHitTime() <= 0) && !(skill.hasEffect("i_open_common_recipebook") && Config.ALLOW_TALK_WHILE_SITTING))
		{
			// Если в этот момент встаем, то использовать скилл когда встанем
			if(actor.getSittingTask())
			{
				if(!skill.isHandler())
				{
					setNextAction(AINextAction.CAST, skillEntry, target, forceUse, dontMove);
					clientActionFailed();
					return true;
				}
				clientActionFailed();
				return false;
			}
			else if(skill.hasEffect("i_summon") && actor.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
			{
				actor.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE);
				clientActionFailed();
				return false;
			}
			// если сидим - скиллы нельзя использовать
			else if(actor.isSitting())
			{
				if(skill.hasEffect(EffectUseType.NORMAL, "Transformation"))
					actor.sendPacket(SystemMsg.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
				else
					actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);

				clientActionFailed();
				return false;
			}
		}

		return super.Cast(skillEntry, target, forceUse, dontMove);
	}

	@Override
	protected void notifyDie(LostItems lostItems, Creature actor) {
		Player player = getActor();
		DamageInfo damageInfo = player.getDamageInfo();
		actor.broadcastPacket(new DiePacket(actor, lostItems));
		actor.sendPacket(new ExDieInfo(lostItems, damageInfo.copy()));
		damageInfo.clear();
	}

	public boolean isFake()
	{
		return false;
	}

	private synchronized void startAutoplayTask()
	{
		if(autoplayTask == null)
		{
			autoplayTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, autoPlayTaskDelay);
		}
	}

	private synchronized void stopAutoplayTask()
	{
		if(autoplayTask != null)
		{
			autoplayTask.cancel(false);
			autoplayTask = null;
		}
	}

	@Override
	public void run()
	{
		if(autoplayTask == null) {
			return;
		}

		autoplay();
	}

	public void setAutoplaySettings(AutoplaySettings autoplaySettings)
	{
		this.autoplaySettings = autoplaySettings;
	}

	public void startAutoplay() {
		if (!autoplaySettings.pickUpEnabled()) {
			autoPickUpItems = Collections.emptyList();
		}

		Player player = getActor();
		player.sendPacket(new ExAutoplaySetting(autoplaySettings));

		ShortCut shortCut = player.getShortCut(0, ShortCut.PAGE_AUTO_USABLE_MACRO);
		if (shortCut != null) {
			shortCut.setAutoUseEnabled(true);
		}

		setAutoplayAttackTarget(null);

		startAutoplayTask();
	}

	public void stopAutoplay() {
		AutoplaySettings autoplaySettings = this.autoplaySettings;
		if (autoplaySettings != null) {
			this.autoplaySettings = null;

			Player player = getActor();
			player.sendPacket(new ExAutoplaySetting(autoplaySettings.disabled()));

			ShortCut shortCut = player.getShortCut(0, ShortCut.PAGE_AUTO_USABLE_MACRO);
			if (shortCut != null) {
				shortCut.setAutoUseEnabled(false);
			}
		}

		autoPickUpItems = Collections.emptyList();
		setAutoplayAttackTarget(null);

		stopAutoplayTask();
	}

	public void addAutoPickUpItems(List<ItemInstance> items) {
		if (autoplaySettings == null) {
			return;
		}

		if (!autoplaySettings.pickUpEnabled()) {
			return;
		}

		autoPickUpItems = new ArrayList<>(items);
	}

	private void autoplay() {
		Player actor = getActor();

		if (autoplaySettings == null) {
			stopAutoplay();
			return;
		}

		if (actor.isCastingNow() || actor.isAttackingNow()) {
			return;
		}

		/*if (actor.getAutoShortcutsCast().get()) {
			return;
		}*/

		if (getNextAction() == AINextAction.PICKUP) {
			setNextIntention();
			return;
		}

		Creature attackTarget = getAutoplayAttackTarget();
		if (attackTarget != null) {
			final boolean attackTargetValid = isAutoplayAttackTargetValid(actor, attackTarget);
			if (attackTargetValid) {
				if (!attackTarget.equals(actor.getTarget())) {
					actor.setTarget(attackTarget);
				}

			/*if (actor.isInCombat()) {
				if (getAttackTarget() == attackTarget) {
					return;
				}
				if (getCastTarget() == attackTarget) {
					return;
				}
				actor.setTarget(attackTarget);
				actor.sendPacket(ExAutoplayDoMacro.Companion.getAUTOPLAY_MACRO());
				return;
			}*/

				actor.sendPacket(ExAutoplayDoMacro.Companion.getAUTOPLAY_MACRO());
				return;
			} else {
				setAutoplayAttackTarget(null);
			}
		}

		if (!autoPickUpItems.isEmpty()) {
			ItemInstance itemToPickUp = autoPickUpItems.remove(0);
			if (!GeoEngine.canSeeCoord(actor, itemToPickUp.getX(), itemToPickUp.getY(), itemToPickUp.getZ(), false)) {
				return;
			}
			if (!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), itemToPickUp.getX(), itemToPickUp.getY(), itemToPickUp.getZ(), actor.getGeoIndex())) {
				return;
			}

			setIntention(CtrlIntention.AI_INTENTION_PICK_UP, itemToPickUp, null);
			return;
		}

		Sequence<Creature> targets = autoplaySettings.getNextTargetMode().getTargets(actor, autoplaySettings.getNearTargetMode(), autoplaySettings.isInMannerMode());
		Iterator<Creature> iterator = targets.iterator();
		while (iterator.hasNext()) {
			Creature target = iterator.next();
			if (target != null) {
				if (!GeoEngine.canSeeTarget(actor, target)) {
					continue;
				}

				if (!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ(), actor.getGeoIndex())) {
					continue;
				}

				setAutoplayAttackTarget(target);
				actor.setTarget(target);
				actor.sendPacket(ExAutoplayDoMacro.Companion.getAUTOPLAY_MACRO());
				break;
			}
		}
	}

	public void setAutoplayAttackTarget(Creature target)
	{
		autoPlayAttackTarget = target == null ? HardReferences.<Creature> emptyRef() : target.getRef();
	}

	public Creature getAutoplayAttackTarget()
	{
		return autoPlayAttackTarget.get();
	}

	private boolean isAutoplayAttackTargetValid(Player player, Creature attackTarget) {
		if (attackTarget.isDead()) {
			return false;
		}

		if (!attackTarget.isVisible()) {
			return false;
		}

		if (!GeoEngine.canSeeTarget(player, attackTarget)) {
			return false;
		}

		if (!GeoEngine.canMoveToCoord(player.getX(), player.getY(), player.getZ(),
				attackTarget.getX(), attackTarget.getY(), attackTarget.getZ(), player.getGeoIndex())) {
			return false;
		}

		return true;
	}
}