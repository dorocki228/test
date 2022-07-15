package l2s.gameserver.network.l2.c2s;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Config;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterDeleteFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterDeleteSuccessPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;

public class CharacterDelete implements IClientIncomingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	// cd
	private int _charSlot;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_charSlot = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		int clan = clanStatus(client);
		int online = onlineStatus(client);

		if(clan > 0 || online > 0)
		{
			if(clan == 2)
				client.sendPacket(new CharacterDeleteFailPacket(CharacterDeleteFailPacket.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			else if(clan == 1)
				client.sendPacket(new CharacterDeleteFailPacket(CharacterDeleteFailPacket.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
			else if(online > 0)
				client.sendPacket(new CharacterDeleteFailPacket(CharacterDeleteFailPacket.REASON_DELETION_FAILED));

			CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(client);
			client.sendPacket(cl);
			client.setCharSelection(cl.getCharInfo());
			return;
		}

		try
		{
			if(Config.CHARACTER_DELETE_AFTER_HOURS == 0)
				client.deleteChar(_charSlot);
			else
				client.markToDeleteChar(_charSlot);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error:" );
		}

		client.sendPacket(new CharacterDeleteSuccessPacket());

		CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(client);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	private int clanStatus(GameClient client)
	{
		int obj = client.getObjectIdForSlot(_charSlot);
		if(obj == -1)
			return 0;
		if(mysql.simple_get_int("clanid", "characters", "obj_Id=" + obj) > 0)
		{
			if(mysql.simple_get_int("leader_id", "clan_subpledges", "leader_id=" + obj + " AND type = " + Clan.SUBUNIT_MAIN_CLAN) > 0)
				return 2;
			return 1;
		}
		return 0;
	}

	private int onlineStatus(GameClient client)
	{
		int obj = client.getObjectIdForSlot(_charSlot);
		if(obj == -1)
			return 0;
		if(mysql.simple_get_int("online", "characters", "obj_Id=" + obj) > 0)
			return 1;
		return 0;
	}
}