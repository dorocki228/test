package l2s.gameserver.skills.targets;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Mount;
import l2s.gameserver.model.base.MountType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;

/**
 * @author Bonux
**/
public enum AffectObject
{
	ALL
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			return true;
		}
	},
	CLAN
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
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
				return ((NpcInstance) caster).isInFaction(((NpcInstance) target));

			return false;
		}
	},
	FRIEND
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			if(caster == target)
				return true;

			final Player player = caster.getPlayer();
			if(player != null)
			{
				final Player targetPlayer = target.getPlayer();
				if(targetPlayer != null)
				{
					// Same player.
					if(player == targetPlayer)
						return true;

					// Events engine.
					for(Event e : player.getEvents())
					{
						if(e.checkForAttack(targetPlayer, player, skill, false) != null)
							return true;

						if(e.canAttack(targetPlayer, player, skill, false, false))
							return false;
					}

					// Olympiad.
					if(player.isInOlympiadMode() && targetPlayer.isInOlympiadMode() && (player.getOlympiadGame() != targetPlayer.getOlympiadGame() || player.getOlympiadSide() != targetPlayer.getOlympiadSide()))
						return false;

					// Party (command channel doesn't make you friends).
					if(player.isInSameParty(targetPlayer))
						return true;

					// Command channel
					if(player.isInSameChannel(targetPlayer))
						return true;

					// Clan.
					if(player.isInSameClan(targetPlayer))
						return true;

					// Alliance.
					if(player.isInSameAlly(targetPlayer))
						return true;

					// Arena.
					if(caster.isInZoneBattle() && target.isInZoneBattle())
						return false;

					// By default any neutral non-flagged player is considered a friend.
					return !player.atMutualWarWith(targetPlayer) && targetPlayer.getPvpFlag() == 0 && !targetPlayer.isPK();
				}

				// By default any npc that isnt mob is considered friend.
				return !target.isMonster() && !target.isAutoAttackable(player);
			}
			return !target.isAutoAttackable(caster);
		}
	},
	FRIEND_PC
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			return target.isPlayer() && FRIEND.checkObject(caster, target, skill, forceUse);
		}
	},
	HIDDEN_PLACE
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			// TODO: ??????????????????????.
			return false;
		}
	},
	INVISIBLE
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			return target.isInvisible(caster);
		}
	},
	NOT_FRIEND
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			if(caster.isInPeaceZone() || target.isInPeaceZone())
			{
				Player player = caster.getPlayer();
				Player targetPlayer = target.getPlayer();
				if (player != null && targetPlayer != null && !player.getPlayerAccess().PeaceAttack) {
					return false;
				}
			}
			return !FRIEND.checkObject(caster, target, skill, forceUse);
		}
	},
	NOT_FRIEND_PC
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			return target.isPlayer() && NOT_FRIEND.checkObject(caster, target, skill, forceUse);
		}
	},
	OBJECT_DEAD_NPC_BODY
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			if(caster == target)
				return false;
			return target.isNpc() && target.isDead();
		}
	},
	UNDEAD_REAL_ENEMY
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			if(caster == target)
				return false;
			return target.isUndead() && target.isAutoAttackable(caster);
		}
	},
	WYVERN_OBJECT
	{
		@Override
		public boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse)
		{
			if(!target.isPlayer())
				return false;

			Player player = target.getPlayer();

			Mount mount = player.getMount();
			if(mount == null)
				return false;

			return mount.isOfType(MountType.WYVERN);
		}
	};

	public abstract boolean checkObject(Creature caster, Creature target, Skill skill, boolean forceUse);
}
