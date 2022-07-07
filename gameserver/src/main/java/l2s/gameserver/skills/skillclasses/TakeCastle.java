package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.StatsSet;

import java.util.List;

public class TakeCastle extends Skill
{
	private final ResidenceSide _side;

	public TakeCastle(StatsSet set)
	{
		super(set);
		_side = set.getEnum("castle_side", ResidenceSide.class, ResidenceSide.NEUTRAL);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(activeChar == null || !activeChar.isPlayer())
			return false;
		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(player.isMounted())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		if(!player.isInRangeZ(target, 185L))
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(!target.isArtefact())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		List<CastleSiegeEvent> siegeEvents = player.getEvents(CastleSiegeEvent.class);
		if(siegeEvents.isEmpty())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		List<CastleSiegeEvent> targetEvents = target.getEvents(CastleSiegeEvent.class);
		if(targetEvents.isEmpty())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		boolean success = false;
		for(CastleSiegeEvent event : targetEvents)
		{
			if(!siegeEvents.contains(event))
				continue;
			if(event.getSiegeClan("attackers", player.getClan()) == null)
				continue;
			if(event.getResidence().getId() == 1)
			{
				List<Object> guards = event.getObjects("guards");
				loop: for(Object guard : guards)
				{
					if(guard instanceof SpawnExObject)
					{
						SpawnExObject spawn = (SpawnExObject) guard;
						for(Spawner s : spawn.getSpawns())
						{
							if(s.getMainNpcId() == 35064)
							{
								if(s.getFirstSpawned() == null || s.getFirstSpawned().isDead())
									break loop;

								activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
								return false;
							}
						}
					}
				}
			}
			if(first)
				event.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, "defenders");
			success = true;
		}
		if(!success)
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		return true;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!activeChar.isPlayer())
			return;
		if(!target.isArtefact())
			return;
		Player player = activeChar.getPlayer();
		List<CastleSiegeEvent> siegeEvents = player.getEvents(CastleSiegeEvent.class);
		if(!siegeEvents.isEmpty())
		{
			List<CastleSiegeEvent> targetEvents = target.getEvents(CastleSiegeEvent.class);
			for(CastleSiegeEvent event : targetEvents)
				if(siegeEvents.contains(event))
				{
					event.broadcastTo(new SystemMessagePacket(SystemMsg.CLAN_S1_HAS_SUCCESSFULLY_ENGRAVED_THE_HOLY_ARTIFACT).addString(player.getClan().getName()), "attackers", "defenders");
					event.takeCastle(player);
				}
		}
	}
}
