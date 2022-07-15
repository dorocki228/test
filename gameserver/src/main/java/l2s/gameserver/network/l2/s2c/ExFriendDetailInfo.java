package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExFriendDetailInfo implements IClientOutgoingPacket
{
	private final int _objectId;
	private final Friend _friend;
	private final int _clanCrestId;
	private final int _allyCrestId;

	public ExFriendDetailInfo(Player player, Friend friend)
	{
		_objectId = player.getObjectId();
		_friend = friend;
		_clanCrestId = _friend.getClanId() > 0 ? CrestCache.getInstance().getPledgeCrestId(_friend.getClanId()) : 0;
		_allyCrestId = _friend.getAllyId() > 0 ? CrestCache.getInstance().getAllyCrestId(_friend.getAllyId()) : 0;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FRIEND_DETAIL_INFO.writeId(packetWriter);
		packetWriter.writeD(_objectId); // Character ID
		packetWriter.writeS(_friend.getName()); // Name
		packetWriter.writeD(_friend.isOnline()); // Online
		packetWriter.writeD(_friend.isOnline() ? _friend.getObjectId() : 0); // Friend OID
		packetWriter.writeH(_friend.getLevel()); // Level
		packetWriter.writeH(_friend.getClassId()); // Class
		packetWriter.writeD(_friend.getClanId()); // Pledge ID
		packetWriter.writeD(_clanCrestId); // Pledge crest ID
		packetWriter.writeS(_friend.getClanName()); // Pledge name
		packetWriter.writeD(_friend.getAllyId()); // Alliance ID
		packetWriter.writeD(_allyCrestId); // Alliance crest ID
		packetWriter.writeS(_friend.getAllyName()); // Alliance name
		packetWriter.writeC(_friend.getCreationMonth() + 1); // Creation month
		packetWriter.writeC(_friend.getCreationDay()); // Creation day
		packetWriter.writeD(_friend.getLastAccessDelay()); // Last login
		packetWriter.writeS(_friend.getMemo()); // Memo

		return true;
	}
}
