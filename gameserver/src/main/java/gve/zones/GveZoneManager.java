package gve.zones;

import com.google.common.collect.*;
import com.google.common.math.DoubleMath;
import gve.util.GveMessageUtil;
import gve.zones.model.GveOutpost;
import gve.zones.model.GveZone;
import gve.zones.model.GveZoneStatus;
import l2s.Phantoms.PhantomVariables;
import l2s.Phantoms.Utils.PhantomUtils;
import l2s.commons.lang.ArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.service.LocationBalancerService;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GveZoneManager
{
	private static final GveZoneManager INSTANCE = new GveZoneManager();

	private static final ZoneType[] ZONE_TYPES = new ZoneType[] {
			ZoneType.gve_static_mid,
			ZoneType.gve_static_high,
			ZoneType.gve_low,
			ZoneType.gve_mid,
			ZoneType.gve_high,
			ZoneType.gve_pvp };
	private static final ZoneType[] ZONE_TYPES_WITHOUT_STATIC = new ZoneType[] {
			ZoneType.gve_low,
			ZoneType.gve_mid,
			ZoneType.gve_high,
			ZoneType.gve_pvp };

	private final long ZONE_CHANGE_DELAY = 30;

	//За ZONE_CHANGE_ANNOUNCE минуту до смены локации будет анонс о смене (ZONE_CHANGE_MESSAGE)
	private final long ZONE_CHANGE_ANNOUNCE_5 = 5;
	private final long ZONE_CHANGE_ANNOUNCE_1 = 1;

	private ListMultimap<ZoneType, GveZone> zones = ArrayListMultimap.create();

	private final ListMultimap<ZoneType, GveZone> nextZones = Multimaps.newListMultimap(new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);

	private ScheduledFuture<?> zoneChangeTask;

	public static final EnterLeaveListenerimpl zoneListener = new EnterLeaveListenerimpl();

	public static GveZoneManager getInstance()
	{
		return INSTANCE;
	}

	public void init()
	{
		initZones();

		zoneChangeTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this::changeZones, ZONE_CHANGE_DELAY, ZONE_CHANGE_DELAY, TimeUnit.MINUTES);

		ThreadPoolManager.getInstance().schedule(() -> {
			setNextZones();
			scheduleAnnounceChangeZone(ZONE_CHANGE_ANNOUNCE_5);
		}, ZONE_CHANGE_DELAY - ZONE_CHANGE_ANNOUNCE_5, TimeUnit.MINUTES);
		ThreadPoolManager.getInstance().schedule(() -> scheduleAnnounceChangeZone(ZONE_CHANGE_ANNOUNCE_1), ZONE_CHANGE_DELAY - ZONE_CHANGE_ANNOUNCE_1, TimeUnit.MINUTES);
	}

	public void changeZones()
	{
		Arrays.stream(ZONE_TYPES_WITHOUT_STATIC).forEach(zoneType -> {
			List<GveZone> activatedZones = zones.get(zoneType).stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).collect(Collectors.toList());

			List<GveZone> nextGveZones = nextZones.removeAll(zoneType);
			nextGveZones.forEach(gveZone -> {
				if(gveZone.getStatus() == GveZoneStatus.ACTIVATED)
					return;

				gveZone.setStatus(GveZoneStatus.ACTIVATED);
				gveZone.onChangeStatus();
			});

			activatedZones.removeAll(nextGveZones);
			activatedZones.forEach(gveZone -> {
				gveZone.setStatus(GveZoneStatus.ENABLED);
				gveZone.onChangeStatus();

				GveZone next = Rnd.get(nextGveZones);
				if(next == null)
					return;

				Zone zone = gveZone.getZone();
				List<Player> insidePlayers = zone.getInsidePlayers();
				int locationLimit = LocationBalancerService.getInstance().getLocationLimit(zone.getName());
				if(locationLimit > 0)
				{
					int playerSize = insidePlayers.size();
					int toPlayersSize = Math.min(playerSize, locationLimit);
					List<Player> playersTeleport = insidePlayers.subList(0, toPlayersSize);
					teleportPlayers(next, playersTeleport);
					if(toPlayersSize < playerSize)
					{
						List<Player> playersTeleportToTown = insidePlayers.subList(toPlayersSize, playerSize);
						playersTeleportToTown.forEach(Player::teleToClosestTown);
					}
				}
				else
				{
					teleportPlayers(next, insidePlayers);
				}
			});
		});

		ThreadPoolManager.getInstance().schedule(() -> {
			setNextZones();
			scheduleAnnounceChangeZone(ZONE_CHANGE_ANNOUNCE_5);
		}, ZONE_CHANGE_DELAY - ZONE_CHANGE_ANNOUNCE_5, TimeUnit.MINUTES);
		ThreadPoolManager.getInstance().schedule(() -> scheduleAnnounceChangeZone(ZONE_CHANGE_ANNOUNCE_1), ZONE_CHANGE_DELAY - ZONE_CHANGE_ANNOUNCE_1, TimeUnit.MINUTES);

		GveMessageUtil.updateProtectMessage(Fraction.NONE);
	}

	private void teleportPlayers(GveZone next, List<Player> players)
	{
		players.forEach(player -> {
			var respawnLoc = next.getRandomRespawnLoc(player);
			if(respawnLoc != null)
			{
				respawnLoc = Location.findAroundPosition(respawnLoc, 250, player.getGeoIndex());
				player.teleToLocation(respawnLoc);
			}
			else
				player.teleToClosestTown();
		});
	}

	private void setNextZones()
	{
		Arrays.stream(ZONE_TYPES_WITHOUT_STATIC).forEach(zoneType -> {
			int activeZoneCount = getActiveZoneCount(zoneType);

			List<GveZone> enabledZones = zones.get(zoneType).stream().filter(zone -> zone.getStatus() == GveZoneStatus.ENABLED).collect(Collectors.toList());

			int count = enabledZones.size() >= activeZoneCount ? 0 : activeZoneCount - enabledZones.size();

			List<GveZone> rndEnabledZones = Rnd.get(enabledZones, activeZoneCount);
			nextZones.putAll(zoneType, rndEnabledZones);

			if(count > 0)
			{
				List<GveZone> activatedZones = zones.get(zoneType).stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).collect(Collectors.toList());
				List<GveZone> rndActivatedZones = Rnd.get(activatedZones, count);
				nextZones.putAll(zoneType, rndActivatedZones);
			}
		});
	}

	public List<GveZone> getActiveZones()
	{
		return zones.values().stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).collect(Collectors.toList());
	}

	public Map<String, GveZone> getActiveZoneMap()
	{
		return zones.values().stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).collect(Collectors.toMap(e -> e.getZone().getName(), Function.identity()));
	}

	public Multimap<ZoneType, GveZone> getActiveZonesMap()
	{
		return zones.values().stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).collect(Multimaps.toMultimap(GveZone::getType, Function.identity(), HashMultimap::create));
	}

	public List<GveOutpost> getAttackedOutposts(Fraction fraction)
	{
		return GveZoneManager.getInstance().getActiveZones().stream().filter(z -> z.getType() != Zone.ZoneType.gve_pvp).flatMap(z -> z.getOutposts(fraction, GveOutpost.ATTACKED).stream()).collect(Collectors.toUnmodifiableList());
	}

	public Location getClosestRespawnLoc(Player player)
	{
		Location playerLoc = player.getLoc();

		Location respawnLoc = null;
		for(GveZone z : getActiveZones())
		{
			if(z.getType() == Zone.ZoneType.gve_pvp)
			{
				if(!player.isInZone(z.getZone()))
					continue;
			}

			var tempLoc = z.getClosestRespawnLoc(player);
			if(tempLoc == null)
				continue;

			if(respawnLoc == null || playerLoc.distance(tempLoc) < playerLoc.distance(respawnLoc))
				respawnLoc = tempLoc;
		}

		return respawnLoc;
	}

	public boolean canManageZones()
	{
		return nextZones.isEmpty();
	}

	private static class EnterLeaveListenerimpl implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature creature)
		{
			if(!creature.isPlayable())
			{ return; }

			Player player = creature.getPlayer();

			GveZone gveZone = zone.getGveZone();
			if(gveZone == null)
				return;

			if(gveZone.getStatus() != GveZoneStatus.ACTIVATED || !gveZone.canEnterZone(player))
				ThreadPoolManager.getInstance().execute(player::teleToClosestTown);
			else
				gveZone.onZoneEnter(player);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature creature)
		{
			if(!creature.isPlayable())
			{ return; }

			Player player = creature.getPlayer();

			GveZone gveZone = zone.getGveZone();
			if(gveZone == null)
				return;

			gveZone.onZoneLeave(player);
		}
	}

	private void scheduleAnnounceChangeZone(long minutes)
	{
		String prefix = "\nIn " + minutes + " minutes the following locations will be active:\n";
		String message = nextZones.values().stream().map(gveZone -> gveZone.getInGameName() + " [" + gveZone.getType() + "]").collect(Collectors.joining("\n", prefix, "\n"));

		Announcements.announceToAll(message);
	}

	public int getActiveZoneCount(ZoneType zoneType)
	{
		return ServerVariables.getInt("gve_zone_active_count_" + zoneType.name(), 2);
	}

	public void setActiveZoneCount(ZoneType zoneType, int count)
	{
		ServerVariables.set("gve_zone_active_count_" + zoneType.name(), count);
	}

	public void setZoneStatus(String zoneName, GveZoneStatus status)
	{
		zones.values().stream().filter(zone -> zone.getZone().getName().equals(zoneName)).findAny().ifPresent(zone -> {
			zone.setStatus(status);
			zone.onChangeStatus();
		});
	}

	public ZoneType[] getZoneTypes()
	{
		return ZONE_TYPES_WITHOUT_STATIC;
	}

	public Stream<ZoneType> getZoneTypesStream()
	{
		return Arrays.stream(ZONE_TYPES);
	}

	private void initZones()
	{
		zones = ReflectionManager.MAIN.getZones().stream().filter(zone -> ArrayUtils.contains(ZONE_TYPES, zone.getType())).map(Zone::makeGveZone).collect(Multimaps.toMultimap(GveZone::getType, Function.identity(), ArrayListMultimap::create));

		Arrays.stream(ZONE_TYPES_WITHOUT_STATIC).forEach(zoneType -> {
			int activeZoneCount = getActiveZoneCount(zoneType);

			List<GveZone> gveZones = zones.get(zoneType);

			long count = gveZones.stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).count();
			if(count < activeZoneCount)
			{
				List<GveZone> temp = gveZones.stream().filter(zone -> zone.getStatus() == GveZoneStatus.ENABLED).collect(Collectors.toList());

				Rnd.get(temp, activeZoneCount).forEach(zone -> zone.setStatus(GveZoneStatus.ACTIVATED));
			}
			else if(count > activeZoneCount)
			{
				List<GveZone> temp = gveZones.stream().filter(zone -> zone.getStatus() == GveZoneStatus.ACTIVATED).collect(Collectors.toList());

				Rnd.get(temp, activeZoneCount).forEach(zone -> zone.setStatus(GveZoneStatus.ENABLED));
			}
		});

		zones.forEach((zone, gveZone) -> gveZone.onChangeStatus());
	}

	public String getZonesHtml()
	{
		StringBuilder builder = new StringBuilder();
		Arrays.stream(ZONE_TYPES_WITHOUT_STATIC).forEach(zoneType -> {
			int activeZoneCount = getActiveZoneCount(zoneType);

			List<GveZone> gveZones = zones.get(zoneType);

			builder.append("<table width=260>\n" + "    <tr>\n" + "        <td rowspan=\"" + gveZones.size() + "\"><font color=\"d5a34c\">" + zoneType + ":</font></td>\n" + "    </tr>\n" + "    <tr>\n" + "        <td width=150><font color=\"156b7c\">Number of locations - " + activeZoneCount + "</font></td>\n" + "        <td width=30><edit var=\"count_" + zoneType.name() + "\" length=\"3\" width=30 height=12></td>\n" + "        <td><button value=\"Set\" " + "action=\"bypass -h admin_gve_zone_set_active_count " + zoneType.name() + " $count_" + zoneType.name() + "\" width=50 height=20 " + "back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>\n" + "    </tr>\n");

			gveZones.forEach(gveZone -> {
				GveZoneStatus status = gveZone.getStatus();
				Pair<String, String> button = status == GveZoneStatus.DISABLED ? new ImmutablePair<>("Enable", "ENABLED") : new ImmutablePair<>("Disable", "DISABLED");
				String zoneName = gveZone.getName();
				builder.append("    <tr>\n" + "        <td width=130>" + zoneName + "</td>\n" + "        <td width=50><font color=\"" + status.getColor() + "\">" + status + "</font></td>\n" + "        <td><button value=\"" + button.getLeft() + "\" action=\"bypass -h admin_gve_zone_set_status " + zoneName + ' ' + button.getRight() + "\" width=50 height=20 " + "back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>\n" + "    </tr>\n");
			});

			builder.append("</table>");
		});

		return builder.toString();
	}

	private static final String PREFIX = "<font color=\"4CB950\">";
	private static final String MIN_COUNT_STRING = "<font color=\"4CB950\">|</font>|||||||||||||||||||";
	private static final String MAX_COUNT_STRING = "<font color=\"4CB950\">||||||||||||||||||||</font>";

	public String zonePlayersCountHtml(GveZone gveZone)
	{
		Zone zone = gveZone.getZone();
		if(zone == null)
		{ return MIN_COUNT_STRING; }

		var playersCount = zone.getInsidePlayers().size();
		if(playersCount <= 5)
			return MIN_COUNT_STRING;
		if(playersCount >= 100)
			return MAX_COUNT_STRING;
		var count = DoubleMath.roundToInt(playersCount / 5.0D, RoundingMode.CEILING);

		return new StringBuilder(PREFIX + "||||||||||||||||||||").insert(PREFIX.length() + count, "</font>").toString();
	}

	public String zoneLoad(GveZone gveZone)
	{
		Zone zone = gveZone.getZone();
		int playersCount = zone == null ? 0 : zone.getInsidePlayers().size();
		playersCount = playersCount == 0 ? Rnd.get(1, 3) : playersCount;
		String bg;
		String fill;
		if(playersCount <= 33)
		{
			bg = "L2UI_CT1.Gauge_DF_Large_Food_Bg_Center";
			fill = "L2UI_CT1.Gauge_DF_Large_Food_Center";
		}
		else if(playersCount <= 66)
		{
			bg = "L2UI_CT1.Gauge_DF_Large_Weight_bg_Center1";
			fill = "L2UI_CT1.Gauge_DF_Large_Weight_Center1";
		}
		else
		{
			bg = "L2UI_CT1.Gauge_DF_Large_Weight_bg_Center3";
			fill = "L2UI_CT1.Gauge_DF_Large_Weight_Center3";
		}
		playersCount = Math.min(playersCount, 100);
		return HtmlUtils.getGauge(120, playersCount, 100, true, bg, fill, 16, -12);
	}
	
	public ListMultimap<ZoneType, GveZone> getZones() {
		return zones;
	}
	
}
