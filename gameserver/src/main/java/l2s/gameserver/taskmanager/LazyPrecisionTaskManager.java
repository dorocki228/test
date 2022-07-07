package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.service.BroadcastService;

import java.util.concurrent.Future;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();

	public static final LazyPrecisionTaskManager getInstance()
	{
		return _instance;
	}

	private LazyPrecisionTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> purge(), 60000L, 60000L);
	}

	public Future<?> addPCCafePointsTask(Player player)
	{
		long delay = Config.ALT_PCBANG_POINTS_DELAY * 60000L;
		return scheduleAtFixedRate(() -> {
			if(!player.isInOfflineMode() && player.getLevel() >= Config.ALT_PCBANG_POINTS_MIN_LVL)
				if(!Config.ALT_PCBANG_POINTS_ONLY_PREMIUM || player.hasPremiumAccount())
					player.addPcBangPoints(Config.ALT_PCBANG_POINTS_BONUS, Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0.0 && Rnd.chance(Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE), true);
		}, delay, delay);
	}

	public Future<?> startPremiumAccountExpirationTask(Player player, int expire)
	{
		long delay = expire * 1000L - System.currentTimeMillis();
		return schedule(() -> player.removePremiumAccount(), delay);
	}

	public Future<?> addNpcAnimationTask(NpcInstance npc)
	{
		return scheduleAtFixedRate(() -> {
			if(npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving() && !npc.isInCombat())
				npc.onRandomAnimation();
		}, 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
	}

	public Future<?> startBroadcastAvailableActivitiesTask(Player player, long delay)
	{
		return schedule(() -> BroadcastService.getInstance().broadcastAvailableActivities(player), delay);
	}
}
