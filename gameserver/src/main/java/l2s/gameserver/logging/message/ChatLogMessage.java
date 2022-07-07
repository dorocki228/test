package l2s.gameserver.logging.message;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;

/**
 * @author Java-man
 * @since 22.04.2018
 */
public class ChatLogMessage extends AbstractLogMessage
{
    private final ChatType type;
    private final Player player;
    private final Player target;
    private final String text;

    public ChatLogMessage(ChatType type, Player player, Player target, String text)
    {
        this.type = type;
        this.player = player;
        this.target = target;
        this.text = text;
    }

    @Override
    public Object[] getParameters()
    {
        return new Object[] {
                type, player, target, text
        };
    }

    @Override
    public void formatMessage(StringBuilder builder)
    {
        builder.append(type);
        builder.append(' ');
        builder.append('[');
        builder.append(player);
        if(target != null)
        {
            builder.append(" -> ");
            builder.append(target);
        }
        builder.append(']');
        builder.append(' ');
        builder.append(text);
    }
}
