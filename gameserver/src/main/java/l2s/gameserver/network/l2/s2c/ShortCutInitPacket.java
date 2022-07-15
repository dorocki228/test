package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ShortCutInitPacket extends ShortCutPacket
{
	private List<ShortcutInfo> _shortCuts = Collections.emptyList();

	public ShortCutInitPacket(Player pl)
	{
		Collection<ShortCut> shortCuts = pl.getAllShortCuts();
		_shortCuts = new ArrayList<ShortcutInfo>(shortCuts.size());
		for(ShortCut shortCut : shortCuts)
			_shortCuts.add(convert(pl, shortCut));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHORT_CUT_INIT.writeId(packetWriter);
		packetWriter.writeD(_shortCuts.size());

		for(final ShortcutInfo sc : _shortCuts)
			sc.write(packetWriter, this);

		return true;
	}
}