package l2s.gameserver.statistics;

import java.time.LocalDateTime;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class Statistics
{
    private final LocalDateTime dateTime;
    private final String source;

    private final long accountsTotalCount;
    private final long accountsLoggedInAtLeastOneTimeCount;
    private final long accountsLoggedInAtLeastThreeTimesCount;
    private final long donatorMailCount;

    private final double donateAmount;

    private final double registeredToLoggedInRatio;
    private final double activeToRegisteredRatio;
    private final double activeToLoggedInRatio;

    private final double donateOnRegisteredAmount;
    private final double donateOnActiveAmount;

    private final double donatorToActivePercentage;

    private final double averageDonate;

    public Statistics(LocalDateTime dateTime, String source, long accountsTotalCount,
                      long accountsLoggedInAtLeastOneTimeCount, long accountsLoggedInAtLeastThreeTimesCount,
                      long donatorMailCount, double donateAmount, double registeredToLoggedInRatio,
                      double activeToRegisteredRatio, double activeToLoggedInRatio,
                      double donateOnRegisteredAmount, double donateOnActiveAmount,
                      double donatorToActivePercentage, double averageDonate)
    {
        this.dateTime = dateTime;
        this.source = source;
        this.accountsTotalCount = accountsTotalCount;
        this.accountsLoggedInAtLeastOneTimeCount = accountsLoggedInAtLeastOneTimeCount;
        this.accountsLoggedInAtLeastThreeTimesCount = accountsLoggedInAtLeastThreeTimesCount;
        this.donatorMailCount = donatorMailCount;
        this.donateAmount = donateAmount;
        this.registeredToLoggedInRatio = registeredToLoggedInRatio;
        this.activeToRegisteredRatio = activeToRegisteredRatio;
        this.activeToLoggedInRatio = activeToLoggedInRatio;
        this.donateOnRegisteredAmount = donateOnRegisteredAmount;
        this.donateOnActiveAmount = donateOnActiveAmount;
        this.donatorToActivePercentage = donatorToActivePercentage;
        this.averageDonate = averageDonate;
    }

    public Statistics(LocalDateTime dateTime, String source, double accountsTotalCount,
                      double accountsLoggedInAtLeastOneTimeCount, double accountsLoggedInAtLeastThreeTimesCount,
                      double donatorMailCount, double donateAmount, double registeredToLoggedInRatio,
                      double activeToRegisteredRatio, double activeToLoggedInRatio,
                      double donateOnRegisteredAmount, double donateOnActiveAmount,
                      double donatorToActivePercentage, double averageDonate)
    {
        this.dateTime = dateTime;
        this.source = source;
        this.accountsTotalCount = (long) accountsTotalCount;
        this.accountsLoggedInAtLeastOneTimeCount = (long) accountsLoggedInAtLeastOneTimeCount;
        this.accountsLoggedInAtLeastThreeTimesCount = (long) accountsLoggedInAtLeastThreeTimesCount;
        this.donatorMailCount = (long) donatorMailCount;
        this.donateAmount = donateAmount;
        this.registeredToLoggedInRatio = registeredToLoggedInRatio;
        this.activeToRegisteredRatio = activeToRegisteredRatio;
        this.activeToLoggedInRatio = activeToLoggedInRatio;
        this.donateOnRegisteredAmount = donateOnRegisteredAmount;
        this.donateOnActiveAmount = donateOnActiveAmount;
        this.donatorToActivePercentage = donatorToActivePercentage;
        this.averageDonate = averageDonate;
    }

    public LocalDateTime getDateTime()
    {
        return dateTime;
    }

    public String getSource()
    {
        return source;
    }

    public long getAccountsTotalCount()
    {
        return accountsTotalCount;
    }

    public long getAccountsLoggedInAtLeastOneTimeCount()
    {
        return accountsLoggedInAtLeastOneTimeCount;
    }

    public long getAccountsLoggedInAtLeastThreeTimesCount()
    {
        return accountsLoggedInAtLeastThreeTimesCount;
    }

    public long getDonatorMailCount()
    {
        return donatorMailCount;
    }

    public double getDonateAmount()
    {
        return donateAmount;
    }

    public double getRegisteredToLoggedInRatio()
    {
        return registeredToLoggedInRatio;
    }

    public double getActiveToRegisteredRatio()
    {
        return activeToRegisteredRatio;
    }

    public double getActiveToLoggedInRatio()
    {
        return activeToLoggedInRatio;
    }

    public double getDonateOnRegisteredAmount()
    {
        return donateOnRegisteredAmount;
    }

    public double getDonateOnActiveAmount()
    {
        return donateOnActiveAmount;
    }

    public double getDonatorToActivePercentage()
    {
        return donatorToActivePercentage;
    }

    public double getAverageDonate()
    {
        return averageDonate;
    }
}
