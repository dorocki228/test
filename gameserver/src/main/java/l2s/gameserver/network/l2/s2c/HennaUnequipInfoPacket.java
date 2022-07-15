package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.HennaTemplate;

public class HennaUnequipInfoPacket implements IClientOutgoingPacket
{
	private final HennaTemplate _hennaTemplate;
	private final Player _player;

	public HennaUnequipInfoPacket(HennaTemplate hennaTemplate, Player player)
	{
		_hennaTemplate = hennaTemplate;
		_player = player;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.HENNA_UNEQUIP_INFO.writeId(packetWriter);
		packetWriter.writeD(_hennaTemplate.getSymbolId()); //symbol Id
		packetWriter.writeD(_hennaTemplate.getDyeId()); //item id of dye
		packetWriter.writeQ(_hennaTemplate.getRemoveCount());
		packetWriter.writeQ(_hennaTemplate.getRemovePrice());
		packetWriter.writeD(_hennaTemplate.isForThisClass(_player)); //able to draw or not 0 is false and 1 is true
		packetWriter.writeQ(_player.getAdena());
		packetWriter.writeD(_player.getINT()); //current INT
		packetWriter.writeC(_player.getINT() - _hennaTemplate.getBaseStat(BaseStats.INT)); //equip INT
		packetWriter.writeD(_player.getSTR()); //current STR
		packetWriter.writeC(_player.getSTR() - _hennaTemplate.getBaseStat(BaseStats.STR)); //equip STR
		packetWriter.writeD(_player.getCON()); //current CON
		packetWriter.writeC(_player.getCON() - _hennaTemplate.getBaseStat(BaseStats.CON)); //equip CON
		packetWriter.writeD(_player.getMEN()); //current MEM
		packetWriter.writeC(_player.getMEN() - _hennaTemplate.getBaseStat(BaseStats.MEN)); //equip MEM
		packetWriter.writeD(_player.getDEX()); //current DEX
		packetWriter.writeC(_player.getDEX() - _hennaTemplate.getBaseStat(BaseStats.DEX)); //equip DEX
		packetWriter.writeD(_player.getWIT()); //current WIT
		packetWriter.writeC(_player.getWIT() - _hennaTemplate.getBaseStat(BaseStats.WIT)); //equip WIT
		packetWriter.writeD(0x00); //current LUC
		packetWriter.writeC(0x00); //equip LUC
		packetWriter.writeD(0x00); //current CHA
		packetWriter.writeC(0x00); //equip CHA
		packetWriter.writeD(_hennaTemplate.getPeriod()); // UNK

		return true;
	}
}