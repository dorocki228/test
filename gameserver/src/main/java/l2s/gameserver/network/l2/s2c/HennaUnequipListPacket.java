package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.actor.instances.player.HennaList;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.HennaTemplate;

public class HennaUnequipListPacket implements IClientOutgoingPacket
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
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.HENNA_UNEQUIP_LIST.writeId(packetWriter);
		packetWriter.writeQ(_adena);
		packetWriter.writeD(_hennaList.getFreeSize());
		packetWriter.writeD(_hennaList.size());
		for(Henna henna : _hennaList.values(true))
		{
			HennaTemplate template = henna.getTemplate();
			packetWriter.writeD(template.getSymbolId()); //symbolid
			packetWriter.writeD(template.getDyeId()); //itemid of dye
			packetWriter.writeQ(template.getRemoveCount());
			packetWriter.writeQ(template.getRemovePrice());
			packetWriter.writeD(template.isForThisClass(_player) ? 0x01 : 0x00);
		}

		return true;
	}
}