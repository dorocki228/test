package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.actor.instances.player.HennaList;

public class HennaInfoPacket extends L2GameServerPacket
{
	private final Player _player;
	private final HennaList _hennaList;

	public HennaInfoPacket(Player player)
	{
		_player = player;
		_hennaList = player.getHennaList();
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_hennaList.getINT());
		writeH(_hennaList.getSTR());
		writeH(_hennaList.getCON());
		writeH(_hennaList.getMEN());
		writeH(_hennaList.getDEX());
		writeH(_hennaList.getWIT());
		writeH(0);
		writeH(0);
		writeD(3);
		writeD(_hennaList.size());

		for(Henna henna : _hennaList.values(false))
		{
			writeD(henna.getTemplate().getSymbolId());
			writeD(_hennaList.isActive(henna));
		}

		Henna henna2 = _hennaList.getPremiumHenna();
		if(henna2 != null)
		{
			writeD(henna2.getTemplate().getSymbolId());
			writeD(henna2.getLeftTime());
			writeD(_hennaList.isActive(henna2));
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
		}
	}
}
