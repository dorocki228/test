package handler.bbs;

import static l2s.gameserver.model.entity.events.EventType.CUSTOM_PVP_EVENT;
import static l2s.gameserver.model.entity.events.EventType.FUN_EVENT;
import static l2s.gameserver.model.entity.events.EventType.SIEGE_EVENT;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;
import gve.zones.GveZoneManager;
import gve.zones.model.GveZone;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.entity.ArtifactEntity;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.events.impl.InstantClanHallAuctionEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.service.ArtifactService;
import l2s.gameserver.service.CommunityBoardService;
import l2s.gameserver.service.ConfrontationService;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Util;
import l2s.gameserver.utils.velocity.VelocityUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityBoard extends ScriptBbsHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommunityBoard.class);

    private static final List<EventType> EVENT_TYPES = Arrays.asList(SIEGE_EVENT, FUN_EVENT, CUSTOM_PVP_EVENT);
    private static final Predicate<Event> EVENT_FILTER = event ->
            EVENT_TYPES.contains(event.getType()) &&
                    !(event instanceof ClanHallAuctionEvent) && !(event instanceof InstantClanHallAuctionEvent);

    private final int duration = GameServer.DEVELOP ? 1 : 30;
    private final LoadingCache<Player, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(duration, TimeUnit.SECONDS)
            .build(this::createHtml);

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbshome", "_bbsevent", "_bbsscripts"};
    }

    @Override
    public void onBypassCommand(Player player, String bypass) {
        if(!checkPlayer(player))
            return;

        String[] split = bypass.split(" ");
        String cmd = split[0];
        String html = "";
        if("_bbshome".equals(cmd)) {
            html = cache.get(player);
        } else if(cmd.startsWith("_bbsevent")) {
            var id = Longs.tryParse(split[1]);
            if(id == null) {
                onBypassCommand(player, Config.BBS_DEFAULT);
                return;
            }

            var entry = CommunityBoardService.getInstance().getEntry(id);
            if(entry == null) {
                onBypassCommand(player, Config.BBS_DEFAULT);
                return;
            }

            entry.use(player);

            onBypassCommand(player, Config.BBS_DEFAULT);
            return;
        }
        else if(bypass.startsWith("_bbsscripts")) {
            //Example: bypass _bbsbypass:services.RateBonus:list;_bbshome
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            String sBypass = st2.nextToken().substring(12);
            if(st2.hasMoreTokens())
                Util.communityNextPage(player, st2.nextToken());
            String[] word = sBypass.split("\\s+");
            String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
            String[] path = word[0].split(":");
            if(path.length != 2)
                return;
            Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word[0]);
            if(b != null) {
                final Bypass bp = b.getValue().getDeclaredAnnotation(Bypass.class);
                if(!bp.bbsInvoke()) {
                    logger.warn("Cannot invoke bbs html bypass: " + sBypass);
                    return;
                }
                try {
                    b.getValue().invoke(b.getKey(), player, player.getLastNpc(), word.length == 1 ? new String[0] : args);
                } catch (Exception e) {
                    logger.error("Exception: " + e, e);
                }
            }
            else
                logger.warn("Cannot find html bypass: " + sBypass);
            return;
        }
        ShowBoardPacket.separateAndSend(html, player);
    }

    private String createHtml(Player player) {
        String html = HtmCache.getInstance().getHtml(Config.BBS_PATH + "/home.htm", player);
        Map<String, Object> variables = getVariables(player);

        html = VelocityUtils.evaluate(html, variables);
        html = HtmlUtils.compress(html);
        return html;
    }

    private boolean checkPlayer(Player player) {
        if(player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player))
            return false;

        if(!player.getEvents().stream().allMatch(e -> e.canUseCommunityFunctions(player)))
            return false;

        return true;
    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
    }

    private Map<String, Object> getVariables(Player player) {
        var map = new HashMap<String, Object>(15);

        var rateAdena = Math.round(player.getRateAdena() * 100.0D) - 100;
        if(rateAdena > 0) {
            map.put("adena_bonus", "Adena Bonus - " + rateAdena + '%');
            map.put("adena_bonus_color", "33cc33");
        } else if(rateAdena < 0) {
            map.put("adena_bonus", "Adena Penalty - " + -rateAdena + '%');
            map.put("adena_bonus_color", "FF0000");
        } else {
            map.put("adena_bonus", "No Bonus");
            map.put("adena_bonus_color", "FFFFFF");
        }

        int percentageWater = FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.WATER);
        map.put("water_percent", percentageWater);
        int percentageFire = FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.FIRE);
        map.put("fire_percent", percentageFire);

        int playerCount = GameObjectsStorage.getPlayersCount(GameObjectsStorage.getPlayers().stream().filter(p -> p != null && !p.isInOfflineMode()).count());
        map.put("online", playerCount);

        ZonedDateTime dateTime = ZonedDateTime.now();
        map.put("current_datetime", dateTime);

        map.put("GveZoneManager", GveZoneManager.getInstance());
        map.put("zoneTypes", GveZoneManager.getInstance().getZoneTypes());
        Multimap<Zone.ZoneType, GveZone> zones = GveZoneManager.getInstance().getActiveZonesMap();
        var temp = zones.removeAll(Zone.ZoneType.gve_static_high);
        zones.putAll(Zone.ZoneType.gve_high, temp);
        temp = zones.removeAll(Zone.ZoneType.gve_static_mid);
        zones.putAll(Zone.ZoneType.gve_mid, temp);
        map.put("zones", zones);

        var entries = CommunityBoardService.getInstance().getVisibleEntries(player);
        map.put("entries", entries);

        var raidBossTeleports = CommunityBoardService.getInstance().getRaidBossTeleports(player);
        map.put("RBTeleports", raidBossTeleports);

        var events = EventHolder.getInstance().getEvents().stream().filter(EVENT_FILTER).collect(Collectors.toList());
        var eventsSoon = events.stream()
                .filter(event -> !event.isInProgress())
                .filter(event -> event.getClass() != FortressSiegeEvent.class)
                .filter(event -> event.startDateTime().isBefore(ZonedDateTime.now().plusHours(12)))
                .sorted(Comparator.comparing(Event::startDateTime))
                .collect(Collectors.toUnmodifiableList());
        map.put("events_soon", eventsSoon);

        var fortresses = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
        map.put("fortresses", fortresses);
        var castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        map.put("castles", castles);

        final Collection<ArtifactEntity> artifacts = ArtifactService.getInstance().getArtifactEntityMap().values();
        map.put("player", player);
        map.put("artifacts", artifacts);
        Arrays.stream(Fraction.VALUES_WITH_NONE).forEach(f -> {
            int factionWin = ConfrontationService.getInstance().getFactionWin(f);
            int totalPoints = ConfrontationService.getInstance().getTotalPoints(f).get();
            if(f == Fraction.WATER) {
                map.put("waterScore", factionWin);
                map.put("waterPoints", totalPoints);
            } else {
                map.put("fireScore", factionWin);
                map.put("firePoints", totalPoints);
            }
        });
        final int region = MatchingRoomManager.getInstance().getLocation(player);
        final List<MatchingRoom> rooms = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, true, player);
        final int lookingForParty = rooms.stream().mapToInt(MatchingRoom::getMemberCount).sum();
        map.put("lookingForParty", lookingForParty);
        return map;
    }
}
