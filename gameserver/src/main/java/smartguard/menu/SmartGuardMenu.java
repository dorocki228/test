package smartguard.menu;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import smartguard.core.manager.admin.GmCommandHandler;
import smartguard.integration.SmartPlayer;

public class SmartGuardMenu extends GmCommandHandler implements IAdminCommandHandler
{
    @Override
    public boolean useAdminCommand(Enum comm, String[] wordList, String commands, Player player) {
        try
        {
            String[] strings = commands.split(" ");

            handle(new SmartPlayer(player), strings[0], strings);
            return true;

        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return getEnumCommands();
    }
}
