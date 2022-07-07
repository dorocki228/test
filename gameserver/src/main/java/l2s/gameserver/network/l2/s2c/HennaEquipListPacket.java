package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.HennaHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.HennaTemplate;

import java.util.ArrayList;
import java.util.List;

public class HennaEquipListPacket extends L2GameServerPacket
{
	private final Player _player;
	private final int _emptySlots;
	private final long _adena;
	private final List<HennaTemplate> _hennas;

	public HennaEquipListPacket(Player player)
	{
		_hennas = new ArrayList<>();
		_player = player;
		_adena = player.getAdena();
		_emptySlots = player.getHennaList().getFreeSize();
		List<HennaTemplate> list = HennaHolder.getInstance().generateList(player);
		for(HennaTemplate element : list)
			if(player.getInventory().getItemByItemId(element.getDyeId()) != null)
				_hennas.add(element);
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_adena);
		writeD(_emptySlots);
		writeD(_hennas.size());
		for(HennaTemplate henna : _hennas)
		{
			writeD(henna.getSymbolId());
			writeD(henna.getDyeId());
			writeQ(henna.getDrawCount());
			writeQ(henna.getDrawPrice());
			writeD(henna.isForThisClass(_player) ? 1 : 0);
		}
	}
}
