package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.FenceInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author HoridoJoho / FBIagent
 * @reworked by Bonux
 */
public class ExColosseumFenceInfoPacket implements IClientOutgoingPacket
{
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _width;
	private final int _length;
	private final int _state;

	public ExColosseumFenceInfoPacket(int objId, int x, int y, int z, int width, int length, int state)
	{
		_objId = objId;
		_x = x;
		_y = y;
		_z = z;
		_width = width;
		_length = length;
		_state = state;
	}

	public ExColosseumFenceInfoPacket(FenceInstance fence)
	{
		this(fence.getObjectId(), fence.getX(), fence.getY(), fence.getZ(), fence.getWidth(), fence.getLength(), fence.getState().getClientId());
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_COLOSSEUM_FENCE_INFO.writeId(packetWriter);
		packetWriter.writeD(_objId);
		packetWriter.writeD(_state);
		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);
		packetWriter.writeD(_width);
		packetWriter.writeD(_length);

		return true;
	}
}