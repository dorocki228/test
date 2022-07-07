package l2s.gameserver.model.entity.events.impl.pvparena;

import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.Objects;

public class PvpArenaZone {
    private final ZoneObject zoneObject;
    private final List<Location> points;

    public PvpArenaZone(ZoneObject zoneObject, List<Location> points) {
        Objects.requireNonNull(zoneObject);
        Objects.requireNonNull(points);
        this.zoneObject = zoneObject;
        this.points = points;
    }

    public ZoneObject getZoneObject() {
        return zoneObject;
    }

    public List<Location> getPoints() {
        return points;
    }
}
