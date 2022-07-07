package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.network.l2.s2c.ExServerPrimitivePacket;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.utils.Location;

public class ValidatePosition extends L2GameClientPacket 
{
	public static final int MIN_VALIDATE_DIFF = 350;
	private final Location _loc;
	private int _boatObjectId;
	private Location _lastClientPosition;
	private Location _lastServerPosition;

	public ValidatePosition()
	{
		_loc = new Location();
	}

	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
		_boatObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{ return; }
		if(activeChar.isTeleporting() || activeChar.isInObserverMode())
		{ return; }
		_lastClientPosition = activeChar.getLastClientPosition();
		_lastServerPosition = activeChar.getLastServerPosition();
		if(_lastClientPosition == null)
		{
			_lastClientPosition = activeChar.getLoc();
		}
		if(_lastServerPosition == null)
		{
			_lastServerPosition = activeChar.getLoc();
		}
		if(activeChar.getX() == 0 && activeChar.getY() == 0 && activeChar.getZ() == 0)
		{
			correctPosition(activeChar);
			return;
		}
		if(activeChar.isInFlyingTransform())
		{
			if(_loc.x > -166168)
			{
				activeChar.setTransform(null);
				return;
			}
			if(_loc.z <= 0 || _loc.z >= 6000)
			{
				activeChar.teleToLocation(activeChar.getLoc().setZ(Math.min(5950, Math.max(50, _loc.z))));
				return;
			}
		}
		double diff = activeChar.getDistance(_loc.x, _loc.y);
		int dz = Math.abs(_loc.z - activeChar.getZ());
		int h = _lastServerPosition.z - activeChar.getZ();
		if(_boatObjectId > 0)
		{
			final Boat boat = BoatHolder.getInstance().getBoat(_boatObjectId);
			if(boat != null && activeChar.getBoat() == boat)
			{
				activeChar.setHeading(_loc.h);
				boat.validateLocationPacket(activeChar);
			}
			activeChar.setLastClientPosition(_loc.setH(activeChar.getHeading()));
			activeChar.setLastServerPosition(activeChar.getLoc());
			return;
		}
		if(activeChar.isFalling())
		{
			diff = 0.0;
			dz = 0;
			h = 0;
		}
		final int maxDiff = 256 + getClient().getPing() * activeChar.getMoveSpeed() / 1000;
		final int maxZDiff = activeChar.maxZDiff() + 64;
		final int dbgMove = activeChar.getVarInt("debugMove", 0);
		if(dbgMove > 0)
		{
			Location tmp1 = activeChar.getLoc().clone().setZ(activeChar.getZ() + 64);
			final ExServerPrimitivePacket sp = new ExServerPrimitivePacket("", tmp1.getX(),tmp1.getY(),tmp1.getZ());
			sp.addLine("Diff: " + diff + " max: " + maxDiff, 16777215, true, activeChar.getX(), activeChar.getY(), activeChar.getZ() + 80, _loc.getX(), _loc.getY(), _loc.getZ() + 64);
			activeChar.broadcastPacket(sp);
		}

		if(!activeChar.isIgnoreValidatePosition())
		{
			final boolean canFall = !activeChar.isInWater() && !activeChar.isFlying();
			if(canFall && h >= 256 )
			{
				activeChar.falling(h);
			}else if (diff > MIN_VALIDATE_DIFF && isNotCastingFlySkillAndNotFalling(activeChar)) 
			{
				//Если валидация вызвана скиллом телепорта, до конца каста не обрабатываем
				if (activeChar.isInWater()) {
					activeChar.waterFall();
					activeChar.validateLocation(0);
				} else {
					activeChar.validateLocation(1);
				}
			}
			else if(canFall && dz >= maxZDiff)
			{
				if(activeChar.getIncorrectValidateCount() >= 6)
				{
					activeChar.teleToClosestTown();
				}
				else if(activeChar.getIncorrectValidateCount() > 3)
				{
					activeChar.teleToLocation(Location.findPointToStay(_lastServerPosition, activeChar.getIncorrectValidateCount() * 32, activeChar.getGeoIndex()));
					activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
				}
				else
				{
					activeChar.teleToLocation(_lastServerPosition);
					activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
				}
			}
			else if(canFall && dz >= maxZDiff / 2)
			{
				activeChar.validateLocation(0);
			}
			else if(_loc.z < -30000 || _loc.z > 30000)
			{
				if(activeChar.getIncorrectValidateCount() >= 3)
				{
					activeChar.teleToClosestTown();
				}
				else
				{
					correctPosition(activeChar);
					activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
				}
			}
			else if(diff > 1024.0)
			{
				if(activeChar.getIncorrectValidateCount() >= 6)
				{
					activeChar.teleToClosestTown();
				}
				else if(activeChar.getIncorrectValidateCount() > 3)
				{
					activeChar.teleToLocation(Location.findPointToStay(_lastServerPosition, activeChar.getIncorrectValidateCount() * 32, activeChar.getGeoIndex()));
					activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
				}
				else
				{
					activeChar.teleToLocation(activeChar.getLoc());
					activeChar.setIncorrectValidateCount(activeChar.getIncorrectValidateCount() + 1);
				}
			}
			else if(diff > maxDiff)
			{
				activeChar.validateLocation(1);
			}
			else
			{
				activeChar.setIncorrectValidateCount(0);
			}
		}
		for (Servitor servitor : activeChar.getServitors())
			if (!servitor.isInRange())
				servitor.teleportToOwner();

		activeChar.setLastClientPosition(_loc.setH(activeChar.getHeading()));
		activeChar.setLastServerPosition(activeChar.getLoc());
	}

	private void correctPosition(final Player activeChar)
	{
		if(activeChar.isGM())
		{
			activeChar.sendMessage("Server loc: " + activeChar.getLoc());
			activeChar.sendMessage("Correcting position...");
		}
		if(_lastServerPosition.x != 0 && _lastServerPosition.y != 0 && _lastServerPosition.z != 0)
		{
			if(GeoEngine.getNSWE(_lastServerPosition.x, _lastServerPosition.y, _lastServerPosition.z, activeChar.getGeoIndex()) == 15)
			{
				activeChar.teleToLocation(_lastServerPosition);
			}
			else
			{
				activeChar.teleToClosestTown();
			}
		}
		else if(_lastClientPosition.x != 0 && _lastClientPosition.y != 0 && _lastClientPosition.z != 0)
		{
			if(GeoEngine.getNSWE(_lastClientPosition.x, _lastClientPosition.y, _lastClientPosition.z, activeChar.getGeoIndex()) == 15)
			{
				activeChar.teleToLocation(_lastClientPosition);
			}
			else
			{
				activeChar.teleToClosestTown();
			}
		}
		else
		{
			activeChar.teleToClosestTown();
		}
	}

	public static boolean isNotCastingFlySkillAndNotFalling(Player player) {
		if (player.isFalling())
			return false;

		Skill castingSkill = player.getCastingSkill();
		if (castingSkill != null && castingSkill.getFlyType() != FlyToLocationPacket.FlyType.NONE)
			return false;

		return true;
	}
}
