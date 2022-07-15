package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 23:13/21.03.2011
 */
public class ExAddPostFriend implements IClientOutgoingPacket
{
	public static int NAME_IS_NOT_EXISTS = 0;
	public static int SUCCESS = 1;
	public static int PREVIOS_NAME_IS_BEEN_REGISTERED = -1; // The previous name is being registered. Please try again later.
	public static int NAME_IS_NOT_EXISTS2 = -2;
	public static int LIST_IS_FULL = -3;
	public static int ALREADY_ADDED = -4;
	public static int NAME_IS_NOT_REGISTERED = -4;

	private String _name;
	private int _result;

	public ExAddPostFriend(String name, int s)
	{
		_name = name;
		_result = s;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ADD_POST_FRIEND.writeId(packetWriter);
		packetWriter.writeS(_name);
		packetWriter.writeD(_result);

		return true;
	}
}
