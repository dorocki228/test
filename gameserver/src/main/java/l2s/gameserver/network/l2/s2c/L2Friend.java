package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

public class L2Friend implements IClientOutgoingPacket
{
	private boolean _add, _online;
	private String _name;
	private int _object_id;

	public L2Friend(Player player, boolean add)
	{
		_add = add;
		_name = player.getName();
		_object_id = player.getObjectId();
		_online = true;
	}

	public L2Friend(String name, boolean add, boolean online, int object_id)
	{
		_name = name;
		_add = add;
		_object_id = object_id;
		_online = online;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.L2_FRIEND.writeId(packetWriter);
		packetWriter.writeD(_add ? 1 : 3); // 1 - добавить друга в спикок, 3 удалить друга со списка
		packetWriter.writeD(0); //и снова тут идет ID персонажа в списке оффа, не object id
		packetWriter.writeS(_name);
		packetWriter.writeD(_online ? 1 : 0); // онлайн или оффлайн
		packetWriter.writeD(_object_id); //object_id if online

		return true;
	}
}