package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * 0000: 3e 2a 89 00 4c 01 00 00 00                         .|...
 *
 * format   dd
 */
public class ChangeMoveTypePacket implements IClientOutgoingPacket
{
	public static int WALK = 0;
	public static int RUN = 1;

	private int _chaId;
	private boolean _running;

	public ChangeMoveTypePacket(Creature cha)
	{
		_chaId = cha.getObjectId();
		_running = cha.isRunning();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHANGE_MOVE_TYPE.writeId(packetWriter);
		packetWriter.writeD(_chaId);
		packetWriter.writeD(_running ? 1 : 0);
		packetWriter.writeD(0); //c2
		return true;
	}
}