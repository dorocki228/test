package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ShortCutRegisterPacket extends ShortCutPacket
{
	private ShortcutInfo _shortcutInfo;

	public ShortCutRegisterPacket(Player player, ShortCut sc)
	{
		_shortcutInfo = convert(player, sc);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHORT_CUT_REGISTER.writeId(packetWriter);
		_shortcutInfo.write(packetWriter, this);

		return true;
	}
}