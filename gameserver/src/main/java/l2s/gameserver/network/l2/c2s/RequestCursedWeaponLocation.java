package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;

public class RequestCursedWeaponLocation extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {}

    @Override
    protected void runImpl()
    {
        Creature activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;


        BREvent event = activeChar.getEvent(BREvent.class);
        if(event == null)
            return;

        Player player = activeChar.getPlayer();
        if(player == null)
            return;

        var playerObject = event.getEventPlayerObject(player);
        playerObject.ifPresent(temp ->
        {
            event.showSafeZoneCircle(temp);
            event.showNextSafeZoneCircle(temp);
        });
    }
}