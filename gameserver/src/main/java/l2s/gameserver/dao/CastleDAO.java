package l2s.gameserver.dao;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CastleDAO {
    private static final Logger _log;
    private static final CastleDAO _instance;
    public static final String SELECT_SQL_QUERY = "SELECT treasury, siege_date, last_siege_date, owner_id, own_date, side FROM castle WHERE id=? LIMIT 1";
    public static final String UPDATE_SQL_QUERY = "UPDATE castle SET treasury=?, siege_date=?, last_siege_date=?, owner_id=?, own_date=?, side=? WHERE id=?";

    public static CastleDAO getInstance() {
        return _instance;
    }

    public void select(Castle castle) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT treasury, siege_date, last_siege_date, owner_id, own_date, side FROM castle WHERE id=? LIMIT 1");
            statement.setInt(1, castle.getId());
            rset = statement.executeQuery();
            if (rset.next()) {
                castle.setTreasury(rset.getLong("treasury"));
                castle.getSiegeDate().setTimeInMillis(rset.getLong("siege_date") * 1000L);
                castle.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date") * 1000L);
                castle.setOwner(ClanTable.getInstance().getClan(rset.getInt("owner_id")));
                castle.getOwnDate().setTimeInMillis(rset.getLong("own_date") * 1000L);
                castle.setResidenceSide(ResidenceSide.VALUES[rset.getInt("side")], true);
            }
        } catch (Exception e) {
            _log.error("CastleDAO.select(Castle):" + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void update(Castle residence) {
        if (!residence.getJdbcState().isUpdatable())
            return;

        residence.setJdbcState(JdbcEntityState.STORED);
        update0(residence);
    }

    private void update0(Castle castle) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE castle SET treasury=?, siege_date=?, last_siege_date=?, owner_id=?, own_date=?, side=? WHERE id=?");
            statement.setLong(1, castle.getTreasury());
            statement.setInt(2, (int) (castle.getSiegeDate().getTimeInMillis() / 1000L));
            statement.setInt(3, (int) (castle.getLastSiegeDate().getTimeInMillis() / 1000L));
            statement.setInt(4, castle.getOwner() == null ? 0 : castle.getOwner().getClanId());
            statement.setInt(5, (int) (castle.getOwnDate().getTimeInMillis() / 1000L));
            statement.setInt(6, castle.getResidenceSide().ordinal());
            statement.setInt(7, castle.getId());
            statement.execute();
        } catch (Exception e) {
            _log.warn("CastleDAO#update0(Castle): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    static {
        _log = LoggerFactory.getLogger(CastleDAO.class);
        _instance = new CastleDAO();
    }
}
