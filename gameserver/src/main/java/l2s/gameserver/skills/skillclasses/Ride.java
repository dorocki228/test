package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.StatsSet;

public class Ride extends Skill
{
	public Ride(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		if(!activeChar.isPlayer())
			return false;
		Player player = (Player) activeChar;
		if(getNpcId() != 0)
		{
			if(player.isInOlympiadMode())
			{
				player.sendPacket(SystemMsg.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_MATCH);
				return false;
			}

			if(player.isFishing() || player.isInDuel() || player.isSitting() || player.isInCombat() || player.isTransformed() || player.getPet() != null || player.isMounted() || player.isInBoat())
			{
				player.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				return false;
			}
		}
		else if(getNpcId() == 0 && !player.isMounted())
			return false;
		return true;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!activeChar.isPlayer())
			return;
		Player player = activeChar.getPlayer();
		player.setMount(0, getNpcId(), player.getLevel(), -1);
	}
}
