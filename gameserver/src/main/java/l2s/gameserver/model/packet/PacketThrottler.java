package l2s.gameserver.model.packet;

import l2s.commons.limiter.RateLimiter;
import l2s.gameserver.model.Player;

import static l2s.gameserver.Config.FULL_RANGE_PACKET_LIMIT;
import static l2s.gameserver.Config.SHORT_RANGE_PACKET_LIMIT;

public class PacketThrottler {
    public static final int MAX_PACKET_RANGE = 3000;
    private static final int SHORT_PACKET_RANGE = 1500;
    private final Player playerRef;
    private final RateLimiter fullRangePacketLimiter;
    private final RateLimiter shortRangePacketLimiter;

    public PacketThrottler(Player player) {
        playerRef = player;
        fullRangePacketLimiter = new RateLimiter(FULL_RANGE_PACKET_LIMIT, 1000);
        shortRangePacketLimiter = new RateLimiter(SHORT_RANGE_PACKET_LIMIT, 1000);
    }

    public Player getPlayer() {
        return playerRef;
    }

    public PacketRange getPacketRange() {
        if (fullRangePacketLimiter.getRest() > 0)
            return PacketRange.FULL;
        if (shortRangePacketLimiter.getRest() > 0)
            return PacketRange.SHORT;

        return PacketRange.NONE;
    }

    public void onSendPacket() {
        if (!fullRangePacketLimiter.passWithoutStat())
            shortRangePacketLimiter.passWithoutStat();
    }

    public enum PacketRange {
        FULL(MAX_PACKET_RANGE),
        SHORT(SHORT_PACKET_RANGE),
        NONE(0);

        private final int range;

        PacketRange(int range) {
            this.range = range;
        }

        public int range() {
            return range;
        }
    }
}
