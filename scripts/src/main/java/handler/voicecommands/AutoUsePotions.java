package handler.voicecommands;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExUseSharedGroupItem;
import l2s.gameserver.skills.TimeStamp;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/*
 * @author Ro0TT
 * @date 25.06.2015
 */

public class AutoUsePotions implements OnInitScriptListener
{
    private static AutoUsePotions instance;

    public static final String acp_command = "acp";

    public static final String acpcp_enable = "acpcp_enable";
    public static final String acpcp_percent = "acpcp_percent";
    public static final String acphp_enable = "acphp_enable";
    public static final String acphp_percent = "acphp_percent";
    public static final String acpmp_enable = "acpmp_enable";
    public static final String acpmp_percent = "acpmp_percent";

    private final ConcurrentHashMap<Integer, HealListener> players = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    public static AutoUsePotions getInstance() {
        return instance;
    }

    public ConcurrentHashMap<Integer, HealListener> getPlayers() {
        return players;
    }

    @Override
    public void onInit()
    {
        instance = this;

        PlayerListenerList.addGlobal((OnPlayerEnterListener) player ->
        {
            if (checkRestricts(player) && checkNesesary(player))
                enablePlayer(player);
        });

        PlayerListenerList.addGlobal((OnPlayerExitListener) this::disblePlayer);

        ThreadPoolManager.getInstance().scheduleAtFixedDelay(() ->
        {
            players.values().forEach(HealListener::check);
        }, 1000, 1000);
    }

    private class HealListener implements Runnable
    {
        private final HardReference<Player> ref;
        private final int charId;
        private ScheduledFuture<?> scheduledFuture;

        private HealListener(Player player)
        {
            ref = player.getRef();
            charId = player.getObjectId();
        }

        @Override
        public void run()
        {
            Player player = ref.get();

            if (player == null || !player.isOnline() || !checkRestricts(player))
            {
                stop();
                return;
            }

            int[][] potions = new int[3][];

            boolean needHeal = false;

            if (player.getCurrentCpPercents() < player.getVarInt(acpcp_percent, 90) && player.getVarBoolean(acpcp_enable, false))
            {
                needHeal = true;
                potions[0] = Config.ACP_POTIONS_CP;
            }

            if (player.getCurrentHpPercents() < player.getVarInt(acphp_percent, 90) && player.getVarBoolean(acphp_enable, false))
            {
                needHeal = true;
                potions[1] = Config.ACP_POTIONS_HP;
            }

            if (player.getCurrentMpPercents() < player.getVarInt(acpmp_percent, 90) && player.getVarBoolean(acpmp_enable, false))
            {
                needHeal = true;
                potions[2] = Config.ACP_POTIONS_MP;
            }

            if (!needHeal)
            {
                stop();
                return;
            }

            // проверяем все возможные бутылки, если находим нужную - используем.
            for (int[] potions1 : potions)
                if (potions1 != null)
                    for (int potion : potions1)
                    {
                        ItemInstance item = player.getInventory().getItemByItemId(potion);
                        if (item != null && item.getCount() != 0L)
                        {
                            boolean success = canUse(player, item) && item.getTemplate().getHandler().useItem(player, item, false);

                            if (success)
                            {
                                long nextTimeUse = item.getTemplate().getReuseType().next(item);

                                if (nextTimeUse > System.currentTimeMillis())
                                {
                                    TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
                                    player.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);
                                    if (item.getTemplate().getReuseDelay() > 0)
                                        player.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
                                }

                                if (potions1 == Config.ACP_POTIONS_HP || potions1 == Config.ACP_POTIONS_MP)
                                    break;
                            }
                        }
                    }
        }

        private boolean canUse(Player player, ItemInstance item)
        {
            if (player.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
                return false;

            if (!item.getTemplate().testCondition(player, item))
                return false;

            if (player.getInventory().isLockedItem(item))
                return false;

//			IBroadcastPacket result = player.canUseItem(item, false);
//			if (result != null)
//				return false;

            if ((player.isOutOfControl()) || (player.isDead()) || (player.isStunned()) || (player.isSleeping()) || (player.isParalyzed()))
                return false;

            return true;
        }

        public void start()
        {
            stop();
            if (scheduledFuture == null)
                scheduledFuture = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this, 0, 500);
        }

        public void stop()
        {
            if (scheduledFuture != null)
            {
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            }
        }

        public void check()
        {
            if (!Config.ACP_ENABLED || scheduledFuture != null)
                return;

            Player player = ref.get();

            if (player == null || !player.isOnline() || !checkRestricts(player))
                return;

            if (player.getCurrentCpPercents() < player.getVarInt(acpcp_percent, 90) && player.getVarBoolean(acpcp_enable, false))
            {
                start();
                return;
            }

            if (player.getCurrentHpPercents() < player.getVarInt(acphp_percent, 90) && player.getVarBoolean(acphp_enable, false))
            {
                start();
                return;
            }

            if (player.getCurrentMpPercents() < player.getVarInt(acpmp_percent, 90) && player.getVarBoolean(acpmp_enable, false))
            {
                start();
                return;
            }
        }
    }

    void enablePlayer(Player player)
    {
        if (!players.containsKey(player.getObjectId()))
            players.put(player.getObjectId(), new HealListener(player));
    }

    void disblePlayer(Player player)
    {
        HealListener healListner = players.remove(player.getObjectId());
        if (healListner != null)
            healListner.stop();
    }

    static boolean checkRestricts(Player player)
    {
        if (player.isInOlympiadMode())
            return false;

        List<Event> singleMatchEvents = player.getEvents().stream()
                .filter(t-> t instanceof SingleMatchEvent)
                .collect(Collectors.toList());

        if (!singleMatchEvents.isEmpty() && singleMatchEvents.stream().noneMatch(t-> t.canUseAcp(player)))
            return false;

		// нельзя использовать
		for (int skillId : Config.ACP_RESTRICT_WITH_EFFECTS)
		    if (player.getAbnormalList().containsEffects(skillId))
		        return false;

        return true;
    }

    static boolean checkNesesary(Player player)
    {
        return player.getVarBoolean(acpcp_enable, false) || player.getVarBoolean(acphp_enable, false) || player.getVarBoolean(acpmp_enable, false);
    }
}
