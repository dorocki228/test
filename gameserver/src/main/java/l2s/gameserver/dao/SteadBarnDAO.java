package l2s.gameserver.dao;

import l2s.gameserver.component.farm.Harvest;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.farm.SteadBarnManager;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SteadBarnDAO {
    private static final SteadBarnDAO instance = new SteadBarnDAO();
    private static final String SELECT_QUERY = "SELECT * FROM stead_barn";
    private static final String DELETE_QUERY = "DELETE FROM stead_barn WHERE owner = ?";
    private static final String UPDATE_QUERY = "INSERT INTO stead_barn (owner, id, count) VALUES (?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count)+count";
    private static final JdbcTemplate jdbc = DatabaseFactory.getInstance().getJdbcTemplate();

    public static SteadBarnDAO getInstance() {
        return instance;
    }

    public void select() {
        jdbc.query(SELECT_QUERY, rs -> {
            int owner = rs.getInt("owner");
            int id = rs.getInt("id");
            long count = rs.getLong("count");
            Harvest harvest = new Harvest(id, count, owner, false);
            SteadBarnManager.getInstance().addHarvest(harvest);
        });
    }

    public void clear(int owner) {
        jdbc.update(DELETE_QUERY, owner);
    }

    public void save() {
        List<Harvest> list = SteadBarnManager.getInstance().getAllFresh();
        jdbc.batchUpdate(UPDATE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Harvest harvest = list.get(i);
                ps.setInt(1, harvest.getOwner());
                ps.setInt(2, harvest.getId());
                ps.setLong(3, harvest.getCount());
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
    }
}