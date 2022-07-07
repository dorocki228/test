package l2s.gameserver.instancemanager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Fence;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.utils.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author Laky
 * @author Java-man
 */
public class FenceBuilderManager
{
	private final Map<Integer, Fence> fences = new HashMap<>();
	private final Cache<Integer, Fence> lastFences = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

	public static FenceBuilderManager getInstance()
	{
		return LazyHolder.INSTANCE;
	}

	private FenceBuilderManager()
	{}

	public void fenceMenu(Player player)
	{
		player.sendPacket(new HtmlMessage(5).setFile("admin/fence.htm"));
	}

	public void spawnFence(Player player, int type, int wid, int hi, int size)
    {
        IntStream.range(0, size)
                .map(i -> IdFactory.getInstance().getNextId())
                .forEach(id ->
                {
                    Fence fence = new Fence(id, player.getLoc(), type, wid, hi);
                    fence.setReflection(ReflectionManager.MAIN);
                    fence.spawnMe(new Location(player.getX(), player.getY(), player.getZ() + 32));
                    fence.broadcastCharInfo();
                    fence.setCollision(true);

                    fences.put(id, fence);
                    lastFences.put(id, fence);
                });

		fenceMenu(player);
	}

	public void deleteLastFence(Player player)
	{
		lastFences.asMap().keySet().forEach(key ->
		{
			Fence fence = lastFences.getIfPresent(key);
			if(fence != null)
			{
				fence.decayMe();
				fence.setCollision(false);

				fences.remove(key);
				lastFences.invalidate(key);
			}
		});

		fenceMenu(player);
	}

	public void deleteAllFences(Player player)
	{
		fences.forEach((key, fence) ->
		{
			if(fence != null)
			{
				fence.decayMe();
				fence.setCollision(false);
			}
		});

		fences.clear();
		lastFences.invalidateAll();

		fenceMenu(player);
	}

	public void changeFenceType(Player player, int t)
	{
		lastFences.asMap().keySet().stream()
				.mapToInt(key -> key).mapToObj(lastFences::getIfPresent)
				.filter(Objects::nonNull)
				.forEach(f ->
				{
					f.setType(t);
					f.broadcastCharInfo();
				});

		fenceMenu(player);
	}

	private static class LazyHolder
	{
		private static final FenceBuilderManager INSTANCE = new FenceBuilderManager();
	}
}
