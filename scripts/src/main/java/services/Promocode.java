package services;

import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.service.PromocodeService;

/**
 * @author Mangol
 */
public class Promocode
{
    @Bypass("promocode:use")
    public void promocodeUse(Player player, NpcInstance npc, String[] arg)
    {
        if(player == null)
            return;
        if(arg.length != 1)
            return;
        PromocodeService.getInstance().use(player, arg[0]);
    }
}
