package l2s.gameserver.config.type;

/**
 * @author Java-man
 * @since 27.12.2018
 */
public class ValueOrPercentage
{
    private final double value;
    private final boolean percentage;

    public ValueOrPercentage(String text) {
        percentage = text.endsWith("%");
        value = Integer.parseInt(percentage ? text.substring(0, text.length() - 1) : text);
    }

    public double getValue()
    {
        return value;
    }

    public boolean isPercentage()
    {
        return percentage;
    }
}
