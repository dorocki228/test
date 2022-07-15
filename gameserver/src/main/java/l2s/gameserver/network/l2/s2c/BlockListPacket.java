package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Block;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @author Bonux
 */
public class BlockListPacket implements IClientOutgoingPacket
{
	private Block[] _blockList;

	public BlockListPacket(Player player)
	{
		_blockList = player.getBlockList().values();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.BLOCK_LIST.writeId(packetWriter);
		packetWriter.writeD(_blockList.length);
		for(Block b : _blockList)
		{
			packetWriter.writeS(b.getName());
			packetWriter.writeS(b.getMemo());
		}
		return true;
	}
}