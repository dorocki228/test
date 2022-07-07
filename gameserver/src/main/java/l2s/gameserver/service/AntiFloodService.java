package l2s.gameserver.service;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AntiFloodService {
    private static final AntiFloodService INSTANCE = new AntiFloodService();
    private final Map<ChatType, Map<Integer, Long>> map;

    private AntiFloodService() {
        this.map = Arrays.stream(ChatType.values()).collect(Collectors.toUnmodifiableMap(p -> p, p -> new ConcurrentHashMap<>()));
    }

    public static AntiFloodService getInstance() {
        return INSTANCE;
    }

    public long canSay(ChatType type, Player player) {
        if(player == null)
            return 0;
        Map<Integer, Long> map = this.map.get(type);
        long endStamp = map.getOrDefault(player.getObjectId(), 0L);
        long currentTimeMillis = System.currentTimeMillis();
        if(endStamp <= currentTimeMillis) {
            long plusMillis = 0;
            if(type == ChatType.FRACTION_WORLD)
                plusMillis = Duration.ofMinutes(2).toMillis();
            map.put(player.getObjectId(), currentTimeMillis + plusMillis);
            return 0;
        }
        return endStamp - currentTimeMillis;
    }
}
