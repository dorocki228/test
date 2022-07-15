package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExWorldChatCnt implements IClientOutgoingPacket
{
	private final int _count;

	public ExWorldChatCnt(Player player)
	{
		_count = player.getWorldChatPoints();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_WORLDCHAT_CNT.writeId(packetWriter);
		packetWriter.writeD(_count);

		return true;
	}
}
