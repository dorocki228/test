package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public class Recall extends Skill
{
	private final int _townId;
	private final boolean _clanhall;
	private final boolean _castle;
	private final boolean _toflag;
	private final Location _loc;

	public Recall(StatsSet set)
	{
		super(set);
		_townId = set.getInteger("townId", 0);
		_clanhall = set.getBool("clanhall", false);
		_castle = set.getBool("castle", false);
		_toflag = set.getBool("to_flag", false);
		String[] cords = set.getString("loc", "").split(";");
		if(cords.length == 3)
			_loc = new Location(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));
		else
			_loc = null;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(getHitTime() == 200)
		{
			Player player = activeChar.getPlayer();
			if(_clanhall)
			{
				if(player.getClan() == null || player.getClan().getHasHideout() == 0)
				{
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
			}
			else if(_castle && (player.getClan() == null || player.getClan().getCastle() == 0))
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}
		}
		if(activeChar.isPlayer())
		{
			Player p = (Player) activeChar;
			if(_toflag && p.bookmarkLocation == null)
				return false;
			if(p.getActiveWeaponFlagAttachment() != null)
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				return false;
			}

			if(!p.isInDuel() && p.getTeam() != TeamType.NONE)
			{
				activeChar.sendMessage(new CustomMessage("common.RecallInDuel"));
				return false;
			}

			if(p.isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_MATCH);
				return false;
			}

			if(p.getTeam() != TeamType.NONE)
			{
				activeChar.sendMessage(new CustomMessage("common.RecallInDuel"));
				return false;
			}
			for(Event e : p.getEvents())
				if(!e.canUseTeleport(p))
				{
					if(getItemConsumeId() > 0)
						activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					else
						activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
			if(activeChar.getAbnormalList().containsEffects(EffectType.TeleportBlock))
			{
				if(getItemConsumeId() > 0)
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				else
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}
		}
		if(activeChar.isInZone(Zone.ZoneType.no_escape) || _townId > 0 && activeChar.getReflection() != null && activeChar.getReflection().getCoreLoc() != null)
		{
			if(activeChar.isPlayer())
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Recall.Here"));
			return false;
		}
		return true;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!target.isPlayer())
			return;
		Player player = target.getPlayer();
		if(player == null)
			return;
		if(!player.getPlayerAccess().UseTeleport)
			return;
		if(player.isInRange(new Location(-114598, -249431, -2984), 5000L))
			return;
		if(player.getActiveWeaponFlagAttachment() != null)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}

		if(player.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD);
			return;
		}

		if(player.isInObserverMode())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return;
		}
		for(Event e : player.getEvents())
			if(!e.canUseTeleport(player))
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return;
			}
		if(!player.isInDuel() && player.getTeam() != TeamType.NONE)
		{
			activeChar.sendMessage(new CustomMessage("common.RecallInDuel"));
			return;
		}
		if(isHandler())
		{
			if(getItemConsumeId() == 7127)
			{
				player.teleToLocation(105918, 109759, -3207, ReflectionManager.MAIN);
				return;
			}
			if(getItemConsumeId() == 7130)
			{
				player.teleToLocation(85475, 16087, -3672, ReflectionManager.MAIN);
				return;
			}
			if(getItemConsumeId() == 7618)
			{
				player.teleToLocation(149864, -81062, -5618, ReflectionManager.MAIN);
				return;
			}
			if(getItemConsumeId() == 7619)
			{
				player.teleToLocation(108275, -53785, -2524, ReflectionManager.MAIN);
				return;
			}
		}
		if(_loc != null)
		{
			player.teleToLocation(_loc, ReflectionManager.MAIN);
			return;
		}
		switch(_townId)
		{
			case 1:
			{
				player.teleToLocation(-114558, 253605, -1536, ReflectionManager.MAIN);
			}
			case 2:
			{
				player.teleToLocation(45576, 49412, -2950, ReflectionManager.MAIN);
			}
			case 3:
			{
				player.teleToLocation(12501, 16768, -4500, ReflectionManager.MAIN);
			}
			case 4:
			{
				player.teleToLocation(-44884, -115063, -80, ReflectionManager.MAIN);
			}
			case 5:
			{
				player.teleToLocation(115790, -179146, -890, ReflectionManager.MAIN);
			}
			case 6:
			{
				player.teleToLocation(-14279, 124446, -3000, ReflectionManager.MAIN);
			}
			case 7:
			{
				player.teleToLocation(-82909, 150357, -3000, ReflectionManager.MAIN);
			}
			case 8:
			{
				player.teleToLocation(19025, 145245, -3107, ReflectionManager.MAIN);
			}
			case 9:
			{
				player.teleToLocation(82272, 147801, -3350, ReflectionManager.MAIN);
			}
			case 10:
			{
				player.teleToLocation(82323, 55466, -1480, ReflectionManager.MAIN);
			}
			case 11:
			{
				player.teleToLocation(144526, 24661, -2100, ReflectionManager.MAIN);
			}
			case 12:
			{
				player.teleToLocation(117189, 78952, -2210, ReflectionManager.MAIN);
			}
			case 19:
			{
				player.teleToLocation(17144, 170156, -3502, ReflectionManager.MAIN);
			}
			default:
			{
				if(_castle)
				{
					player.teleToCastle();
					return;
				}
				if(_clanhall)
				{
					player.teleToClanhall();
					return;
				}
				if(_toflag)
				{
					player.teleToLocation(player.bookmarkLocation, ReflectionManager.MAIN);
					player.bookmarkLocation = null;
					return;
				}
				player.teleToClosestTown();
			}
		}
	}
}
