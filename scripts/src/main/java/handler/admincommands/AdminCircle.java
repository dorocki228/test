package handler.admincommands;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.brevent.enums.BRCircleColor;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRCircle;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRCircleZone;
import l2s.gameserver.network.l2.s2c.ExCursedWeaponLocation;

public class AdminCircle extends ScriptAdminCommand
{
    private final Table<Player, ExCursedWeaponLocation, Zone> zones = HashBasedTable.create();

    @Override
    public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
    {
        Commands command = (Commands) comm;
        if(!activeChar.getPlayerAccess().Menu)
            return false;
        switch(command)
        {
            case admin_circle:
            {
                int stage = Integer.parseInt(wordList[1]);
                int radius = Integer.parseInt(wordList[1]);

                var areaCircle = new BRCircle(activeChar.getX(), activeChar.getY(), radius, BRCircleColor.BLUE, stage);
                var zone = BRCircleZone.createZone(areaCircle, activeChar.getReflection());

                var packet = zone.getCircle().createCircleShowPacket(true);
                var packetDelete = zone.getCircle().createCircleShowPacket(false);
                activeChar.sendPacket(packet);

                zone.setActive(true);
                zone.setReflection(activeChar.getReflection());

                zones.put(activeChar, packetDelete, zone);

                break;
            }
            case admin_circle_damage:
            {
                int stage = Integer.parseInt(wordList[1]);
                int radius = Integer.parseInt(wordList[2]);

                var areaCircle = new BRCircle(activeChar.getX(), activeChar.getY(), radius, BRCircleColor.BLUE, stage);
                var zone = BRCircleZone.createZone(areaCircle, activeChar.getReflection());

                var packet = zone.getCircle().createCircleShowPacket(true);
                var packetDelete = zone.getCircle().createCircleShowPacket(false);
                activeChar.sendPacket(packet);

                zone.setActive(true);
                zone.setReflection(activeChar.getReflection());

                zones.put(activeChar, packetDelete, zone);

                break;
            }
            case admin_circle_remove:
            {
                zones.row(activeChar)
                        .forEach((packet, zone) ->
                        {
                            activeChar.sendPacket(packet);
                            zone.setActive(false);
                        });
                zones.row(activeChar).clear();

                break;
            }
            case admin_circle_remove_all:
            {
                zones.cellSet().stream()
                        .filter(cell -> cell.getRowKey() != null && cell.getColumnKey() != null && cell.getValue() != null)
                        .forEach(cell ->
                        {
                            cell.getRowKey().sendPacket(cell.getColumnKey());
                            cell.getValue().setActive(false);
                        });
                zones.clear();

                break;
            }
        }
        return true;
    }

    @Override
    public Enum<?>[] getAdminCommandEnum()
    {
        return Commands.values();
    }

    private enum Commands
    {
        admin_circle,
        admin_circle_damage,
        admin_circle_remove,
        admin_circle_remove_all
    }
}
