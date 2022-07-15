package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExVoteSystemInfo implements IClientOutgoingPacket
{
	private int _receivedRec, _givingRec, _time, _bonusPercent;
	private boolean _showTimer;

	public ExVoteSystemInfo(Player player)
	{
		_receivedRec = player.getRecomLeft();
		_givingRec = player.getRecomHave();
		_time = 0;
		_bonusPercent = 0;
		_showTimer = false;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VOTE_SYSTEM_INFO.writeId(packetWriter);
		packetWriter.writeD(_receivedRec); //полученые реки
		packetWriter.writeD(_givingRec); //отданые реки
		packetWriter.writeD(_time); //таймер скок секунд осталось
		packetWriter.writeD(_bonusPercent); // процент бонуса
		packetWriter.writeD(_showTimer ? 0x01 : 0x00); //если ноль то таймера нету 1 - пишет чтоли "Работает"

		return true;
	}
}