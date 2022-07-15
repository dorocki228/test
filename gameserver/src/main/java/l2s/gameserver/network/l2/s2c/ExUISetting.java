package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExUISetting implements IClientOutgoingPacket
{
	private final byte data[];

	public ExUISetting(Player player)
	{
		data = player.getKeyBindings();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_UI_SETTING.writeId(packetWriter);
		packetWriter.writeD(data.length);
		packetWriter.writeB(data);

		return true;
	}
}
