package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExOneDayRewardInfo implements IClientOutgoingPacket
{
	private final int availableDailyMissionCount;

	public ExOneDayRewardInfo(int availableDailyMissionCount) {
		this.availableDailyMissionCount = availableDailyMissionCount;
	}

	public ExOneDayRewardInfo(Player player) {
		this.availableDailyMissionCount = player.getDailyMissionList().getAvailableMissions().size();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ONE_DAY_REWARD_INFO.writeId(packetWriter);
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(availableDailyMissionCount);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK
		packetWriter.writeD(0x00);	// TODO[UNDERGROUND]: UNK

		return true;
	}
}