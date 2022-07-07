package l2s.gameserver.dao;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FortressDAO {
    private static final Logger _log = LoggerFactory.getLogger(FortressDAO.class);

    private static final FortressDAO _instance = new FortressDAO();

    public static final String SELECT_SQL_QUERY = "SELECT * FROM fortress WHERE id = ?";
    public static final String UPDATE_SQL_QUERY
            = "UPDATE fortress SET owner_id=?,fraction=?,last_siege_date=? WHERE id=?";

    public static FortressDAO getInstance() {
        return _instance;
    }

    public void select(Fortress fortress) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, fortress.getId());
            rset = statement.executeQuery();
            if (rset.next()) {
                fortress.setFraction(Fraction.VALUES_WITH_NONE[rset.getInt("fraction")]);
                fortress.setOwner(ClanTable.getInstance().getClan(rset.getInt("owner_id")));
                fortress.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date") * 1000L);
            }
        } catch (Exception e) {
            _log.error("FortressDAO.select(Fortress):" + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void update(Fortress fortress) {
        if (!fortress.getJdbcState().isUpdatable()) {
            return;
        }
        fortress.setJdbcState(JdbcEntityState.STORED);
        update0(fortress);
    }

    private void update0(Fortress fortress) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            statement.setInt(1, fortress.getOwnerId());
            statement.setInt(2, fortress.getFraction().ordinal());
            statement.setInt(3, (int) (fortress.getLastSiegeDate().getTimeInMillis() / 1000L));
            statement.setInt(4, fortress.getId());
            statement.execute();
        } catch (Exception e) {
            _log.warn("FortressDAO#update0(Fortress): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }
}