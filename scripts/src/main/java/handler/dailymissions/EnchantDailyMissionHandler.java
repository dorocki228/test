package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnchantListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author Java-man
 * @since 13.01.2019
 */
public class EnchantDailyMissionHandler extends ProgressDailyMissionHandler
{
    private class HandlerListeners implements OnPlayerEnchantListener
    {
        @Override
        public void onEnchant(Player player, ItemInstance item)
        {
            progressMission(player, 1, dailyMissionTemplate ->
                    item.getEnchantLevel() == Integer.parseInt(dailyMissionTemplate.getValue()));
        }
    }

    private final HandlerListeners listeners = new HandlerListeners();

    @Override
    public CharListener getListener()
    {
        return listeners;
    }
}