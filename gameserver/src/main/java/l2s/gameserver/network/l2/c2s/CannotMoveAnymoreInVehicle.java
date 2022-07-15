package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;

public class CannotMoveAnymoreInVehicle implements IClientIncomingPacket
{
	private int _boatId = -1;
	private final Location _loc = new Location();

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_boatId = packet.readD();
		_loc.x = packet.readD();
		_loc.y = packet.readD();
		_loc.z = packet.readD();
		_loc.h = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		l2s.gameserver.model.entity.boat.Boat boat = activeChar.getBoat();
		if(boat != null && boat.getBoatId() == _boatId)
		{
			activeChar.setInBoatPosition(_loc);
			activeChar.setHeading(_loc.h);
			activeChar.broadcastPacket(boat.inStopMovePacket(activeChar));
		}
	}
}