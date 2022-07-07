package l2s.gameserver.ai;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExRotation;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;

public class PlayerAI extends PlayableAI
{
	public PlayerAI(Player actor)
	{
		super(actor);
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
		if(actor.isSitting())
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
		if(actor.isSitting())
		{
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionPickUp(object);
	}

	@Override
	protected void thinkAttack(boolean checkRange)
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
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}
		if(actor.isFrozen())
		{
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFailPacket.STATIC);
			return;
		}
		super.thinkAttack(checkRange);
	}

	@Override
	protected boolean thinkCast(boolean checkRange)
	{
		Player actor = getActor();
		FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
		if(attachment != null && !attachment.canCast(actor, _skill))
		{
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return false;
		}
		if(actor.isFrozen())
		{
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFailPacket.STATIC);
			return false;
		}
		return super.thinkCast(checkRange);
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

		if(cancel || !actor.isInRange(target, target.getActingRange() / 2) || actor.isInRange(target, 20) || actor.getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(actor, target, false))
		{
			target.sendPacket(SystemMsg.THE_COUPLE_ACTION_WAS_CANCELLED);
			actor.sendPacket(SystemMsg.THE_COUPLE_ACTION_WAS_CANCELLED);
			return;
		}
		if(_forceUse)
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_COUPLE_ACTION, actor, socialId);
		ThreadPoolManager.getInstance().schedule(() -> {
			int heading = actor.calcHeading(target.getX(), target.getY());
			actor.setHeading(heading);
			actor.broadcastPacket(new ExRotation(actor.getObjectId(), heading));
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), socialId));
		}, 500);
	}

	@Override
	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		Player actor = getActor();
		if(actor.isInFlyingTransform())
		{
			actor.sendActionFailed();
			return;
		}
		if(System.currentTimeMillis() - actor.getLastAttackPacket() < Config.ATTACK_PACKET_DELAY)
		{
			actor.sendActionFailed();
			return;
		}
		actor.setLastAttackPacket();
		if(actor.getSittingTask())
		{
			setNextAction(AINextAction.ATTACK, target, null, forceUse, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}

		if(target.isPlayable())
		{
			for(Event event : actor.getEvents())
				if(event.checkForAttack((Creature) target, actor, null, forceUse) != null)
				{
					clientActionFailed();
					return;
				}
		}

		super.Attack(target, forceUse, dontMove);
	}

	@Override
	public boolean Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		Player actor = getActor();
		if(actor == null)
		{
			clientActionFailed();
			return false;
		}
		Skill castingSkill = actor.getCastingSkill();
		if(castingSkill != null && (castingSkill.hasEffect(EffectUseType.NORMAL, EffectType.Transformation) || castingSkill.isToggle()))
		{
			clientActionFailed();
			return false;
		}
		if(!skill.altUse() && (!skill.isToggle() || skill.getHitTime() > 0) && (skill.getSkillType() != Skill.SkillType.CRAFT || !Config.ALLOW_TALK_WHILE_SITTING))
			if(actor.getSittingTask())
			{
				if(!skill.isHandler())
				{
					setNextAction(AINextAction.CAST, skill, target, forceUse, dontMove);
					clientActionFailed();
					return true;
				}
				clientActionFailed();
				return false;
			}
			else
			{
				if(skill.getSkillType() == Skill.SkillType.SUMMON && actor.getPrivateStoreType() != 0)
				{
					actor.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE);
					clientActionFailed();
					return false;
				}
				if(actor.isSitting())
				{
					if(skill.hasEffect(EffectUseType.NORMAL, EffectType.Transformation))
						actor.sendPacket(SystemMsg.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
					else
						actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
					clientActionFailed();
					return false;
				}
			}
		return super.Cast(skill, target, forceUse, dontMove);
	}

	@Override
	public Player getActor()
	{
		return (Player) super.getActor();
	}
}
