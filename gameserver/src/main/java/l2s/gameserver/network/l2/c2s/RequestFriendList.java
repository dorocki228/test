package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class RequestFriendList implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		// ======<Friend List>======
		activeChar.sendPacket(SystemMsg.FRIENDS_LIST);

		for (Friend friend : activeChar.getFriendList().values()) {
			// int friendId = rset.getInt("friendId");
			final String friendName = friend.getName();

			if (friendName == null) {
				continue;
			}

			SystemMessagePacket sm;
			if (!friend.isOnline()) {
				// (Currently: Offline)
				sm = new SystemMessagePacket(SystemMsg.S1_CURRENTLY_OFFLINE);
				sm.addString(friendName);
			} else {
				// (Currently: Online)
				sm = new SystemMessagePacket(SystemMsg.S1_CURRENTLY_ONLINE);
				sm.addString(friendName);
			}

			activeChar.sendPacket(sm);
		}

		// =========================
		activeChar.sendPacket(SystemMsg.LINE_490);
	}
}