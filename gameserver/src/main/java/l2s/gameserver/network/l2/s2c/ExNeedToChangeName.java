package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExNeedToChangeName implements IClientOutgoingPacket
{
	public static final int TYPE_PLAYER = 0;
	public static final int TYPE_PLEDGE = 1;

	public static final int NONE_REASON = 0;
	public static final int NAME_ALREADY_IN_USE_OR_INCORRECT_REASON = 1;

	private int _type, _reason;
	private String _origName;

	public ExNeedToChangeName(int type, int reason, String origName)
	{
		_type = type;
		_reason = reason;
		_origName = origName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_NEED_TO_CHANGE_NAME.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeD(_reason);
		packetWriter.writeS(_origName);

		return true;
	}
}