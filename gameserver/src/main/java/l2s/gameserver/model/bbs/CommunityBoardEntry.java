package l2s.gameserver.model.bbs;

import l2s.gameserver.model.Player;
import l2s.gameserver.service.CommunityBoardService;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Java-man
 * @since 15.05.2018
 */
public abstract class CommunityBoardEntry {
    private static final AtomicLong idGenerator = new AtomicLong();

    private final long id;
    private final String name;
    private final String value;

    public CommunityBoardEntry(String name, String value) {
        id = idGenerator.getAndIncrement();
        this.name = name;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    protected abstract CommunityBoardEntryType getType();

    public abstract boolean isVisible(Player player);

    protected abstract boolean canUse(Player player);

    protected abstract void onAction(Player player);

    public void use(Player player) {
        if (!isVisible(player) || !canUse(player)) {
            return;
        }

        onAction(player);
    }

    public void register() {
        CommunityBoardService.getInstance().addEntry(getId(), this);
    }

    public void unregister() {
        CommunityBoardService.getInstance().removeEntry(getId());
    }
}
