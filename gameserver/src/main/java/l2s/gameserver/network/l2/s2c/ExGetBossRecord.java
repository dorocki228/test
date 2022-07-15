package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.List;

/**
 * Format: ch ddd [ddd]
 */
public class ExGetBossRecord implements IClientOutgoingPacket
{
	private List<BossRecordInfo> _bossRecordInfo;
	private int _ranking;
	private int _totalPoints;

	public ExGetBossRecord(int ranking, int totalScore, List<BossRecordInfo> bossRecordInfo)
	{
		_ranking = ranking; // char ranking
		_totalPoints = totalScore; // char total points
		_bossRecordInfo = bossRecordInfo;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_GET_BOSRECORD.writeId(packetWriter);
		packetWriter.writeD(_ranking); // char ranking
		packetWriter.writeD(_totalPoints); // char total points

		packetWriter.writeD(_bossRecordInfo.size()); // list size
		for(BossRecordInfo w : _bossRecordInfo)
		{
			packetWriter.writeD(w._bossId);
			packetWriter.writeD(w._points);
			packetWriter.writeD(w._unk1);// don`t know
		}

		return true;
	}

	public static class BossRecordInfo
	{
		public int _bossId;
		public int _points;
		public int _unk1;

		public BossRecordInfo(int bossId, int points, int unk1)
		{
			_bossId = bossId;
			_points = points;
			_unk1 = unk1;
		}
	}
}