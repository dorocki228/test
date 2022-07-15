package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

/**
 * @author Bonux
 */
public class ExOneDayRewardList implements IClientOutgoingPacket
{
	private static final SchedulingPattern DAILY_REUSE_PATTERN = new SchedulingPattern("30 6 * * *");
	private static final SchedulingPattern WEEKLY_REUSE_PATTERN = new SchedulingPattern("30 6 * * 1");
	private static final SchedulingPattern MONTHLY_REUSE_PATTERN = new SchedulingPattern("30 6 1 * *");

	private final int _dayRemainTime;
	private final int _weekRemainTime;
	private final int _monthRemainTime;
	private final int _classId;
	private final int _dayOfWeek;
	private final List<DailyMission> _missions = new ArrayList<DailyMission>();
	
	public ExOneDayRewardList(Player player)
	{
		_dayRemainTime = (int) ((DAILY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_weekRemainTime = (int) ((WEEKLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_monthRemainTime = (int) ((MONTHLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_classId = player.getBaseClassId();
		_dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		for(DailyMissionTemplate missionTemplate : player.getDailyMissionList().getAvailableMissions())
		{
			DailyMission mission = player.getDailyMissionList().get(missionTemplate);
			if(!mission.isFinallyCompleted())
				_missions.add(mission);
		}

		Collections.sort(_missions);
	}

	public ExOneDayRewardList()
	{
		_dayRemainTime = (int) ((DAILY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_weekRemainTime = (int) ((WEEKLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_monthRemainTime = (int) ((MONTHLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_classId = 0;
		_dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ONE_DAY_REWARD_LIST.writeId(packetWriter);
		packetWriter.writeD(_dayRemainTime); // DayRemainTime
		packetWriter.writeD(_weekRemainTime); // WeekRemainTime
		packetWriter.writeD(_monthRemainTime); // MonthRemainTime
		packetWriter.writeC(20); // Inzone (ID or Count?!?)
		packetWriter.writeD(_classId);
		packetWriter.writeD(_dayOfWeek);
		packetWriter.writeD(_missions.size());
		for(DailyMission mission : _missions)
		{
			packetWriter.writeH(mission.getId()); // Reward
			packetWriter.writeC(mission.getStatus().ordinal()); // 1 Available, 2 Not Available, 3 Complete
			packetWriter.writeC(0x01); // Requires multiple completion - YesOrNo (Deprecated)
			packetWriter.writeD(mission.getCurrentProgress()); // Current progress
			packetWriter.writeD(mission.getRequiredProgress()); // Required total
		}

		return true;
	}
}
