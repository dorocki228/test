package l2s.gameserver.service;

import gve.zones.GveZoneManager;
import gve.zones.model.GveOutpost;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.events.impl.InstantClanHallAuctionEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.utils.Language;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static l2s.gameserver.model.entity.events.EventType.*;

/**
 * @author Java-man
 * @since 24.03.2018
 */
public class BroadcastService
{
    private static final BroadcastService INSTANCE = new BroadcastService();

    private static final L2GameServerPacket AVAILABLE_EVENTS_HEADER =
            AAScreenStringPacketPresets.AVAILABLE_EVENTS_HEADER.addOrUpdate("Available Events:");
    private static final L2GameServerPacket AVAILABLE_EVENTS_HEADER_REMOVE =
            AAScreenStringPacketPresets.AVAILABLE_EVENTS_HEADER.remove();
    private static final L2GameServerPacket AVAILABLE_EVENTS_REMOVE =
            AAScreenStringPacketPresets.AVAILABLE_EVENTS.remove();

    private static final List<EventType> EVENT_TYPES = Arrays.asList(SIEGE_EVENT, FUN_EVENT, CUSTOM_PVP_EVENT);
    private static final Predicate<Event> EVENT_FILTER = event ->
            EVENT_TYPES.contains(event.getType()) &&
                    !(event instanceof ClanHallAuctionEvent) && !(event instanceof InstantClanHallAuctionEvent);

    private static final List<Block> STATIC_BLOCKS = List.of(new OlympiadBlock(), new ZoneBlock(), new ArtifactBlock());

    public void broadcastAvailableActivities(Player player)
    {
        Pair<String, Long> result = getAvailableActivities(player);
        var str = result.getLeft();
        if(str == null || str.isEmpty() || "\n".equals(str))
        {
            player.sendPacket(AVAILABLE_EVENTS_HEADER_REMOVE);
            player.sendPacket(AVAILABLE_EVENTS_REMOVE);

            player.startBroadcastAvailableActivities(TimeUnit.SECONDS.toMillis(30));
            return;
        }

        player.sendPacket(AVAILABLE_EVENTS_HEADER);
        var packet = AAScreenStringPacketPresets.AVAILABLE_EVENTS.addOrUpdate(str);
        player.sendPacket(packet);

        player.startBroadcastAvailableActivities(result.getRight());
    }

    private Pair<String, Long> getAvailableActivities(Player player)
    {
        var blocks = createBlockList(player);

        String str = blocks.stream()
                .map(block -> block.message(player))
                .flatMap(Optional::stream)
                .collect(Collectors.joining("\n"));
        var minUpdateTime = blocks.stream().mapToLong(block -> block.updateTime(TimeUnit.MILLISECONDS)).min();

        return new ImmutablePair<>(str, minUpdateTime.orElse(TimeUnit.SECONDS.toMillis(30L)));
    }

    private List<Block> createBlockList(Player player)
    {
        List<Block> eventBlocks = EventHolder.getInstance().getEvents().stream()
                .filter(EVENT_FILTER)
                .map(EventBlock::new)
                .collect(Collectors.toList());
        eventBlocks.addAll(STATIC_BLOCKS);

        Optional<Block> overridingBlock = eventBlocks.stream()
                .filter(block -> block.override(player))
                .findFirst();
        return overridingBlock.map(List::of).orElse(eventBlocks);
    }

    public void sendToAll(L2GameServerPacket packet)
    {
        if (packet == null)
            return;

        GameObjectsStorage.getPlayersStream()
                .forEach(player -> player.sendPacket(packet));
    }

    public static BroadcastService getInstance()
    {
        return INSTANCE;
    }

    private interface Block
    {
        boolean override(Player player);

        Optional<String> message(Player player);

        long updateTime(TimeUnit timeUnit);
    }

    private static final class EventBlock implements Block
    {
        private final Event event;

        public EventBlock(Event event)
        {
            this.event = event;
        }

        @Override
        public boolean override(Player player)
        {
            return event.overrideOnScreenMessage(player);
        }

        @Override
        public Optional<String> message(Player player)
        {
            return event.getOnScreenMessage(player);
        }

        @Override
        public long updateTime(TimeUnit timeUnit)
        {
            return event.onScreenMessageUpdateTime(timeUnit);
        }
    }

    private static final class OlympiadBlock implements Block
    {
        @Override
        public boolean override(Player player)
        {
            return false;
        }

        @Override
        public Optional<String> message(Player player)
        {
            return Olympiad.isRegistrationActive() ? Optional.of("Grand Olympiad") : Optional.empty();
        }

        @Override
        public long updateTime(TimeUnit timeUnit)
        {
            return timeUnit.convert(30, TimeUnit.SECONDS);
        }
    }

    private static final class ZoneBlock implements Block
    {
        @Override
        public boolean override(Player player)
        {
            return false;
        }

        @Override
        public Optional<String> message(Player player)
        {
            Fraction fraction = player.getFraction().revert();
            String builder = GveZoneManager.getInstance().getAttackedOutposts(fraction).stream()
                    .map(g -> g.getName(player.getLanguage()))
                    .collect(Collectors.joining("\n"));

            return builder.isEmpty() ? Optional.empty() : Optional.of(builder);
        }

        @Override
        public long updateTime(TimeUnit timeUnit)
        {
            return timeUnit.convert(30, TimeUnit.SECONDS);
        }
    }

    private static final class ArtifactBlock implements Block
    {
        @Override
        public boolean override(Player player)
        {
            return false;
        }

        @Override
        public Optional<String> message(Player player)
        {
            var builder = new StringBuilder();

            Map<Fraction, Map<Language, String>> artifactBroadcastAttackMap = ArtifactService.getInstance().getBroadcastAttackMap();
            Optional.ofNullable(artifactBroadcastAttackMap.get(player.getFraction())).
                    map(p -> p.get(player.getLanguage())).
                    ifPresent(builder::append);

            return builder.length() == 0 ? Optional.empty() : Optional.of(builder.toString());
        }

        @Override
        public long updateTime(TimeUnit timeUnit)
        {
            return timeUnit.convert(30, TimeUnit.SECONDS);
        }
    }
}