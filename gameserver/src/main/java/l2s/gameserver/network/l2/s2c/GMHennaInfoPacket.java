package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.actor.instances.player.HennaList;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.network.l2.OutgoingPackets;

public class GMHennaInfoPacket implements IClientOutgoingPacket
{
	private final Player _player;
	private final HennaList _hennaList;

	public GMHennaInfoPacket(Player player)
	{
		_player = player;
		_hennaList = player.getHennaList();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GMHENNA_INFO.writeId(packetWriter);
		packetWriter.writeH(_hennaList.getValue(BaseStats.INT)); //equip INT
		packetWriter.writeH(_hennaList.getValue(BaseStats.STR)); //equip STR
		packetWriter.writeH(_hennaList.getValue(BaseStats.CON)); //equip CON
		packetWriter.writeH(_hennaList.getValue(BaseStats.MEN)); //equip MEN
		packetWriter.writeH(_hennaList.getValue(BaseStats.DEX)); //equip DEX
		packetWriter.writeH(_hennaList.getValue(BaseStats.WIT)); //equip WIT
		packetWriter.writeH(0x00); // LUC
		packetWriter.writeH(0x00); // CHA
		packetWriter.writeD(HennaList.MAX_SIZE); //interlude, slots?
		packetWriter.writeD(_hennaList.size());
		for(Henna henna : _hennaList.values(false))
		{
			packetWriter.writeD(henna.getTemplate().getSymbolId());
			packetWriter.writeD(_hennaList.isActive(henna));
		}

		Henna henna = _hennaList.getPremiumHenna();
		if(henna != null)
		{
			packetWriter.writeD(henna.getTemplate().getSymbolId());	// Premium symbol ID
			packetWriter.writeD(_hennaList.isActive(henna));	// Premium symbol active
			packetWriter.writeD(henna.getLeftTime());	// Premium symbol left time
		}
		else
		{
			packetWriter.writeD(0x00);	// Premium symbol ID
			packetWriter.writeD(0x00);	// Premium symbol active
			packetWriter.writeD(0x00);	// Premium symbol left time
		}

		return true;
	}
}