package l2s.gameserver.logging.message;

/**
 * @author Java-man
 * @since 22.04.2018
 */
public class AdminJobLogMessage extends AbstractLogMessage
{
    private final String type;
    private final String requester;
    private final String text;

    public AdminJobLogMessage(String type, String requester, String text)
    {
        this.type = type;
        this.requester = requester;
        this.text = text;
    }

    @Override
    protected void formatMessage(StringBuilder builder)
    {
        builder.append('[');
        builder.append(type);
        builder.append(']');
        builder.append(' ');
        builder.append('[');
        builder.append(requester);
        builder.append(']');
        builder.append(' ');
        builder.append(text);
    }

    @Override
    public Object[] getParameters()
    {
        return new Object[] {
                type, requester, text
        };
    }
}
