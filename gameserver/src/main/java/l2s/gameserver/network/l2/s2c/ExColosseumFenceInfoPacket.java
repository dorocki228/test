package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Fence;

public class ExColosseumFenceInfoPacket extends L2GameServerPacket
{
    private final Fence fence;

	public ExColosseumFenceInfoPacket(Fence fence)
	{
		this.fence = fence;
	}

	@Override
	protected void writeImpl()
	{
		writeD(fence.getObjectId());
		writeD(fence.getType());
		writeD(fence.getX());
		writeD(fence.getY());
		writeD(fence.getZ());
		writeD(fence.getWidth());
		writeD(fence.getHeight());
	}
}
