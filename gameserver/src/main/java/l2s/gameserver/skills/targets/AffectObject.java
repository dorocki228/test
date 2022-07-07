package l2s.gameserver.skills.targets;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Mount;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.permission.ActionPermissionComponent;
import l2s.gameserver.permission.ActionPermissionContext;
import l2s.gameserver.permission.EActionPermissionLevel;
import l2s.gameserver.permission.interfaces.IAttackPermission;
import l2s.gameserver.permission.interfaces.IIncomingAttackPermission;

public enum AffectObject
{
	ALL
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return true;
		}
	},
	CLAN
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			if(caster == target)
				return true;
			Player player = caster.getPlayer();
			if(player != null)
			{
				Clan clan = player.getClan();
				if(clan != null)
					return clan == target.getClan();
			}
			else if(caster.isNpc() && target.isNpc())
				return ((NpcInstance) caster).isInFaction((NpcInstance) target);
			return false;
		}
	},
	FRIEND
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			for(Event e : caster.getEvents()) {
				if(!e.canUseSkill(caster, target, skill)) {
					return false;
				}
			}
			if(caster == target)
				return true;
			Player player = caster.getPlayer();
			if(player == null)
				return !target.isAutoAttackable(caster);
			Player targetPlayer = target.getPlayer();
			if(targetPlayer == null)
				return !target.isMonster() && !target.isAutoAttackable(player);
			if(player == targetPlayer)
				return true;
			for(Event e : player.getEvents())
			{
				if(e.checkForAttack(targetPlayer, player, skill, false) != null)
					return true;
				if(e.canAttack(targetPlayer, player, skill, false, false))
					return false;
			}
			ActionPermissionComponent actionPermissionComponent = caster.getActionPermissionComponent();
			if(actionPermissionComponent.anyFailure(EActionPermissionLevel.None, IAttackPermission.class, caster, target, skill)) {
				return true;
			}
			if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IAttackPermission.class, caster, target, skill)) {
				ActionPermissionComponent selfActionPermission = target.getActionPermissionComponent();
				if(selfActionPermission.anyFailure(EActionPermissionLevel.None, IIncomingAttackPermission.class, caster, target,  skill)) {
					return false;
				}
				if(selfActionPermission.anySuccess(EActionPermissionLevel.None, IIncomingAttackPermission.class, caster, target, skill)) {
					return true;
				}
				return false;
			}
			return (!caster.isInZoneBattle() || !target.isInZoneBattle()) && (player.isInSameParty(targetPlayer) || player.isInSameClan(targetPlayer) || player.isInSameAlly(targetPlayer) || !player.atMutualWarWith(targetPlayer) && targetPlayer.getPvpFlag() == 0 && !targetPlayer.isPK());
		}
	},
	FRIEND_PC
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return target.isPlayer() && FRIEND.checkObject(caster, target, skill);
		}
	},
	HIDDEN_PLACE
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return false;
		}
	},
	INVISIBLE
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return target.isInvisible(caster);
		}
	},
	NOT_FRIEND
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			if(caster.isInPeaceZone() || target.isInPeaceZone())
			{
				Player player = caster.getPlayer();
				if(player == null || !player.getPlayerAccess().PeaceAttack)
					return false;
			}
			return !FRIEND.checkObject(caster, target, skill);
		}
	},
	NOT_FRIEND_PC
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return target.isPlayer() && NOT_FRIEND.checkObject(caster, target, skill);
		}
	},
	OBJECT_DEAD_NPC_BODY
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return caster != target && target.isNpc() && target.isDead();
		}
	},
	UNDEAD_REAL_ENEMY
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			return caster != target && target.isUndead() && target.isAutoAttackable(caster);
		}
	},
	WYVERN_OBJECT
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill)
		{
			if(!target.isPlayer())
				return false;
			Player player = target.getPlayer();
			Mount mount = player.getMount();
			return mount != null && mount.isOfType(MountType.WYVERN);
		}
	};

	public abstract boolean checkObject(Creature p0, Creature p1, Skill p2);
}
