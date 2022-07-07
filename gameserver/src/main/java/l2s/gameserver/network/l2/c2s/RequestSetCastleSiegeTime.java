package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfoPacket;

public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
	private int _id;
	private int _time;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_time = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _id);
		if(castle == null)
			return;
		if(player.getClan().getCastle() != castle.getId())
			return;
		if((player.getClanPrivileges() & 0x40000) != 0x40000)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
			return;
		}
		player.sendPacket(new CastleSiegeInfoPacket(castle, player));
	}
}
