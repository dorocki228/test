package l2s.gameserver.model.entity.events.impl.brevent.model;

import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;

/**
 * @author : Nami
 * @date : 19.06.2018
 * @time : 21:59
 * <p/>
 */
public interface IBREventHandler {
    EBREventState getState();

    boolean invoke(BREvent event, EventPlayerObject playerObject, String... args);
}
