package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.HennaTemplate;

public class HennaUnequipInfoPacket extends L2GameServerPacket
{
	private final HennaTemplate _hennaTemplate;
	private final Player _player;

	public HennaUnequipInfoPacket(HennaTemplate hennaTemplate, Player player)
	{
		_hennaTemplate = hennaTemplate;
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_hennaTemplate.getSymbolId());
		writeD(_hennaTemplate.getDyeId());
		writeQ(_hennaTemplate.getRemoveCount());
		writeQ(_hennaTemplate.getRemovePrice());
		writeD(_hennaTemplate.isForThisClass(_player));
		writeQ(_player.getAdena());
		writeD(_player.getINT());
		writeC(_player.getINT() - _hennaTemplate.getStatINT());
		writeD(_player.getSTR());
		writeC(_player.getSTR() - _hennaTemplate.getStatSTR());
		writeD(_player.getCON());
		writeC(_player.getCON() - _hennaTemplate.getStatCON());
		writeD(_player.getMEN());
		writeC(_player.getMEN() - _hennaTemplate.getStatMEN());
		writeD(_player.getDEX());
		writeC(_player.getDEX() - _hennaTemplate.getStatDEX());
		writeD(_player.getWIT());
		writeC(_player.getWIT() - _hennaTemplate.getStatWIT());
		writeD(0);
		writeC(0);
		writeD(0);
		writeC(0);
		writeD(_hennaTemplate.getPeriod());
	}
}
