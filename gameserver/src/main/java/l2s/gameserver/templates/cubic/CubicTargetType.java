package l2s.gameserver.templates.cubic;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Cubic;

/**
 * @author Bonux
 */
public enum CubicTargetType
{
	BY_SKILL
	{
		@Override
		public Creature getTarget(Cubic cubic, Skill skill)
		{
			throw new UnsupportedOperationException(getClass().getName() + " not implemented BY_SKILL:getTarget(Cubic,Skill)");
		}
	},
	TARGET
	{
		@Override
		public Creature getTarget(Cubic cubic, Skill skill)
		{
			Player player = cubic.getOwner();
			if(skill.isBad() && !player.isInCombat())
				return null;

			GameObject object = player.getTarget();
			return object != null && object.isCreature() ? (Creature) object : null;
		}
	},
	HEAL
	{
		@Override
		public Creature getTarget(Cubic cubic, Skill skill)
		{
			Creature target = null;

			Player player = cubic.getOwner();
			if(player.getParty() == null)
			{
				if(!player.isCurrentHpFull() && !player.isDead())
					target = player;
			}
			else
			{
				double currentHp = Integer.MAX_VALUE;
				for(Player member : player.getParty().getPartyMembers())
				{
					if(member == null)
						continue;

					if(cubic.canCastSkill(member, skill) && !member.isCurrentHpFull() && !member.isDead() && member.getCurrentHp() < currentHp)
					{
						currentHp = member.getCurrentHp();
						target = member;
					}
				}
			}
			return target;
		}
	},
	MANA_HEAL
	{
		@Override
		public Creature getTarget(Cubic cubic, Skill skill)
		{
			Creature target = null;

			Player player = cubic.getOwner();
			if(player.getParty() == null)
			{
				if(!player.isCurrentMpFull() && !player.isDead())
					target = player;
			}
			else
			{
				double currentMp = Integer.MAX_VALUE;
				for(Player member : player.getParty().getPartyMembers())
				{
					if(member == null)
						continue;

					if(cubic.canCastSkill(member, skill) && !member.isCurrentMpFull() && !member.isDead() && member.getCurrentMp() < currentMp)
					{
						currentMp = member.getCurrentMp();
						target = member;
					}
				}
			}
			return target;
		}
	},
	MASTER
	{
		@Override
		public Creature getTarget(Cubic cubic, Skill skill)
		{
			return cubic.getOwner();
		}
	};

	public abstract Creature getTarget(Cubic cubic, Skill skill);
}