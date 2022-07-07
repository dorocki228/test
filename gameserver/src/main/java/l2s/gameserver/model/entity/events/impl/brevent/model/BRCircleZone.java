package l2s.gameserver.model.entity.events.impl.brevent.model;

import l2s.commons.geometry.Point2D;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.brevent.enums.BRCircleColor;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.network.l2.s2c.ExCursedWeaponLocation;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Java-man
 * @since 20.08.2018
 */
public class BRCircleZone extends Zone
{
    private static final Logger LOGGER = LogManager.getLogger(BRCircle.class);

    private final BRCircle circle;

    private BRCircleZone(ZoneTemplate template, BRCircle circle)
    {
        super(template);
        this.circle = circle;
    }

    public static BRCircleZone createZone(BRCircle circle, Reflection reflection)
    {
        StatsSet set = new StatsSet();
        set.set("name", "circle_zone_" + circle.getColor().toString().toLowerCase() + "_" + circle.getStageNumber());
        set.set("type", Zone.ZoneType.dummy);
        set.set("territory", new Territory().add(circle));

        var zone = new BRCircleZone(new ZoneTemplate(set), circle);
        zone.setReflection(reflection);

        return zone;
    }

    public Point2D getCenter() {
        return circle.getCenter();
    }

    public BRCircle getCircle()
    {
        return circle;
    }

    public int getCircleId()
    {
        return circle.getId();
    }

    public boolean checkIfInZone(EventPlayerObject playerObject)
    {
        return playerObject.getPlayer() != null && checkIfInZone(playerObject.getPlayer());
    }

    public BRCircleZone getNextCircle(BRCircleColor color, int stage, int radius) {
        for(int i = 0; i < 200; i++)
        {
            var tempCircle = new BRCircle(getCenter().getX(), getCenter().getY(), circle.getRadius() - radius, color, stage);
            var nextCircleCenter = new Territory().add(tempCircle).getRandomLoc(getReflection().getGeoIndex());
            var nextCircle = new BRCircle(nextCircleCenter.getX(), nextCircleCenter.getY(), radius, color, stage);

            if(circle.isInside(nextCircle))
                return createZone(nextCircle, getReflection());
        }

        LOGGER.info("Too much attempts to find next circle.");

        var nextCircleCenter = getTerritory().getRandomLoc(getReflection().getGeoIndex());
        var nextCircle = new BRCircle(nextCircleCenter.getX(), nextCircleCenter.getY(), radius, color, stage);
        return createZone(nextCircle, getReflection());
    }

    public void showCircle(EventPlayerObject player) {
        player.ifPlayerExist(temp -> temp.sendPacket(createCircleShowPacket(true)));
    }

    public void hideCircle(EventPlayerObject player) {
        player.ifPlayerExist(temp -> temp.sendPacket(createCircleShowPacket(false)));
    }

    public void addRadar(EventPlayerObject player) {
        player.ifPlayerExist(temp ->
                temp.addRadar(circle.getCenter().getX(), circle.getCenter().getY(), circle.getZmin()));
    }

    public void removeRadar(EventPlayerObject player) {
        player.ifPlayerExist(temp -> temp.removeRadar());
    }

    private ExCursedWeaponLocation createCircleShowPacket(boolean activate) {
        return circle.createCircleShowPacket(activate);
    }
}
