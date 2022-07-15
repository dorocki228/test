package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExBR_PremiumStatePacket implements IClientOutgoingPacket
{
	private int _objectId;
	private int _state;

	public ExBR_PremiumStatePacket(Player activeChar, boolean state)
	{
		_objectId = activeChar.getObjectId();
		_state = state ? 1 : 0;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BR_NOTIFY_PREMIUM_STATE.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeC(_state);

		return true;
	}
}
