package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import org.apache.commons.lang3.ArrayUtils;

public class ExCursedWeaponList extends L2GameServerPacket
{
    private int[] cursedWeapon_ids;

    public ExCursedWeaponList(Creature activeChar)
    {
        BREvent event = activeChar.getEvent(BREvent.class);
        if(event == null)
            cursedWeapon_ids = ArrayUtils.EMPTY_INT_ARRAY;
        else
            cursedWeapon_ids = event.getCircleIds();
    }

    @Override
    protected final void writeImpl()
    {
        writeDD(cursedWeapon_ids, true);
    }
}