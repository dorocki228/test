package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.xml.holder.HennaHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.templates.HennaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CharacterHennaDAO
{
	private static final String SELECT_QUERY = "SELECT symbol_id, draw_time, is_premium FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String INSERT_QUERY = "INSERT INTO `character_hennas` (char_obj_id, symbol_id, class_index, draw_time, is_premium) VALUES (?,?,?,?,?)";
	private static final String DELETE_QUERY = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? AND symbol_id=? AND draw_time=? AND is_premium=?";
	private static final Logger _log;
	private static final CharacterHennaDAO _instance;

	public static CharacterHennaDAO getInstance()
	{
		return _instance;
	}

	public List<Henna> select(Player owner)
	{
		List<Henna> list = new ArrayList<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT symbol_id, draw_time, is_premium FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, owner.getActiveClassId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int symbol_id = rset.getInt("symbol_id");
				int draw_time = rset.getInt("draw_time");
				boolean is_premium = rset.getInt("is_premium") > 0;
				HennaTemplate template = HennaHolder.getInstance().getHenna(symbol_id);
				Henna henna = null;
				boolean remove = template == null;
				if(!remove)
				{
					henna = new Henna(template, draw_time, is_premium);
					if(henna.getLeftTime() <= 0)
						remove = true;
				}
				if(remove)
                    delete(owner, symbol_id, draw_time, is_premium);
				else
					list.add(henna);
			}
		}
		catch(Exception e)
		{
			_log.error("CharacterHennaDAO.select(Player): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return list;
	}

	public boolean insert(Player owner, Henna henna)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, class_index, draw_time, is_premium) VALUES (?,?,?,?,?)");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, henna.getTemplate().getSymbolId());
			statement.setInt(3, owner.getActiveClassId());
			statement.setInt(4, henna.getDrawTime());
			statement.setInt(5, henna.isPremium() ? 1 : 0);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(owner.getHennaList() + " could not add henna to henna list: " + henna, e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean delete(Player owner, Henna henna)
	{
		return delete(owner, henna.getTemplate().getSymbolId(), henna.getDrawTime(), henna.isPremium());
	}

	private boolean delete(Player owner, int symbolId, int drawTime, boolean premium)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? AND symbol_id=? AND draw_time=? AND is_premium=?");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, owner.getActiveClassId());
			statement.setInt(3, symbolId);
			statement.setInt(4, drawTime);
			statement.setInt(5, premium ? 1 : 0);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(owner.getHennaList() + " could not delete henna: " + Henna.toString(symbolId, drawTime, premium) + " ownerId: " + owner.getObjectId(), e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	static
	{
		_log = LoggerFactory.getLogger(CharacterHennaDAO.class);
		_instance = new CharacterHennaDAO();
	}
}
