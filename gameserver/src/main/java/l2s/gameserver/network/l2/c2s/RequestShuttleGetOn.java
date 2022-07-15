package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.boat.Boat;

/**
 * @author Bonux
 */
public class RequestShuttleGetOn implements IClientIncomingPacket
{
	private int _shuttleId;
	private Location _loc = new Location();

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_shuttleId = packet.readD();
		_loc.x = packet.readD();
		_loc.y = packet.readD();
		_loc.z = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		Boat boat = BoatHolder.getInstance().getBoat(_shuttleId);
		if(boat == null)
			return;

		boat.addPlayer(player, _loc);
	}
}