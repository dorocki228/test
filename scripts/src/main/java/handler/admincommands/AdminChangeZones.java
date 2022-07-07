package handler.admincommands;

import gve.zones.GveZoneManager;
import l2s.gameserver.model.Player;

/**
 * @author KRonst
 */
public class AdminChangeZones extends ScriptAdminCommand {
    @Override
    public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar) {
        Commands command = (Commands) comm;
        if(!activeChar.getPlayerAccess().Menu)
            return false;
        switch (command) {
            case admin_change_zones: {
                GveZoneManager.getInstance().changeZones();
                break;
            }
        }
        return true;
    }

    @Override
    public Enum<?>[] getAdminCommandEnum() {
        return Commands.values();
    }
    
    private enum Commands {
        admin_change_zones
    }
}
