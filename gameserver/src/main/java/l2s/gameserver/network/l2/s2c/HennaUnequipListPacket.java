package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.actor.instances.player.HennaList;
import l2s.gameserver.templates.HennaTemplate;

public class HennaUnequipListPacket extends L2GameServerPacket
{
	private final Player _player;
	private final long _adena;
	private final HennaList _hennaList;

	public HennaUnequipListPacket(Player player)
	{
		_player = player;
		_adena = player.getAdena();
		_hennaList = player.getHennaList();
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_adena);
		writeD(_hennaList.getFreeSize());
		writeD(_hennaList.size());
		for(Henna henna : _hennaList.values(true))
		{
			HennaTemplate template = henna.getTemplate();
			writeD(template.getSymbolId());
			writeD(template.getDyeId());
			writeQ(template.getRemoveCount());
			writeQ(template.getRemovePrice());
			writeD(template.isForThisClass(_player) ? 1 : 0);
		}
	}
}
