package l2s.gameserver.model.entity.boat;

import l2s.gameserver.ai.BoatAI;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.*;
import l2s.gameserver.model.entity.events.impl.BoatWayEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.IClientOutgoingPacket;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @date  17:45/26.12.2010
 */
public abstract class Boat extends Creature
{
	private int _moveSpeed; //speed 1
	private int _rotationSpeed; //speed 2

	protected int _fromHome;
	protected int _runState;

	private final BoatWayEvent[] _ways = new BoatWayEvent[2];
	protected final Set<Player> _players = ConcurrentHashMap.newKeySet();

	public Boat(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		_fromHome = 1;

		getCurrentWay().reCalcNextTime(false);
	}

	@Override
	public boolean setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(super.setXYZ(x, y, z, MoveTask))
		{
			updatePeopleInTheBoat(x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public boolean isFlying()
	{
		return true;
	}

	public void onEvtArrived()
	{
		getCurrentWay().moveNext();
	}

	protected void updatePeopleInTheBoat(int x, int y, int z)
	{
		for(Player player : _players)
		{
			if(player != null)
			{
				player.setXYZ(x, y, z, true);

				for(Servitor servitor : player.getServitors())
					servitor.setXYZ(x, y, z, true);
			}
		}
	}

	public void addPlayer(Player player, Location boatLoc)
	{
		synchronized (_players)
		{
			_players.add(player);

			player.setStablePoint(getReturnLoc());
			player.setBoat(this);
			player.setInBoatPosition(boatLoc);
			player.broadcastPacket(getOnPacket(player, boatLoc));
			player.setLoc(getLoc(), true);

			for(Servitor servitor : player.getServitors())
			{
				servitor.setBoat(this);
				servitor.getMovement().moveToLocation(getLoc(), 0, false);
				servitor.broadcastPacket(getOnPacket(servitor, boatLoc));
				servitor.setLoc(getLoc(), true);
			}
		}
	}

	public void moveInBoat(Playable playable, Location ori, Location loc)
	{
		if(!playable.isPlayer())
			return;

		Player player = playable.getPlayer();
		if(!isShuttle())
		{
			if(player.hasServitor())
			{
				player.sendPacket(SystemMsg.YOU_SHOULD_RELEASE_YOUR_PET_OR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN, ActionFailPacket.STATIC);
				return;
			}

			if(player.isTransformed())
			{
				player.sendPacket(SystemMsg.YOU_CANNOT_BOARD_A_SHIP_WHILE_YOU_ARE_POLYMORPHED, ActionFailPacket.STATIC);
				return;
			}
		}

		if(player.isMovementDisabled() || player.isSitting())
		{
			player.sendActionFailed();
			return;
		}

		if(!player.isInBoat())
			player.setBoat(this);

		loc.h = PositionUtils.getHeadingTo(ori, loc);
		player.setInBoatPosition(loc);
		player.broadcastPacket(inMovePacket(player, ori, loc));
	}

	public void trajetEnded(boolean oust)
	{
		_runState = 0;
		_fromHome = _fromHome == 1 ? 0 : 1;

		IClientOutgoingPacket checkLocation = checkLocationPacket();
		if(checkLocation != null)
			broadcastPacket(infoPacket(), checkLocation);

		if(oust)
		{
			oustPlayers();
			getCurrentWay().reCalcNextTime(false);
		}
	}

	public void teleportShip(int x, int y, int z)
	{
		if(getMovement().isMoving())
			getMovement().stopMove(false);

		for(Player player : _players)
			player.teleToLocation(x, y, z);

		setHeading(calcHeading(x, y));

		setXYZ(x, y, z, true);

		getCurrentWay().moveNext();
	}

	public void oustPlayer(Player player, Location loc, boolean teleport)
	{
		synchronized (_players)
		{
			player.setStablePoint(null);
			player.setBoat(null);
			player.setInBoatPosition(null);
			player.broadcastPacket(getOffPacket(player, loc));
			player.setLoc(loc.correctGeoZ(player.getGeoIndex()), true);

			if(teleport)
				player.teleToLocation(loc);

			for(Servitor servitor : player.getServitors())
			{
				servitor.setBoat(null);
				servitor.setInBoatPosition(null);
				servitor.broadcastPacket(getOffPacket(servitor, loc));
				servitor.setLoc(loc, true);
			}

			_players.remove(player);
		}
	}

	public void removePlayer(Player player)
	{
		synchronized (_players)
		{
			_players.remove(player);
		}
	}

	public void broadcastPacketToPassengers(IBroadcastPacket packet)
	{
		for(Player player : _players)
			player.sendPacket(packet);
	}

	//=========================================================================================================
	public abstract IClientOutgoingPacket infoPacket();

	public abstract IClientOutgoingPacket inMovePacket(Player player, Location src, Location desc);

	public abstract IClientOutgoingPacket stopMovePacket();

	public abstract IClientOutgoingPacket inStopMovePacket(Player player);

	public abstract IClientOutgoingPacket startPacket();

	public abstract IClientOutgoingPacket validateLocationPacket(Player player);

	public abstract IClientOutgoingPacket checkLocationPacket();

	public abstract IClientOutgoingPacket getOnPacket(Playable playable, Location location);

	public abstract IClientOutgoingPacket getOffPacket(Playable playable, Location location);

	public abstract void oustPlayers();

	//=========================================================================================================
	@Override
	public CharacterAI getAI()
	{
		if(_ai == null)
			_ai = new BoatAI(this);

		return _ai;
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastPacket(infoPacket());
	}

	@Override
	public void broadcastPacket(IBroadcastPacket... packets)
	{
		for(Player player : World.getAroundObservers(this))
			player.sendPacket(packets);
	}

	@Override
	public void sendChanges()
	{}

	@Override
	public double getMoveSpeed()
	{
		return _moveSpeed;
	}

	@Override
	public double getRunSpeed()
	{
		return _moveSpeed;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		return null;
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	//=========================================================================================================
	public int getRunState()
	{
		return _runState;
	}

	public void setRunState(int runState)
	{
		_runState = runState;
	}

	public void setMoveSpeed(int moveSpeed)
	{
		_moveSpeed = moveSpeed;
	}

	public void setRotationSpeed(int rotationSpeed)
	{
		_rotationSpeed = rotationSpeed;
	}

	public int getRotationSpeed()
	{
		return _rotationSpeed;
	}

	public BoatWayEvent getCurrentWay()
	{
		return _ways[_fromHome];
	}

	public void setWay(int id, BoatWayEvent v)
	{
		_ways[id] = v;
	}

	public Set<Player> getPlayers()
	{
		return _players;
	}

	public boolean isDocked()
	{
		return _runState == 0;
	}

	public Location getReturnLoc()
	{
		return getCurrentWay().getReturnLoc();
	}

	@Override
	public boolean isBoat()
	{
		return true;
	}

	@Override
	public List<IClientOutgoingPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(!getMovement().isMoving())
		{
			return Collections.singletonList(infoPacket());
		}
		else
		{
			List<IClientOutgoingPacket> list = new ArrayList<IClientOutgoingPacket>(2);
			list.add(infoPacket());
			list.add(movePacket());
			return list;
		}
	}

	public int getBoatId()
	{
		return getObjectId();
	}

	@Override
	public boolean isMovementDisabled()
	{
		return isImmobilized();
	}
}
