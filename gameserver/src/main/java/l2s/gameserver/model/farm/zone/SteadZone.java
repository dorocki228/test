package l2s.gameserver.model.farm.zone;

import l2s.gameserver.model.Zone;
import l2s.gameserver.model.farm.Stead;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SteadZone extends Zone {
    private static final Logger log = LogManager.getLogger(SteadZone.class);
    private Stead stead;
    private List<Location> points;

    public SteadZone(ZoneTemplate template) {
        super(template);
        points = new ArrayList<>();
    }

    public void setStead(Stead stead) {
        this.stead = stead;
    }

    public Stead getStead() {
        return stead;
    }

    public void addPoint(Location location) {
        points.add(location);
    }

    public List<Location> getPoints() {
        return points;
    }
}