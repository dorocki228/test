package l2s.gameserver.model.bbs;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Java-man
 * @since 15.05.2019
 */
public abstract class DelayedCommunityBoardEntry extends CommunityBoardEntry {
    private final Map<Player, Long> playerUsageAttempts = new ConcurrentHashMap<>();

    public DelayedCommunityBoardEntry(String name, String value) {
        super(name, value);

        if (getActionDelayInSeconds() <= 0)
            throw new IllegalArgumentException("DelayedCommunityBoardEntry should have positive delay.");
    }

    protected abstract long getActionDelayInSeconds();

    protected abstract void onActionSchedule(Player player);

    protected abstract void onActionCancel(Player player);

    @Override
    public void use(Player player) {
        long currentTime = System.currentTimeMillis();

        long nextAttemptTime = playerUsageAttempts.getOrDefault(player, (long) 0);
        if (nextAttemptTime > currentTime) {
            return;
        }

        if (!isVisible(player) || !canUse(player)) {
            return;
        }

        long delay = getActionDelayInSeconds();
        playerUsageAttempts.put(player, currentTime + TimeUnit.SECONDS.toMillis(delay + 1));

        ThreadPoolManager.getInstance().schedule(() -> {
            if (!isVisible(player) || !canUse(player)) {
                onActionCancel(player);
                return;
            }

            onAction(player);
        }, delay, TimeUnit.SECONDS);

        onActionSchedule(player);
    }
}
