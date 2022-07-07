package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.LocationBalancerService;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public class Call extends Skill
{
	private final boolean _force;

	public Call(StatsSet set)
	{
		super(set);
		_force = set.getBool("force_call", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(activeChar.isPlayer())
		{
			IBroadcastPacket msg = canSummonHere((Player) activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}
			if(oneTarget())
			{
				if(activeChar == target)
					return false;
				msg = canBeSummoned(activeChar.getPlayer(), target);
				if(msg != null)
				{
					activeChar.sendPacket(msg);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(activeChar == target)
			return;
		if(!activeChar.isPlayer())
			return;
		if(!target.isPlayer())
			return;
		Player player = target.getPlayer();
		if(canBeSummoned(activeChar, player) != null)
			return;
		if(_force)
			player.teleToLocation(Location.findPointToStay(activeChar, 100, 150), ReflectionManager.MAIN);
		else
			player.summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), getId() == 1403 || getId() == 1404 ? 1 : 0);
	}

	public static IBroadcastPacket canSummonHere(Player activeChar)
	{
		if(activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.isInObserverMode() || activeChar.isFlying())
			return SystemMsg.NOTHING_HAPPENED;
		if(activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.SIEGE) || activeChar.isInZone(Zone.ZoneType.no_restart) || activeChar.isInZone(Zone.ZoneType.no_summon) || activeChar.isInBoat() || !activeChar.getReflection().isMain())
			return SystemMsg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
		if(activeChar.isInStoreMode() || activeChar.isPrivateBuffer() || activeChar.isProcessingRequest())
			return SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE;
		if(!LocationBalancerService.getInstance().canTeleport(activeChar.getPlayer(), activeChar.getLoc())) {
			return SystemMsg.INVALID_TARGET;
		}
		return null;
	}

	public static IBroadcastPacket canBeSummoned(Creature actor, Creature target)
	{
		if(target == null || !target.isPlayer() || target.isFlying() || target.isOutOfControl() || !target.getPlayer().getPlayerAccess().UseTeleport)
			return SystemMsg.INVALID_TARGET;
		if(target.isInRange(new Location(-114598, -249431, -2984), 5000L))
			return SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
		if(target.getPlayer().isInOlympiadMode())
			return SystemMsg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;
		if(target.isInZoneBattle() || target.isInZone(Zone.ZoneType.SIEGE) || target.isInZone(Zone.ZoneType.no_restart) || target.isInZone(Zone.ZoneType.no_summon) || !target.getReflection().isMain() || target.isInBoat())
			return SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
		if(target.isAlikeDead())
			return new SystemMessagePacket(SystemMsg.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addName(target);
		if(target.getPvpFlag() != 0 || target.isInCombat())
			return new SystemMessagePacket(SystemMsg.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addName(target);
		Player pTarget = (Player) target;
		if(pTarget.getPrivateStoreType() != 0 || pTarget.isProcessingRequest() || pTarget.isPrivateBuffer())
			return new SystemMessagePacket(SystemMsg.C1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addName(target);
		if(!LocationBalancerService.getInstance().canTeleport(actor.getPlayer(), actor.getLoc())) {
			return SystemMsg.INVALID_TARGET;
		}
		return null;
	}
}
