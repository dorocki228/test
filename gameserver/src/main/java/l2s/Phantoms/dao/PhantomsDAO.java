package  l2s.Phantoms.dao;

import l2s.commons.dbutils.DbUtils;
import  l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author : 4ipolino
 */
public class PhantomsDAO
{
	private static final Logger _log = LoggerFactory.getLogger(PhantomsDAO.class);

	private static final PhantomsDAO ourInstance = new PhantomsDAO();
	private static final String SELECT_SQL_QUERY = "SELECT obj_Id,char_name,title FROM characters WHERE account_name = ?";
	
	public static PhantomsDAO getInstance()
	{
		return ourInstance;
	}
	
	private PhantomsDAO()
	{}
	
	public void cleaningPhantomClan()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET clanid=0, pledge_type=0, pledge_rank=0, lvl_joined_academy=0, apprentice=0, title='', leaveclan=0 WHERE obj_Id>600000000");
			statement.execute();
		}catch(Exception e)
		{
			_log.warn("Exception: "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement);
			_log.info("----------[ Очистка кланов... Выполнено. ]----------");
		}
	}
}
