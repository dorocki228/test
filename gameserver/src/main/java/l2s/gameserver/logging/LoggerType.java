package l2s.gameserver.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Java-man
 * @since 22.04.2018
 */
public enum LoggerType
{
    GAME("Game"),
    DEBUG("Debug"),
    CHAT("Chat"),
    ABUSE("Abuse"),
    ITEM("Item"),
    ADMIN_ACTIONS("AdminActions"),
    ADMIN_JOBS("AdminJobs"),
    MULTISELL("Multisell"),
    OLYMPIAD("Olympiad"),
    ILLEGAL_ACTIONS("IllegalActions"),
    BOSSES("Bosses"),
    TREASURY("Treasury"),
    RENAMES("Renames"),
    ENCHANTS("Enchants"),
    CLAN("Clan"),
    SERVICES("Services"),
    TIME_COUNTER("TimeCounter");

    private final Logger logger;

    LoggerType(String loggerName)
    {
        logger = LogManager.getLogger(loggerName);
    }

    public Logger getLogger()
    {
        return logger;
    }
}
