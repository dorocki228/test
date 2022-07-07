package l2s.gameserver.model.base;

/**
 * @author Java-man
 * @since 13.06.2018
 */
public enum AdenaModifier
{
    NONE(0.0D),
    FRACTION_BONUS(0.15D),
    FRACTION_PENALTY(-0.15D);

    private final double value;

    AdenaModifier(double value)
    {
        this.value = value;
    }

    public double getValue()
    {
        return value;
    }
}