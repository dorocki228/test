package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;
import org.napile.primitive.maps.IntObjectMap;

/**
 * @author VISTALL
 * @date 22:01/22.03.2011
 */
public class ExShowPostFriend implements IClientOutgoingPacket
{
	private IntObjectMap<String> _list;

	public ExShowPostFriend(Player player)
	{
		_list = player.getPostFriends();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_POST_FRIEND.writeId(packetWriter);
		packetWriter.writeD(_list.size());
		for(String t : _list.valueCollection())
			packetWriter.writeS(t);

		return true;
	}
}
