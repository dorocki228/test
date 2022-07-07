package l2s.gameserver.statistics;

public class DelayedItemsStatistics
{
    private String login;
    private double donateAmount;

    public DelayedItemsStatistics(String login, double donateAmount)
    {
        this.login = login;
        this.donateAmount = donateAmount;
    }

    public String getLogin()
    {
        return login;
    }

    public double getDonateAmount()
    {
        return donateAmount;
    }
}
