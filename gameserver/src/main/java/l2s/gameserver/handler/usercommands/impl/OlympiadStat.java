package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 109 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		GameObject objectTarget = Config.OLYMPIAD_OLDSTYLE_STAT ? activeChar : activeChar.getTarget();

		if(objectTarget == null)
			objectTarget = activeChar;

		if(!objectTarget.isPlayer() || objectTarget.getPlayer().getClassLevel().ordinal() < 2)
		{
			activeChar.sendPacket(SystemMsg.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
			return true;
		}

		Player playerTarget = objectTarget.getPlayer();
		SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.FOR_THE_CURRENT_GRAND_OLYMPIAD_YOU_HAVE_PARTICIPATED_IN_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_CURRENTLY_HAVE_S4_OLYMPIAD_POINTS);
		sm.addNumber(Olympiad.getCompetitionDone(playerTarget.getObjectId()));
		sm.addNumber(Olympiad.getCompetitionWin(playerTarget.getObjectId()));
		sm.addNumber(Olympiad.getCompetitionLoose(playerTarget.getObjectId()));
		sm.addNumber(Olympiad.getParticipantPoints(playerTarget.getObjectId()));
		activeChar.sendPacket(sm);

		sm = new SystemMessagePacket(SystemMsg.YOU_HAVE_S1_MATCHES_REMAINING_THAT_YOU_CAN_PARTICIPATE_IN_THIS_WEEK);
		sm.addNumber(Olympiad.getWeekGameCounts(playerTarget.getObjectId())[0]);
		activeChar.sendPacket(sm);

		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
