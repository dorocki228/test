package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

public class FinishRotating implements IClientOutgoingPacket
{
	private int _charId, _degree, _speed;

	public FinishRotating(Creature player, int degree, int speed)
	{
		_charId = player.getObjectId();
		_degree = degree;
		_speed = speed;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FINISH_ROTATING.writeId(packetWriter);
		packetWriter.writeD(_charId);
		packetWriter.writeD(_degree);
		packetWriter.writeD(_speed);
		packetWriter.writeD(0x00); //??

		return true;
	}
}