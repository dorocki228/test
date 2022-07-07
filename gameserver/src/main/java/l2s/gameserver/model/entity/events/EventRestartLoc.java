package l2s.gameserver.model.entity.events;

import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.Location;

public class EventRestartLoc {
    private final Location loc;
    private final Reflection reflection;

    public EventRestartLoc(Location loc) {
        this(loc, null);
    }

    public EventRestartLoc(Location loc, Reflection reflection) {
        this.loc = loc;
        this.reflection = reflection;
    }

    public Location getLoc() {
        return loc;
    }

    public Reflection getReflection() {
        return reflection;
    }
}
