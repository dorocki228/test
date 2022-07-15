package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.HennaTemplate;

public class HennaItemInfoPacket implements IClientOutgoingPacket
{
	private final int _str, _con, _dex, _int, _wit, _men;
	private final long _adena;
	private final HennaTemplate _hennaTemplate;
	private final boolean _available;

	public HennaItemInfoPacket(HennaTemplate hennaTemplate, Player player)
	{
		_hennaTemplate = hennaTemplate;
		_adena = player.getAdena();
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
		_available = _hennaTemplate.isForThisClass(player);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.HENNA_ITEM_INFO.writeId(packetWriter);
		packetWriter.writeD(_hennaTemplate.getSymbolId()); //symbol Id
		packetWriter.writeD(_hennaTemplate.getDyeId()); //item id of dye
		packetWriter.writeQ(_hennaTemplate.getDrawCount());
		packetWriter.writeQ(_hennaTemplate.getDrawPrice());
		packetWriter.writeD(_available); //able to draw or not 0 is false and 1 is true
		packetWriter.writeQ(_adena);
		packetWriter.writeD(_int); //current INT
		packetWriter.writeH(_int + _hennaTemplate.getBaseStat(BaseStats.INT)); //equip INT
		packetWriter.writeD(_str); //current STR
		packetWriter.writeH(_str + _hennaTemplate.getBaseStat(BaseStats.STR)); //equip STR
		packetWriter.writeD(_con); //current CON
		packetWriter.writeH(_con + _hennaTemplate.getBaseStat(BaseStats.CON)); //equip CON
		packetWriter.writeD(_men); //current MEM
		packetWriter.writeH(_men + _hennaTemplate.getBaseStat(BaseStats.MEN)); //equip MEM
		packetWriter.writeD(_dex); //current DEX
		packetWriter.writeH(_dex + _hennaTemplate.getBaseStat(BaseStats.DEX)); //equip DEX
		packetWriter.writeD(_wit); //current WIT
		packetWriter.writeH(_wit + _hennaTemplate.getBaseStat(BaseStats.WIT)); //equip WIT
		packetWriter.writeD(0x00); //current LUC
		packetWriter.writeH(0x00); //equip LUC
		packetWriter.writeD(0x00); //current CHA
		packetWriter.writeH(0x00); //equip CHA
		packetWriter.writeD(_hennaTemplate.getPeriod()); // UNK

		return true;
	}
}