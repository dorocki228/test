package l2s.gameserver.instancemanager;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Couple;
import l2s.gameserver.network.l2.components.CustomMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.flogger.LazyArgs.lazy;

public class CoupleManager
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private static CoupleManager _instance;

	private List<Couple> _couples;
	private List<Couple> _deletedCouples;

	public static CoupleManager getInstance()
	{
		if(_instance == null)
			new CoupleManager();
		return _instance;
	}

	public CoupleManager()
	{
		_instance = this;
		_log.atInfo().log( "Initializing CoupleManager" );
		_instance.load();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new StoreTask(), 10 * 60 * 1000, 10 * 60 * 1000);
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM couples ORDER BY id");
			rs = statement.executeQuery();
			while(rs.next())
			{
				Couple c = new Couple(rs.getInt("id"));
				c.setPlayer1Id(rs.getInt("player1Id"));
				c.setPlayer2Id(rs.getInt("player2Id"));
				c.setMaried(rs.getBoolean("maried"));
				c.setAffiancedDate(rs.getLong("affiancedDate"));
				c.setWeddingDate(rs.getLong("weddingDate"));
				getCouples().add(c);
			}
			_log.atInfo().log( "Loaded: %s couples(s)", lazy(() -> getCouples().size()) );
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public final Couple getCouple(int coupleId)
	{
		for(Couple c : getCouples())
			if(c != null && c.getId() == coupleId)
				return c;
		return null;
	}

	/**
	 * ???????????????????? ?????? ???????????? ?????????? ?????????????????? ?? ??????
	 * @param cha
	 */
	public void engage(Player cha)
	{
		int chaId = cha.getObjectId();

		for(Couple cl : getCouples())
			if(cl != null)
				if(cl.getPlayer1Id() == chaId || cl.getPlayer2Id() == chaId)
				{
					if(cl.getMaried())
						cha.setMaried(true);

					cha.setCoupleId(cl.getId());

					if(cl.getPlayer1Id() == chaId)
						cha.setPartnerId(cl.getPlayer2Id());
					else
						cha.setPartnerId(cl.getPlayer1Id());
				}
	}

	/**
	 * ???????????????????? ???????????????? ?????????????????? ?? ?????? ?????????? ?? ??????.
	 * @param cha
	 */
	public void notifyPartner(Player cha)
	{
		if(cha.getPartnerId() != 0)
		{
			Player partner = GameObjectsStorage.getPlayer(cha.getPartnerId());
			if(partner != null)
				partner.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.CoupleManager.PartnerEntered"));
		}
	}

	public void createCouple(Player player1, Player player2)
	{
		if(player1 != null && player2 != null)
			if(player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
				getCouples().add(new Couple(player1, player2));
	}

	public final List<Couple> getCouples()
	{
		if(_couples == null)
			_couples = new CopyOnWriteArrayList<Couple>();
		return _couples;
	}

	public List<Couple> getDeletedCouples()
	{
		if(_deletedCouples == null)
			_deletedCouples = new CopyOnWriteArrayList<Couple>();
		return _deletedCouples;
	}

	/**
	 * ?????????????????? ?????? ????????????????
	 * ?????????????? ?????????????? ?????????????? ???? ???????????????? ????????????, ?????????? ???????????????? ?? ?????? ?????? ????????????.
	 * ?????????????????? ???????????????????? ???????????? ?????? ????????????????/???????????????? ??????????????, ???? ?????? ???? ??????????????
	 */
	public void store()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			if(_deletedCouples != null && !_deletedCouples.isEmpty())
			{
				statement = con.prepareStatement("DELETE FROM couples WHERE id = ?");
				for(Couple c : _deletedCouples)
				{
					statement.setInt(1, c.getId());
					statement.execute();
				}

				_deletedCouples.clear();
			}

			if(_couples != null && !_couples.isEmpty())
				for(Couple c : _couples)
					if(c != null && c.isChanged())
					{
						c.store(con);
						c.setChanged(false);
					}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private class StoreTask implements Runnable
	{
		@Override
		public void run()
		{
			store();
		}
	}
}