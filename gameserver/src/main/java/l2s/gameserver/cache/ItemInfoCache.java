package l2s.gameserver.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;

import java.util.concurrent.TimeUnit;

public class ItemInfoCache
{
    private static final ItemInfoCache INSTANCE = new ItemInfoCache();

    private final Cache<Integer, ItemInfo> cache;

    public static final ItemInfoCache getInstance()
    {
        return INSTANCE;
    }

    private ItemInfoCache()
    {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(10_000)
                .build();
    }

    public void put(ItemInstance item)
    {
        cache.put(item.getObjectId(), new ItemInfo(item));
    }

    public ItemInfo get(int objectId)
    {
        ItemInfo info = cache.getIfPresent(objectId);

        if(info != null)
        {
            Player player = GameObjectsStorage.getPlayer(info.getOwnerId());
            ItemInstance item = null;
            if(player != null)
                item = player.getInventory().getItemByObjectId(objectId);
            if(item != null && item.getItemId() == info.getItemId())
                cache.put(item.getObjectId(), info = new ItemInfo(item));
        }
        return info;
    }
}
