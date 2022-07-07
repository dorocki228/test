package l2s.gameserver.service;

import l2s.gameserver.database.DatabaseFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author KRonst
 */
public class PaidActionsStatsService {
    private static final PaidActionsStatsService INSTANCE = new PaidActionsStatsService();
    private final JdbcTemplate jdbc;

    private PaidActionsStatsService() {
        this.jdbc = DatabaseFactory.getInstance().getJdbcTemplate();
    }

    public static PaidActionsStatsService getInstance() {
        return INSTANCE;
    }

    public void updateStats(PaidActionType type, long value) {
        jdbc.update("INSERT INTO paid_actions_stats (type, value) VALUES (?,?) ON DUPLICATE KEY UPDATE value = value + VALUES(value)", type.actionName, value);
    }

    public enum PaidActionType {
        RAID_BOSS_TELEPORT("Raid Boss Teleport"),
        GATEKEEPER_TELEPORT("Gatekeeper Teleport"),
        TELEPORT_BYPASS("Teleport Bypass"),
        CASINO("Casino Commission");

        private final String actionName;

        PaidActionType(String actionName) {
            this.actionName = actionName;
        }
    }
}
