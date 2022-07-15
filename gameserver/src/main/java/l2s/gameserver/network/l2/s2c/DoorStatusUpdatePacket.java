package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public final class DoorStatusUpdatePacket implements IClientOutgoingPacket
{
	private final int _staticObjectId;
	private final int _objectId;
	private final int _isClosed;
	private final int _isEnemy;
	private final int _maxHp;
	private final int _currentHp;
	private final int _damageGrade;

	public DoorStatusUpdatePacket(DoorInstance door, Player player)
	{
		_staticObjectId = door.getDoorId();
		_objectId = door.getObjectId();
		_isClosed = door.isOpen() ? 0 : 1; //opened 0 /closed 1
		_isEnemy = door.isAutoAttackable(player) ? 1 : 0;
		_currentHp = (int) door.getCurrentHp();
		_maxHp = door.getMaxHp();
		_damageGrade = door.getDamage();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.DOOR_STATUS_UPDATE.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_isClosed);
		packetWriter.writeD(_damageGrade);
		packetWriter.writeD(_isEnemy);
		packetWriter.writeD(_staticObjectId);
		packetWriter.writeD(_currentHp);
		packetWriter.writeD(_maxHp);
		return true;
	}
}