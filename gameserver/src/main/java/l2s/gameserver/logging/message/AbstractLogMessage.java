package l2s.gameserver.logging.message;

import com.cronutils.utils.StringUtils;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.StringBuilderFormattable;

/**
 * @author Java-man
 * @since 22.04.2018
 */
public abstract class AbstractLogMessage implements Message, StringBuilderFormattable
{
    private String formattedMessage;

    @Override
    public String getFormattedMessage()
    {
        if(formattedMessage == null)
        {
            StringBuilder builder = new StringBuilder(255);
            formatTo(builder);
            formattedMessage = builder.toString();
        }
        return formattedMessage;
    }

    @Override
    public String getFormat()
    {
        return StringUtils.EMPTY;
    }

    @Override
    public Throwable getThrowable()
    {
        return null;
    }

    @Override
    public void formatTo(StringBuilder builder)
    {
        if(formattedMessage != null)
        {
            builder.append(formattedMessage);
        }
        else
        {
            formatMessage(builder);
        }
    }

    protected abstract void formatMessage(StringBuilder builder);
}