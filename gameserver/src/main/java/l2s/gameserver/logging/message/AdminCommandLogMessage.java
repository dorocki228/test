package l2s.gameserver.logging.message;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;

/**
 * @author Java-man
 * @since 22.04.2018
 */
public class AdminCommandLogMessage extends AbstractLogMessage
{
    private final Player player;
    private final GameObject target;
    private final String command;
    private final boolean success;

    public AdminCommandLogMessage(Player player, GameObject target, String command, boolean success)
    {
        this.player = player;
        this.target = target;
        this.command = command;
        this.success = success;
    }

    @Override
    protected void formatMessage(StringBuilder builder)
    {
        builder.append(player);
        if(target != null)
        {
            builder.append(" -> ");
            builder.append(target);
        }
        builder.append(' ');
        builder.append(command);
        builder.append(' ');
        builder.append(success ? "SUCCESS" : "FAIL");
    }

    @Override
    public Object[] getParameters()
    {
        return new Object[] {
                player, target, command, success
        };
    }
}
