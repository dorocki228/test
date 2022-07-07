package l2s.gameserver.utils;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TeleportPoint;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.mapregion.DomainArea;
import l2s.gameserver.templates.mapregion.RestartArea;
import l2s.gameserver.templates.mapregion.RestartPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TeleportUtils
{
	private static final Logger _log = LoggerFactory.getLogger(TeleportUtils.class);

	private static final Location DEFAULT_RESTART = new Location(17817, 170079, -3530);

	private static final Location[] WATER_DEFAULT = {
			new Location(44296, 50200, -3056),
			new Location(45192, 50040, -3056),
			new Location(44216, 49224, -3056),
			new Location(45480, 48216, -3056),
			new Location(47560, 49000, -2992) };
	private static final Location[] FIRE_DEFAULT = {
			new Location(82728, 53576, -1448),
			new Location(82328, 54648, -1520),
			new Location(82792, 55752, -1520),
			new Location(81064, 55768, -1552),
			new Location(80056, 56056, -1552),
			new Location(80856, 54696, -1520),
			new Location(80344, 53464, -1552),
			new Location(81864, 53496, -1488) };

	public static TeleportPoint getRestartPoint(Player player, RestartType restartType)
	{
		return getRestartPoint(player, player.getLoc(), restartType);
	}

	public static TeleportPoint getRestartPoint(Player player, Location from, RestartType restartType)
	{
		TeleportPoint teleportPoint = new TeleportPoint();
		Reflection r = player.getReflection();
		if(!r.isMain())
		{
			if(r.getCoreLoc() != null)
				return teleportPoint.setLoc(r.getCoreLoc());
			if(r.getReturnLoc() != null)
				return teleportPoint.setLoc(r.getReturnLoc());
		}
		Clan clan = player.getClan();
		if(clan != null)
		{
			int residenceId = 0;
			if(restartType == RestartType.TO_CLANHALL)
				residenceId = clan.getHasHideout();
			else if(restartType == RestartType.TO_CASTLE)
				residenceId = clan.getCastle();
			if(residenceId != 0)
			{
				Residence residence = ResidenceHolder.getInstance().getResidence(residenceId);
				if(residence != null)
				{
					Reflection reflection = residence.getReflection(clan.getClanId());
					if(reflection != null)
					{
						teleportPoint.setLoc(residence.getOwnerRestartPoint());
						teleportPoint.setReflection(reflection);
						return teleportPoint;
					}
				}
			}
		}
		if(player.isPK())
		{
			if(player.getPKRestartPoint() != null)
				return teleportPoint.setLoc(player.getPKRestartPoint());
		}
		else if(player.getRestartPoint() != null)
			return teleportPoint.setLoc(player.getRestartPoint());

		Fraction f = player.getFraction();

		if(f == Fraction.FIRE)
			return teleportPoint.setLoc(Rnd.get(FIRE_DEFAULT));
		else if(f == Fraction.WATER)
			return teleportPoint.setLoc(Rnd.get(WATER_DEFAULT));
		else
			return teleportPoint.setLoc(DEFAULT_RESTART);
	}

	public static Location getRandomTown(Player player)
	{
		DomainArea domainArea = MapRegionManager.getInstance().getDomainAreaById(Rnd.get(1, 4));
		RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, domainArea.getTerritory().getRandomLoc(player.getGeoIndex()));
		if(ra != null)
		{
			RestartPoint rp = ra.getRestartPoint().get(player.getRace());
			Location restartPoint = (Location) Rnd.get((List) rp.getRestartPoints());
			Location PKrestartPoint = (Location) Rnd.get((List) rp.getPKrestartPoints());
			return player.isPK() ? PKrestartPoint : restartPoint;
		}
		return domainArea.getTerritory().getRandomLoc(player.getGeoIndex());
	}

}
