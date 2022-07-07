package l2s.gameserver.handler.admincommands.impl;

import gve.zones.GveZoneManager;
import gve.zones.model.GveZoneStatus;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.network.l2.components.HtmlMessage;

/**
 * @author Java-man
 * @since 12.04.2018
 */
public class AdminGve implements IAdminCommandHandler
{
    @Override
    public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
    {
        Commands command = (Commands) comm;

        switch(command)
        {
            case admin_gve_zones:
            {
                HtmlMessage htmlMessage = new HtmlMessage(5).setFile("admin/gve/zones.htm");
                htmlMessage.replace("%zones%", GveZoneManager.getInstance().getZonesHtml());
                activeChar.sendPacket(htmlMessage);

                break;
            }
            case admin_gve_zone_set_active_count:
            {
                if(wordList.length < 3)
                    return false;

                if(!GveZoneManager.getInstance().canManageZones())
                {
                    activeChar.sendMessage("Can't manage zones now.");
                    return false;
                }

                Zone.ZoneType zoneType = Zone.ZoneType.valueOf(wordList[1]);
                int count = Integer.parseInt(wordList[2]);
                if(count < 0)
                {
                    activeChar.sendMessage("Active zone count can't be negative.");
                    return false;
                }
                GveZoneManager.getInstance().setActiveZoneCount(zoneType, count);

                HtmlMessage htmlMessage = new HtmlMessage(5).setFile("admin/gve/zones.htm");
                htmlMessage.replace("%zones%", GveZoneManager.getInstance().getZonesHtml());
                activeChar.sendPacket(htmlMessage);

                break;
            }
            case admin_gve_zone_set_status:
            {
                if(wordList.length < 3)
                    return false;

                if(!GveZoneManager.getInstance().canManageZones())
                {
                    activeChar.sendMessage("Can't manage zones now.");
                    return false;
                }

                GveZoneStatus status = GveZoneStatus.valueOf(wordList[2]);
                GveZoneManager.getInstance().setZoneStatus(wordList[1], status);

                HtmlMessage htmlMessage = new HtmlMessage(5).setFile("admin/gve/zones.htm");
                htmlMessage.replace("%zones%", GveZoneManager.getInstance().getZonesHtml());
                activeChar.sendPacket(htmlMessage);

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
        admin_gve_zones,
        admin_gve_zone_set_active_count,
        admin_gve_zone_set_status
    }
}
