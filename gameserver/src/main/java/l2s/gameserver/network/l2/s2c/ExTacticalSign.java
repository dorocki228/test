package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExTacticalSign implements IClientOutgoingPacket
{
	public static final int STAR = 1;
	public static final int HEART = 2;
	public static final int MOON = 3;
	public static final int CROSS = 4;

	private int _targetId;
	private int _signId;

	public ExTacticalSign(int target, int sign)
	{
		_targetId = target;
		_signId = sign;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_TACTICAL_SIGN.writeId(packetWriter);
		packetWriter.writeD(_targetId);
		packetWriter.writeD(_signId);

		return true;
	}
}
