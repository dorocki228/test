package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.CharacterPostFriendDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExAddPostFriend;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import org.napile.primitive.maps.IntObjectMap;

/**
 * @author VISTALL
 * @date 21:06/22.03.2011
 */
public class RequestExAddContactToContactList implements IClientIncomingPacket
{
	private String _name;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_name = packet.readS(Config.CNAME_MAXLEN);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client) throws Exception
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		int targetObjectId = CharacterDAO.getInstance().getObjectIdByName(_name);
		if(targetObjectId == 0)
		{
			player.sendPacket(new ExAddPostFriend(_name, ExAddPostFriend.NAME_IS_NOT_EXISTS));
			return;
		}

		if(_name.equalsIgnoreCase(player.getName()))
		{
			player.sendPacket(new ExAddPostFriend(_name, ExAddPostFriend.NAME_IS_NOT_REGISTERED));
			return;
		}

		IntObjectMap<String> postFriend = player.getPostFriends();
		if(postFriend.size() >= Player.MAX_POST_FRIEND_SIZE)
		{
			player.sendPacket(new ExAddPostFriend(_name, ExAddPostFriend.LIST_IS_FULL));
			return;
		}

		if(postFriend.containsKey(targetObjectId))
		{
			player.sendPacket(new ExAddPostFriend(_name, ExAddPostFriend.ALREADY_ADDED));
			return;
		}

		CharacterPostFriendDAO.getInstance().insert(player, targetObjectId);
		postFriend.put(targetObjectId, CharacterDAO.getInstance().getNameByObjectId(targetObjectId));

		player.sendPacket(new SystemMessagePacket(SystemMsg.S1_WAS_SUCCESSFULLY_ADDED_TO_YOUR_CONTACT_LIST).addString(_name), new ExAddPostFriend(_name, ExAddPostFriend.SUCCESS));
	}
}
