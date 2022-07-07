package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.component.fraction.FractionTreasure;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.Fraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FractionTreasureDAO {
    private static final Logger _log = LoggerFactory.getLogger(FortressUpgradeDAO.class);

    private static final FractionTreasureDAO _instance = new FractionTreasureDAO();
    public static final String SELECT_SQL_QUERY = "SELECT * FROM fraction_data";
    public static final String UPDATE_SQL_QUERY = "UPDATE fraction_data SET treasure = ? WHERE fraction = ?";

    public static FractionTreasureDAO getInstance() {
        return _instance;
    }

    public void init() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            rset = statement.executeQuery();
            while (rset.next()) {
                Fraction fraction = Fraction.valueOf(rset.getString("fraction"));
                FractionTreasure.getInstance().init(fraction, rset.getLong("treasure"));
            }
        } catch (Exception e) {
            _log.error("FractionTreasureDAO.init(): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }


    public void update(Fraction fraction, long treasure) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            statement.setLong(1, treasure);
            statement.setString(2, fraction.name());
            statement.execute();
        } catch (Exception e) {
            _log.error("FortressUpgradeDAO.update(Fraction,long): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }
}