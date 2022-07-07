package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ShortCutInitPacket extends ShortCutPacket
{
	private List<ShortcutInfo> _shortCuts;

	public ShortCutInitPacket(Player pl)
	{
		_shortCuts = Collections.emptyList();
		Collection<ShortCut> shortCuts = pl.getAllShortCuts();
		_shortCuts = new ArrayList<>(shortCuts.size());
		for(ShortCut shortCut : shortCuts)
			_shortCuts.add(ShortCutPacket.convert(pl, shortCut));
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_shortCuts.size());
		for(ShortcutInfo sc : _shortCuts)
			sc.write(this);
	}
}
