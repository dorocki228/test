package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.utils.PledgeBonusUtils;

/**
 * @author Bonux
**/
public class ExPledgeBonusUIOpen implements IClientOutgoingPacket
{
	private int _attendanceProgress = 0;
	private int _huntingProgress = 0;
	private int _yesterdayAttendanceReward = 0;
	private int _yesterdayHuntingReward = 0;
	private int _yesterdayAttendanceRewardId = 0;
	private int _yesterdayHuntingRewardId = 0;
	private boolean _attendanceRewardReceivable = false;
	private boolean _huntingRewardReceivable = false;

	public ExPledgeBonusUIOpen(Player player)
	{
		if(!Config.EX_USE_PLEDGE_BONUS)
			return;

		Clan clan = player.getClan();
		if(clan == null)
			return;

		_attendanceProgress = clan.getAttendanceProgress();
		_huntingProgress = clan.getHuntingProgress();
		_yesterdayAttendanceReward = clan.getYesterdayAttendanceReward();
		_yesterdayHuntingReward = clan.getYesterdayHuntingReward();
		_yesterdayAttendanceRewardId = PledgeBonusUtils.ATTENDANCE_REWARDS.get(_yesterdayAttendanceReward);
		_yesterdayHuntingRewardId = PledgeBonusUtils.HUNTING_REWARDS.get(_yesterdayHuntingReward);
		_attendanceRewardReceivable = _yesterdayAttendanceRewardId > 0 && PledgeBonusUtils.isAttendanceRewardAvailable(player);
		_huntingRewardReceivable = _yesterdayHuntingRewardId > 0 && PledgeBonusUtils.isHuntingRewardAvailable(player);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_BONUUI_OPEN.writeId(packetWriter);
		//ddcdccddcdcc
		packetWriter.writeD(PledgeBonusUtils.MAX_ATTENDANCE_PROGRESS);    // Attendance requirement (max)
		packetWriter.writeD(_attendanceProgress);    // Current amount
		packetWriter.writeC(0x00);	// Reward Type (0 - Skill, 1 - Item)
		packetWriter.writeD(_yesterdayAttendanceRewardId);    // Yesterday's reward
		packetWriter.writeC(_yesterdayAttendanceReward);    // Reward level
		packetWriter.writeC(_attendanceRewardReceivable);    // Receivable
		packetWriter.writeD(PledgeBonusUtils.MAX_HUNTING_PROGRESS);    // Hunting requirement (max)
		packetWriter.writeD(_huntingProgress);    // Current amount
		packetWriter.writeC(0x01);	// Reward Type (0 - Skill, 1 - Item)
		packetWriter.writeD(_yesterdayHuntingRewardId);    // Yesterday's reward
		packetWriter.writeC(_yesterdayHuntingReward);    // Reward level
		packetWriter.writeC(_huntingRewardReceivable);    // Receivable

		return true;
	}
}