package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExBlockDefailInfo implements IClientOutgoingPacket
{
	private final String _blockName;
	private final String _blockMemo;

	public ExBlockDefailInfo(String name, String memo)
	{
		_blockName = name;
		_blockMemo = memo;

	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BLOCK_DETAIL_INFO.writeId(packetWriter);
		packetWriter.writeS(_blockName);
		packetWriter.writeS(_blockMemo);

		return true;
	}
}
