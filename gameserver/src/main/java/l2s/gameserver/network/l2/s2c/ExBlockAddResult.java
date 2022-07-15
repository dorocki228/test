package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExBlockAddResult implements IClientOutgoingPacket
{
	private final String _blockName;

	public ExBlockAddResult(String name)
	{
		_blockName = name;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BLOCK_ADD_RESULT.writeId(packetWriter);
		packetWriter.writeD(1); //UNK
		packetWriter.writeS(_blockName);

		return true;
	}
}
