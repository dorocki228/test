package l2s.gameserver.service;

import gve.zones.GveZoneManager;
import gve.zones.model.GveZone;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Location;

/**
 * @author mangol
 */
public class LocationBalancerService {
	public static LocationBalancerService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean canTeleport(Player player, Location location) {
		Map<String, GveZone> activeZoneMap = GveZoneManager.getInstance().getActiveZoneMap();
		Set<Map.Entry<String, Integer>> entries = Config.BALANCE_LOCATIONS.entrySet();
		for(Map.Entry<String, Integer> entry : entries) {
			GveZone gveZone = activeZoneMap.get(entry.getKey());
			if(gveZone == null) {
				continue;
			}
			Zone zone = gveZone.getZone();
			if(zone.checkIfInZone(location.x, location.y, location.z)) {
				if(Config.FACTION_BALANCE_IN_LOCATIONS && getLocationUniquePlayerCount(zone) >= Config.FACTION_BALANCE_IN_LOCATION_MIN_COUNT) {
					double percentExpressionFromFraction = getPercentExpressionFromFraction(gveZone, player.getFraction());
					if(percentExpressionFromFraction > Config.FACTION_BALANCE_IN_LOCATION_MAX_PERCENT) {
						player.sendMessage(new CustomMessage("locationBalancer.s2"));
						return false;
					}
				}
				if(Config.MAX_LOAD_LOCATIONS_SYSTEM && getLocationUniquePlayerCount(zone) >= entry.getValue()) {
					boolean duplicatedHwid = zone.getInsidePlayers().stream().filter(e-> e.getHwidHolder() != null).anyMatch(e -> e.getHwidHolder().equals(player.getHwidHolder()));
					if(!duplicatedHwid) {
						player.sendMessage(new CustomMessage("locationBalancer.s1"));
						return false;
					}
				}
				return true;
			}
		}
		return true;
	}

	public boolean isLocationLimit(GveZone gveZone) {
		int locationLimit = getLocationLimit(gveZone.getZone().getName());
		if(locationLimit == -1) {
			return false;
		}
		int playerCount = gveZone.getZone().getInsidePlayers().size();
		return playerCount >= locationLimit;
	}

	public int getPercentExpressionFromFraction(GveZone gveZone, Fraction fraction) {
		List<Player> playerList = gveZone.getZone().getInsidePlayers();
		Map<Fraction, Integer> fractionMap = playerList.stream().
				collect(Collectors.groupingBy(Creature::getFraction,
						Collectors.filtering(e-> e.getHwidHolder() != null, Collectors.mapping(Player::getHwidHolder, Collectors.collectingAndThen(Collectors.toSet(), Set::size)))));
		Fraction revertFraction = fraction.revert();
		long currentFractionSize = fractionMap.getOrDefault(fraction, 0);
		long revertFractionSize = fractionMap.getOrDefault(revertFraction, 0);
		long sum = currentFractionSize + revertFractionSize;
		if(sum == 0) {
			return 0;
		}
		return Math.toIntExact((long) Math.ceil(currentFractionSize * 100d / sum));
	}

	public int getLocationLimit(String id) {
		return Config.BALANCE_LOCATIONS.getOrDefault(id, -1);
	}

	private int getLocationUniquePlayerCount(Zone zone) {
		List<Player> players = zone.getInsidePlayers();
		return players.stream().filter(p -> p.getHwidHolder() != null).map(Player::getHwidHolder).
				collect(Collectors.collectingAndThen(Collectors.toSet(), Set::size));
	}

	static class SingletonHolder {
		public static final LocationBalancerService INSTANCE = new LocationBalancerService();
	}
}
