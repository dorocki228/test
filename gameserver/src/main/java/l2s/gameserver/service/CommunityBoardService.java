package l2s.gameserver.service;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.bbs.CommunityBoardEntry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import l2s.gameserver.model.bbs.RaidBossTeleportationCommunityBoardEntry;

/**
 * @author Java-man
 * @since 11.06.2018
 */
public class CommunityBoardService
{
    private static final CommunityBoardService INSTANCE = new CommunityBoardService();

    private final Map<Long, CommunityBoardEntry> entries;

    public CommunityBoardService()
    {
        entries = new ConcurrentHashMap<>();
    }

    public void addEntry(long id, CommunityBoardEntry entry)
    {
        entries.put(id, entry);
    }

    public void removeEntry(long id)
    {
        entries.remove(id);
    }

    public CommunityBoardEntry getEntry(long id)
    {
        return entries.get(id);
    }

    public List<CommunityBoardEntry> getVisibleEntries(Player player)
    {
        return entries.values().stream()
                .filter(entry -> entry.isVisible(player) && !entry.getValue().equals("RaidBossTeleport"))
                .collect(Collectors.toUnmodifiableList());
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getEntries(Class<T> clazz) {
        return entries.values().stream()
            .filter(entry -> entry.getClass() == clazz)
            .map(entry -> (T) entry)
            .collect(Collectors.toList());
    }

    public List<CommunityBoardEntry> getRaidBossTeleports(Player player) {
        return entries.values().stream()
            .filter(entry -> (entry instanceof RaidBossTeleportationCommunityBoardEntry) && entry.isVisible(player))
            .limit(5)
            .collect(Collectors.toList());
    }

    public static CommunityBoardService getInstance()
    {
        return INSTANCE;
    }
}
