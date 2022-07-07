package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.SubClassType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterSubclassDAO
{
	private static final Logger LOGGER = LogManager.getLogger(CharacterSubclassDAO.class);

	private static final CharacterSubclassDAO INSTANCE = new CharacterSubclassDAO();

	public static final String SELECT_SQL_QUERY = "SELECT class_id, exp, sp, curHp, curCp, curMp, active, type FROM character_subclasses WHERE char_obj_id=?";
	public static final String INSERT_SQL_QUERY = "INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String SUB_CLASS_STORE_1 = "UPDATE character_subclasses SET exp=?, sp=?, curHp=?, curMp=?, curCp=?, level=?, active=?, type=?" +
			" WHERE char_obj_id=? AND class_id=? LIMIT 1";
	public static final String SUB_CLASS_STORE_2 = "UPDATE character_subclasses SET maxHp=?, maxMp=?, maxCp=? WHERE char_obj_id=? AND active=1 LIMIT 1";

	private final DatabaseFactory databaseFactory;

	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate transactionTemplate;

	private CharacterSubclassDAO()
	{
		databaseFactory = DatabaseFactory.getInstance();
		jdbcTemplate = databaseFactory.getJdbcTemplate();
		transactionTemplate = databaseFactory.getTransactionTemplate();
	}

	public static CharacterSubclassDAO getInstance()
	{
		return INSTANCE;
	}

	public boolean insert(int objId, int classId, long exp, long sp, double curHp, double curMp, double curCp, double maxHp, double maxMp, double maxCp, int level, boolean active, SubClassType type)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = databaseFactory.getConnection();
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, objId);
			statement.setInt(2, classId);
			statement.setLong(3, exp);
			statement.setLong(4, sp);
			statement.setDouble(5, curHp);
			statement.setDouble(6, curMp);
			statement.setDouble(7, curCp);
			statement.setDouble(8, maxHp);
			statement.setDouble(9, maxMp);
			statement.setDouble(10, maxCp);
			statement.setInt(11, level);
			statement.setInt(12, active ? 1 : 0);
			statement.setInt(13, type.ordinal());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			LOGGER.error("CharacterSubclassDAO:insert(player)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public List<SubClass> restore(Player player)
	{
		List<SubClass> result = new ArrayList<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id, exp, sp, curHp, curCp, curMp, active, type FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				SubClass subClass = new SubClass(player);
				subClass.setType(SubClassType.VALUES[rset.getInt("type")]);
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setExp(rset.getLong("exp"), false);
				subClass.setSp(rset.getLong("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setActive(rset.getInt("active") == 1);
				result.add(subClass);
			}
		}
		catch(Exception e)
		{
			LOGGER.error("CharacterSubclassDAO:restore(player)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public boolean store(Player player)
	{
		if (player.isPhantom())
			return true;
		
		List<Object[]> args = player.getSubClassList().values().stream().map(subClass ->
				new Object[] {
						subClass.getExp(), subClass.getSp(), subClass.getHp(), subClass.getMp(),
						subClass.getCp(), subClass.getLevel(), subClass.isActive() ? 1 : 0,
						subClass.getType().ordinal(), player.getObjectId(), subClass.getClassId()
				}).collect(Collectors.toUnmodifiableList());
		transactionTemplate.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			public void doInTransactionWithoutResult(TransactionStatus status)
			{
				try
				{
					jdbcTemplate.batchUpdate(SUB_CLASS_STORE_1, args);
					jdbcTemplate.update(SUB_CLASS_STORE_2, player.getMaxHp(), player.getMaxMp(), player.getMaxCp(),
							player.getObjectId());
				}
				catch(DataAccessException e)
				{
					status.setRollbackOnly();
					LOGGER.error("Can't save player subclass", e);
				}
			}
		});

		return true;
	}
}
