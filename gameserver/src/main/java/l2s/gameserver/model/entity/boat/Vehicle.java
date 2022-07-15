package l2s.gameserver.model.entity.boat;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.templates.CreatureTemplate;

/**
 * @author VISTALL
 * @date  17:46/26.12.2010
 */
public class Vehicle extends Boat
{
	public Vehicle(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public IClientOutgoingPacket startPacket()
	{
		return new VehicleStartPacket(this);
	}

	@Override
	public IClientOutgoingPacket validateLocationPacket(Player player)
	{
		return new ValidateLocationInVehiclePacket(player);
	}

	@Override
	public IClientOutgoingPacket checkLocationPacket()
	{
		return new VehicleCheckLocationPacket(this);
	}

	@Override
	public IClientOutgoingPacket infoPacket()
	{
		return new VehicleInfoPacket(this);
	}

	@Override
	public IClientOutgoingPacket movePacket()
	{
		return new VehicleDeparturePacket(this);
	}

	@Override
	public IClientOutgoingPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new MoveToLocationInVehiclePacket(player, this, src, desc);
	}

	@Override
	public IClientOutgoingPacket stopMovePacket()
	{
		return new StopMovePacket(this);
	}

	@Override
	public IClientOutgoingPacket inStopMovePacket(Player player)
	{
		return new StopMoveInVehiclePacket(player);
	}

	@Override
	public IClientOutgoingPacket getOnPacket(Playable playable, Location location)
	{
		if(!playable.isPlayer())
			return null;

		return new GetOnVehiclePacket(playable.getPlayer(), this, location);
	}

	@Override
	public IClientOutgoingPacket getOffPacket(Playable playable, Location location)
	{
		if(!playable.isPlayer())
			return null;

		return new GetOffVehiclePacket(playable.getPlayer(), this, location);
	}

	@Override
	public void oustPlayers()
	{
		//
	}

	@Override
	public boolean isVehicle()
	{
		return true;
	}
}
