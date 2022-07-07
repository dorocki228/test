package l2s.gameserver.model.entity.events.impl.brevent.model;

import l2s.commons.geometry.Circle;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.impl.brevent.enums.BRCircleColor;
import l2s.gameserver.network.l2.s2c.ExCursedWeaponLocation;
import l2s.gameserver.utils.Location;

/**
 * @author Java-man
 */
public class BRCircle extends Circle {
    private final int id;
    private BRCircleColor color;
    private final int stageNumber;

    private final ExCursedWeaponLocation addPacket;
    private final ExCursedWeaponLocation removePacket;

    public BRCircle(int x, int y, int radius, BRCircleColor color, int stageNumber) {
        super(x, y, radius);
        this.color = color;
        this.stageNumber = stageNumber;
        id = -(110000 + stageNumber * 10 + (color.ordinal() + 1));

        var location = new Location(getCenter().getX(), getCenter().getY(), 0);
        addPacket = new ExCursedWeaponLocation(new ExCursedWeaponLocation.CursedWeaponInfo(location, id, 1));
        removePacket = new ExCursedWeaponLocation(new ExCursedWeaponLocation.CursedWeaponInfo(location, id, 0));

        setZmax(World.MAP_MAX_Z);
        setZmin(World.MAP_MIN_Z);
    }

    public BRCircle(BRCircle circle, BRCircleColor color, int stageNumber) {
        this(circle.getCenter().getX(), circle.getCenter().getY(), circle.getRadius(), color, stageNumber);
    }

    public int getId()
    {
        return id;
    }

    public BRCircleColor getColor()
    {
        return color;
    }

    public int getStageNumber()
    {
        return stageNumber;
    }

    public ExCursedWeaponLocation createCircleShowPacket(boolean activate) {
        return activate ? addPacket : removePacket;
    }
}
