package as.dao;

import l2s.gameserver.database.DatabaseFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Mangol
 */
public class CharacterDAO
{
    private static final Logger LOGGER = LogManager.getLogger(CharacterDAO.class);

    private static JdbcTemplate jdbc = DatabaseFactory.getInstance().getJdbcTemplate();

    public static int updateLocation(int objId, int x, int y, int z)
    {
        return jdbc.update("UPDATE characters SET x=?, y=?, z=? WHERE obj_Id=?", x, y, z, objId);
    }

    public static int getCharacterClanId(int objId)
    {
        return jdbc.query("SELECT clanid FROM characters WHERE obj_Id=? LIMIT 1", new Object[]{objId},
                rs ->
                {
                    if(rs.next())
                        return rs.getInt(1);
                    return 0;
                });
    }

    public static boolean isValidCharacter(int objId)
    {
        Integer count = jdbc.queryForObject("SELECT obj_Id FROM characters WHERE obj_Id=? LIMIT 1", Integer.class, objId);
        return count > 0;
    }

    public static int updateVariable(int objId, String bgr, String name, long expireTime)
    {
        return jdbc.update("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", objId, bgr, name, expireTime);
    }

    public static void updateCharacterData(int objId, int karma, int pkKills, int pvpkills, int sp, long exp)
    {
        try(Connection connection = DatabaseFactory.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("UPDATE character_subclasses SET sp = ?, exp = ? WHERE char_obj_id=? AND active='1' LIMIT 1");
            statement.setInt(1, sp);
            statement.setLong(2, sp);
            statement.setInt(3, objId);
            int count = statement.executeUpdate();
            if(count > 0)
            {
                JdbcUtils.closeStatement(statement);
                statement = connection.prepareStatement("UPDATE characters SET karma = ?, pkkills = ?, pvpkills = ? WHERE obj_Id=? LIMIT 1");
                statement.setInt(1, karma);
                statement.setLong(2, pkKills);
                statement.setLong(3, pvpkills);
                statement.setInt(4, objId);
                statement.executeUpdate();
            }
            JdbcUtils.closeStatement(statement);
        }
        catch(SQLException e)
        {
            LOGGER.error("", e);
        }
    }

    public static List<Integer> getObjIdsFromAccount(String account)
    {
        return jdbc.queryForList("SELECT obj_Id FROM characters WHERE account_name=?", int.class, account);
    }
}
