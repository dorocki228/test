package l2s.gameserver.model.entity;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Couple
{
	private static final Logger _log;
	private int _id;
	private int _player1Id;
	private int _player2Id;
	private boolean _maried;
	private long _affiancedDate;
	private long _weddingDate;
	private boolean isChanged;

	public Couple(int coupleId)
	{
		_id = 0;
		_player1Id = 0;
		_player2Id = 0;
		_maried = false;
		_id = coupleId;
	}

	public Couple(Player player1, Player player2)
	{
		_id = 0;
		_player1Id = 0;
		_player2Id = 0;
		_maried = false;
		_id = IdFactory.getInstance().getNextId();
		_player1Id = player1.getObjectId();
		_player2Id = player2.getObjectId();
		long time = System.currentTimeMillis();
		_affiancedDate = time;
		_weddingDate = time;
		player1.setCoupleId(_id);
		player1.setPartnerId(_player2Id);
		player2.setCoupleId(_id);
		player2.setPartnerId(_player1Id);
	}

	public void marry()
	{
		_weddingDate = System.currentTimeMillis();
		setChanged(_maried = true);
	}

	public void divorce()
	{
		CoupleManager.getInstance().getCouples().remove(this);
		CoupleManager.getInstance().getDeletedCouples().add(this);
	}

	public void store(Connection con)
	{
		PreparedStatement statement = null;
		try
		{
			statement = con.prepareStatement("REPLACE INTO couples (id, player1Id, player2Id, maried, affiancedDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _id);
			statement.setInt(2, _player1Id);
			statement.setInt(3, _player2Id);
			statement.setBoolean(4, _maried);
			statement.setLong(5, _affiancedDate);
			statement.setLong(6, _weddingDate);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
	}

	public final int getId()
	{
		return _id;
	}

	public final int getPlayer1Id()
	{
		return _player1Id;
	}

	public final int getPlayer2Id()
	{
		return _player2Id;
	}

	public final boolean getMaried()
	{
		return _maried;
	}

	public final long getAffiancedDate()
	{
		return _affiancedDate;
	}

	public final long getWeddingDate()
	{
		return _weddingDate;
	}

	public void setPlayer1Id(int _player1Id)
	{
		this._player1Id = _player1Id;
	}

	public void setPlayer2Id(int _player2Id)
	{
		this._player2Id = _player2Id;
	}

	public void setMaried(boolean _maried)
	{
		this._maried = _maried;
	}

	public void setAffiancedDate(long _affiancedDate)
	{
		this._affiancedDate = _affiancedDate;
	}

	public void setWeddingDate(long _weddingDate)
	{
		this._weddingDate = _weddingDate;
	}

	public boolean isChanged()
	{
		return isChanged;
	}

	public void setChanged(boolean val)
	{
		isChanged = val;
	}

	static
	{
		_log = LoggerFactory.getLogger(Couple.class);
	}
}
