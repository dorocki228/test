package l2s.gameserver.model.entity.events.impl.brevent.handlers;

import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.impl.brevent.model.IBREventHandler;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;

/**
 * @author : Nami
 * @date : 19.06.2018
 * @time : 22:14
 * <p/>
 */
public class BRRegistration implements IBREventHandler {
    @Override
    public EBREventState getState() {
        return EBREventState.REGISTRATION;
    }

    @Override
    public boolean invoke(BREvent event, EventPlayerObject playerObject, String... args) {
        return true;
    }
}
