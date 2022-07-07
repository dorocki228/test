package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.actor.instances.player.CharacterVariable;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: add cache
 */
public class CharacterVariablesDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterVariablesDAO.class);
	private static final CharacterVariablesDAO _instance = new CharacterVariablesDAO();
	private static final String SELECT_SQL_QUERY = "SELECT name, value, expire_time FROM character_variables WHERE obj_id = ?";
	private static final String SELECT_FROM_PLAYER_SQL_QUERY = "SELECT value, expire_time FROM character_variables WHERE obj_id = ? AND name = ?";
	private static final String DELETE_SQL_QUERY = "DELETE FROM character_variables WHERE obj_id = ? AND name = ? LIMIT 1";
	private static final String DELETE_EXPIRED_SQL_QUERY = "DELETE FROM character_variables WHERE expire_time > 0 AND expire_time < ?";
	private static final String INSERT_SQL_QUERY = "REPLACE INTO character_variables (obj_id, name, value, expire_time) VALUES (?,?,?,?)";
	private static final String DELETE_ALL_SQL_QUERY = "DELETE FROM character_variables WHERE name = ?";
	private static final String SELECT_FROM_PLAYERS_SQL_QUERY = "SELECT obj_id, name, value, expire_time FROM character_variables WHERE((obj_id) IN(%s)) AND((name) IN (%s))";

	// @formatter:off
	private static final String SELECT_ALL_PLAYER_LANGUAGE =
            "SELECT cv.obj_id, cv.value AS lang\n" +
			"FROM character_variables AS cv\n" +
			"WHERE cv.value IS NOT NULL\n" +
			"  AND NOT cv.value = ''\n" +
			"  AND cv.name LIKE '%lang@%'";

	private static final String SELECT_PLAYER_LANGUAGE =
			"SELECT cv.obj_id, cv.value AS lang\n" +
					"FROM character_variables AS cv\n" +
					"WHERE cv.value IS NOT NULL\n" +
					"  AND NOT cv.value = ''\n" +
					"  AND cv.name LIKE '%lang@%' LIMIT 1";
    // @formatter:on
	public CharacterVariablesDAO()
	{
		deleteExpiredVars();
	}

	public static CharacterVariablesDAO getInstance()
	{
		return _instance;
	}

	private void deleteExpiredVars()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_EXPIRED_SQL_QUERY);
			statement.setLong(1, System.currentTimeMillis());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CharacterVariablesDAO:deleteExpiredVars()", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean delete(int playerObjId, String varName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, playerObjId);
			statement.setString(2, varName);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CharacterVariablesDAO:delete(playerObjId,varName)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean insert(int playerObjId, CharacterVariable var)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setInt(1, playerObjId);
			statement.setString(2, var.getName());
			statement.setString(3, var.getValue());
			statement.setLong(4, var.getExpireTime());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("CharacterVariablesDAO:insert(playerObjId,var)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public List<CharacterVariable> restore(int playerObjId)
	{
		List<CharacterVariable> result = new ArrayList<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, playerObjId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				long expireTime = rset.getLong("expire_time");
				if(expireTime > 0L && expireTime < System.currentTimeMillis())
					continue;
				result.add(new CharacterVariable(rset.getString("name"), Strings.stripSlashes(rset.getString("value")), expireTime));
			}
		}
		catch(Exception e)
		{
			_log.error("CharacterVariablesDAO:restore(playerObjId)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public String getVarFromPlayer(int playerObjId, String var)
	{
		String value = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_FROM_PLAYER_SQL_QUERY);
			statement.setInt(1, playerObjId);
			statement.setString(2, var);
			rset = statement.executeQuery();
			if(rset.next())
			{
				long expireTime = rset.getLong("expire_time");
				if(expireTime <= 0L || expireTime >= System.currentTimeMillis())
					value = Strings.stripSlashes(rset.getString("value"));
			}
		}
		catch(Exception e)
		{
			_log.error("CharacterVariablesDAO:getVarFromPlayer(playerObjId,var)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return value;
	}

	private static String concatStringFromList(List<?> list){
		return list.stream().distinct().map(Object::toString).map(e -> "'" + e + "'").reduce((t, u) -> t + "," + u).orElse("");
	}

	public Map<Integer, MultiValueSet<String>> getVarsFromPlayers(List<String> listTypes, List<Integer> listObjects) {
		Map<Integer, MultiValueSet<String>> map = Collections.emptyMap();
		if(listObjects.isEmpty() || listTypes.isEmpty()) {
			return map;
		}
		String names = concatStringFromList(listTypes);
		String objIds = concatStringFromList(listObjects);

		String query = String.format(SELECT_FROM_PLAYERS_SQL_QUERY, objIds, names);
		try(Connection connection = DatabaseFactory.getInstance().getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery()) {
			while(resultSet.next()) {
				if(map.isEmpty()) {
					map = new HashMap<>();
				}
				//obj_id, name, value, expire_time
				int objId = resultSet.getInt("obj_id");
				String type = resultSet.getString("name");
				String value = resultSet.getString("value");
				long expireTime = resultSet.getLong("expire_time");
				if(expireTime > 0L && expireTime < System.currentTimeMillis()) {
					continue;
				}
				MultiValueSet<String> playerMap = map.computeIfAbsent(objId, k -> new MultiValueSet<>());
				playerMap.put(type, value);
			}
		} catch(Exception e) {
			_log.error("CharacterVariablesDAO:getVarsFromPlayers(listTypes, listObjects)", e);
		}
		return map;
	}

	public boolean delete(String varName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_ALL_SQL_QUERY);
			statement.setString(1, varName);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CharacterVariablesDAO:delete(varName)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public Map<Integer, Language> getAllPlayerLanguage() {
		return DatabaseFactory.getInstance().getJdbcTemplate().query(SELECT_ALL_PLAYER_LANGUAGE, rs -> {
			Map<Integer, Language> map = new HashMap<>();
			while (rs.next()) {
				final int objId = rs.getInt("obj_id");
				Language language = Language.getLanguage(rs.getString("lang"));
				map.put(objId, language);
			}
			return map;
		});
	}
	public Language getPlayerLanguageFromObjId(int objId) {
		return DatabaseFactory.getInstance().getJdbcTemplate().query(SELECT_PLAYER_LANGUAGE, rs -> {
			Language language = Language.ENGLISH;
			if (rs.next())
				language = Language.getLanguage(rs.getString("lang"));
			return language;
		});
	}
}
