package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * Пример:
 * 08
 * a5 04 31 48 ObjectId
 * 00 00 00 7c unk
 *
 * format  d
 */
public class DeleteObjectPacket implements IClientOutgoingPacket
{
	private final Player forPlayer;
	private int _objectId;

	public DeleteObjectPacket(Player forPlayer, GameObject obj)
	{
		this.forPlayer = forPlayer;
		_objectId = obj.getObjectId();
	}

	public DeleteObjectPacket(Player forPlayer, int objId)
	{
		this.forPlayer = forPlayer;
		_objectId = objId;
	}

	@Override
	public boolean canBeWritten()
	{
		if(forPlayer == null || forPlayer.getObjectId() == _objectId)
			return false;
		return true;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.DELETE_OBJECT.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeC(0x01); // Что-то странное. Если объект сидит верхом то при 0 он сперва будет ссажен, при 1 просто пропадет.
		return true;
	}

	@Override
	public String getType()
	{
		return "[S] " + getClass().getSimpleName() + " " + GameObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
	}
}