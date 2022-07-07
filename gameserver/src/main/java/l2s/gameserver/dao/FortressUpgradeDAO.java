package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FortressUpgradeDAO {
    private static final Logger _log = LoggerFactory.getLogger(FortressUpgradeDAO.class);

    private static final FortressUpgradeDAO _instance = new FortressUpgradeDAO();

    public static final String SELECT_SQL_QUERY = "SELECT * FROM fortress_upgrade WHERE id = ?";
    public static final String UPDATE_SQL_QUERY = "UPDATE fortress_upgrade SET ? = ? WHERE id = ?";

    public static FortressUpgradeDAO getInstance() {
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
                fortress.updateUpgrades(UpgradeType.CRYSTAL, rset.getInt("crystal"));
                fortress.updateUpgrades(UpgradeType.GATE, rset.getInt("gate"));
                fortress.updateUpgrades(UpgradeType.GUARD, rset.getInt("guard"));
                fortress.updateUpgrades(UpgradeType.GUARDIAN, rset.getInt("guardian"));
            }
        } catch (Exception e) {
            _log.error("FortressUpgradeDAO.select(Fortress): id -> " + fortress.getId() + " | " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void update(final Fortress fortress, final UpgradeType type, final int level) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            statement.setString(1, type.name().toLowerCase());
            statement.setInt(2, level);
            statement.setInt(3, fortress.getId());
            statement.execute();
        } catch (Exception e) {
            _log.error("FortressUpgradeDAO.update(Fortress,UpgradeType,int): id -> " + fortress.getId() + " | " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }

        fortress.updateUpgrades(type, level);
    }
}
