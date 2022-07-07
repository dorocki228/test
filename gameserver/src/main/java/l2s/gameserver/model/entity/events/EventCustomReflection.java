package l2s.gameserver.model.entity.events;

import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.model.entity.Reflection;

public class EventCustomReflection extends Reflection {

    public EventCustomReflection(int instantZoneId) {
        super();
        init(InstantZoneHolder.getInstance().getInstantZone(instantZoneId));
    }

    public EventCustomReflection(int id, int instantZoneId) {
        super(id);
        init(InstantZoneHolder.getInstance().getInstantZone(instantZoneId));
    }

    @Override
    public void startCollapseTimer(long timeInMillis) {
    }

    public void startCollapse(long timeInMillis) {
        super.startCollapseTimer(timeInMillis);
    }
}