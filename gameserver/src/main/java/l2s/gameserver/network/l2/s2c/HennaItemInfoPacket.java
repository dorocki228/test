package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.HennaTemplate;

public class HennaItemInfoPacket extends L2GameServerPacket
{
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _wit;
	private final int _men;
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
	protected final void writeImpl()
	{
		writeD(_hennaTemplate.getSymbolId());
		writeD(_hennaTemplate.getDyeId());
		writeQ(_hennaTemplate.getDrawCount());
		writeQ(_hennaTemplate.getDrawPrice());
		writeD(_available);
		writeQ(_adena);
		writeD(_int);
		writeC(_int + _hennaTemplate.getStatINT());
		writeD(_str);
		writeC(_str + _hennaTemplate.getStatSTR());
		writeD(_con);
		writeC(_con + _hennaTemplate.getStatCON());
		writeD(_men);
		writeC(_men + _hennaTemplate.getStatMEN());
		writeD(_dex);
		writeC(_dex + _hennaTemplate.getStatDEX());
		writeD(_wit);
		writeC(_wit + _hennaTemplate.getStatWIT());
		writeD(0);
		writeC(0);
		writeD(0);
		writeC(0);
		writeD(_hennaTemplate.getPeriod());
	}
}
