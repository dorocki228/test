package l2s.gameserver.model.entity.events.impl.brevent.util;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.items.ItemInstance;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author : Nami
 * @date : 08.08.2018
 * @time : 19:21
 * <p/>
 * Используется, если необходимо вернуть потерянные предметы
 * вследствие ошибки на ивенте
 */
public class ItemRestorer {
    private static final Logger LOGGER = getLogger(ItemRestorer.class);

    public static void restoreItems(int ownerId) {
        ItemInstance item = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, variation_stone_id, variation1_id, variation2_id FROM items WHERE owner_id = ?");
            statement.setInt(1, ownerId - 100000000); // у бота, хранящего предметы, ID меньше ровно на 1ккк
            rset = statement.executeQuery();
            while (rset.next()) {
                storeItem(rset, ownerId);   // а вот добавляем итемы уже самому персонажу
            }
        } catch (SQLException e) {
            LOGGER.error("Error while restoring item for character: " + ownerId, e);
            return;
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public static void storeItem(ResultSet rset, int ownerId) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO items (object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, variation_stone_id, variation1_id, variation2_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            statement.setInt(1, rset.getInt(1));
            statement.setInt(2, ownerId);
            statement.setInt(3, rset.getInt(3));
            statement.setLong(4, rset.getLong(4));
            statement.setInt(5, rset.getInt(5));
            statement.setString(6, rset.getString(6));
            statement.setInt(7, rset.getInt(7));
            statement.setInt(8, rset.getInt(8));
            statement.setInt(9, rset.getInt(9));
            statement.setInt(10, rset.getInt(10));
            statement.setInt(11, rset.getInt(11));
            statement.setInt(12, rset.getInt(12));
            statement.setInt(13, rset.getInt(13));
            statement.setInt(14, rset.getInt(14));
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("" + e, e);
            return;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }
}
