package l2s.gameserver.utils;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.network.l2.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author KanuToIIIKa
 */

public class AuthUtils
{
	private static final Logger _log = LoggerFactory.getLogger(AuthUtils.class);

	private static final String INSERT = "INSERT DELAYED INTO auth_log (date, account, objId, hwid, ip) VALUES(NOW(), ?, ?, ?, ?)";

	public static void logAuth(GameClient client)
	{
		Connection con = null;
		PreparedStatement st = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement(INSERT);
			st.setString(1, client.getLogin());
			st.setInt(2, client.getActiveChar().getObjectId());
			st.setString(3, client.getHwidString());
			st.setString(4, client.getIpAddr());
			st.execute();
		}
		catch(Exception e)
		{
			_log.error("AuthUtils: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

}
