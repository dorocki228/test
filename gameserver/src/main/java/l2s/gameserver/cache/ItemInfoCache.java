package l2s.gameserver.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;

import java.util.concurrent.TimeUnit;

public class ItemInfoCache
{
	private final static ItemInfoCache _instance = new ItemInfoCache();

	public final static ItemInfoCache getInstance()
	{
		return _instance;
	}

	private final Cache<Integer, ItemInfo> cache;

	private ItemInfoCache()
	{
		cache = Caffeine.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS)
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.maximumSize(10_000)
				.build();
	}

	public void put(ItemInstance item)
	{
		cache.put(item.getObjectId(), new ItemInfo(null, item));
	}

	/**
	 * Получить информацию из кеша, по objecId предмета. Если игрок онлайн и все еще владеет этим предметом
	 * информация будет обновлена.
	 * 
	 * @param objectId - идентификатор предмета
	 * @return возвращает описание вещи, или null если описания нет, или уже удалено из кеша
	 */
	public ItemInfo get(int objectId)
	{
		ItemInfo info = cache.getIfPresent(objectId);

		if(info != null)
		{
			Player player = World.getPlayer(info.getOwnerId());

			ItemInstance item = null;

			if(player != null)
				item = player.getInventory().getItemByObjectId(objectId);

			if(item != null)
				if(item.getItemId() == info.getItemId()) {
					info = new ItemInfo(null, item);
					cache.put(item.getObjectId(), info);
					return info;
				}
		}

		return info;
	}
}
