package l2s.authserver.accounts;

public class AccountStatistics
{
    private final String login;
    private final String email;
    private final String source;
    private final int loggedInCount;

    public AccountStatistics(String login, String email, String source, int loggedInCount)
    {
        this.login = login;
        this.email = email;
        this.source = source;
        this.loggedInCount = loggedInCount;
    }

    public String getLogin()
    {
        return login;
    }

    public String getEmail()
    {
        return email;
    }

    public String getSource()
    {
        return source;
    }

    public int getLoggedInCount()
    {
        return loggedInCount;
    }
}
