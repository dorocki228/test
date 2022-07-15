package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class MoveToLocationInAirShip implements IClientIncomingPacket
{
	private int _shipId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_shipId = packet.readD();
		_targetX = packet.readD();
		_targetY = packet.readD();
		_targetZ = packet.readD();
		_originX = packet.readD();
		_originY = packet.readD();
		_originZ = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null) {
			return;
		}

		/*
		TODO
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ)) {
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _shipId));
			return;
		}

		if (activeChar.isAttackingNow() && (activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemType() == WeaponType.BOW)) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting() || activeChar.isMovementDisabled()) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!activeChar.isInAirShip()) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final L2AirShipInstance airShip = activeChar.getAirShip();
		if (airShip.getObjectId() != _shipId) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.setInVehiclePosition(new Location(_targetX, _targetY, _targetZ));
		activeChar.broadcastPacket(new ExMoveToLocationInAirShip(activeChar));*/
	}
}