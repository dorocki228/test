package l2s.gameserver.cache;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CrestCache
{
	public static final int ALLY_CREST_SIZE = 192;
	public static final int CREST_SIZE = 256;
	public static final int LARGE_CREST_PART_SIZE = 14336;
	private static final Logger _log = LoggerFactory.getLogger(CrestCache.class);
	private static final CrestCache _instance = new CrestCache();
	private final TIntIntMap _pledgeCrestId;
	private final TIntIntMap _pledgeCrestLargeId;
	private final TIntIntMap _allyCrestId;
	private final TIntObjectMap<byte[]> _pledgeCrest;
	private final TIntObjectMap<TIntObjectMap<byte[]>> _pledgeCrestLarge;
	private final TIntObjectMap<TIntObjectMap<byte[]>> _pledgeCrestLargeTemp;
	private final TIntObjectMap<byte[]> _allyCrest;
	private final ReentrantReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public static final CrestCache getInstance()
	{
		return _instance;
	}

	private CrestCache()
	{
		_pledgeCrestId = new TIntIntHashMap();
		_pledgeCrestLargeId = new TIntIntHashMap();
		_allyCrestId = new TIntIntHashMap();
		_pledgeCrest = new TIntObjectHashMap<>();
		_pledgeCrestLarge = new TIntObjectHashMap<>();
		_pledgeCrestLargeTemp = new TIntObjectHashMap<>();
		_allyCrest = new TIntObjectHashMap<>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		load();
	}

	public void load()
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id, crest FROM clan_data WHERE crest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				int pledgeId = rset.getInt("clan_id");
				byte[] crest = rset.getBytes("crest");
				int crestId = getCrestId(pledgeId, crest);
				_pledgeCrestId.put(pledgeId, crestId);
				_pledgeCrest.put(crestId, crest);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT clan_id, data FROM clan_largecrests WHERE crest_part=0 AND data IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				int pledgeId = rset.getInt("clan_id");
				byte[] crest = rset.getBytes("data");
				int crestId = getCrestId(pledgeId, crest);
				_pledgeCrestLargeId.put(pledgeId, crestId);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT clan_id, crest_part, data FROM clan_largecrests WHERE data IS NOT NULL ORDER BY clan_id asc, crest_part asc");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int pledgeId = rset.getInt("clan_id");
				int crestPartId = rset.getInt("crest_part");
				byte[] crest = rset.getBytes("data");
				if(!_pledgeCrestLargeId.containsKey(pledgeId))
					_log.warn("Clan large crest has crashed. Clan ID: " + pledgeId);
				else
				{
					int crestId = _pledgeCrestLargeId.get(pledgeId);
					TIntObjectMap<byte[]> crestMap = _pledgeCrestLarge.get(crestId);
					if(crestMap == null)
						crestMap = new TIntObjectHashMap();
					crestMap.put(crestPartId, crest);
					_pledgeCrestLarge.put(crestId, crestMap);
				}
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT ally_id, crest FROM ally_data WHERE crest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				int pledgeId = rset.getInt("ally_id");
				byte[] crest = rset.getBytes("crest");
				int crestId = getCrestId(pledgeId, crest);
				_allyCrestId.put(pledgeId, crestId);
				_allyCrest.put(crestId, crest);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("CrestCache: Loaded " + count + " crests");
	}

	private static int getCrestId(int pledgeId, byte[] crest)
	{
		return Math.abs(new HashCodeBuilder(15, 87).append(pledgeId).append(crest).toHashCode());
	}

	public byte[] getPledgeCrest(int crestId)
	{
        readLock.lock();
        byte[] crest = null;
        try
		{
			crest = _pledgeCrest.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}

	public TIntObjectMap<byte[]> getPledgeCrestLarge(int crestId)
	{
        readLock.lock();
        TIntObjectMap<byte[]> crest = null;
        try
		{
			crest = _pledgeCrestLarge.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}

	public byte[] getAllyCrest(int crestId)
	{
        readLock.lock();
        byte[] crest = null;
        try
		{
			crest = _allyCrest.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}

	public int getPledgeCrestId(int pledgeId)
	{
        readLock.lock();
        int crestId = 0;
        try
		{
			crestId = _pledgeCrestId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}

	public int getPledgeCrestLargeId(int pledgeId)
	{
        readLock.lock();
        int crestId = 0;
        try
		{
			crestId = _pledgeCrestLargeId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}

	public int getPledgeIdByCrestLargeId(int crestId)
	{
        readLock.lock();
        int pledgeId = 0;
        try
		{
			if(_pledgeCrestLargeId.containsValue(crestId))
			{
				TIntIntIterator iterator = _pledgeCrestLargeId.iterator();
				while(iterator.hasNext())
				{
					iterator.advance();
					if(iterator.value() == crestId)
					{
						pledgeId = iterator.key();
						break;
					}
				}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return pledgeId;
	}

	public int getAllyCrestId(int pledgeId)
	{
        readLock.lock();
        int crestId = 0;
        try
		{
			crestId = _allyCrestId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}

	public void removePledgeCrest(int pledgeId)
	{
		writeLock.lock();
		try
		{
			_pledgeCrest.remove(_pledgeCrestId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void removePledgeCrestLarge(int pledgeId)
	{
		writeLock.lock();
		try
		{
			_pledgeCrestLarge.remove(_pledgeCrestLargeId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_largecrests WHERE clan_id=?");
			statement.setInt(1, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void removeAllyCrest(int pledgeId)
	{
		writeLock.lock();
		try
		{
			_allyCrest.remove(_allyCrestId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int savePledgeCrest(int pledgeId, byte[] crest)
	{
		int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_pledgeCrestId.put(pledgeId, crestId);
			_pledgeCrest.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}

	public int savePledgeCrestLarge(int pledgeId, int crestPart, int crestTotalSize, byte[] data)
	{
        writeLock.lock();
        int crestId = 0;
        try
		{
			TIntObjectMap<byte[]> crest = _pledgeCrestLargeTemp.get(pledgeId);
			if(crestPart == 0)
			{
				_pledgeCrestLargeTemp.remove(pledgeId);
				crest = new TIntObjectHashMap();
			}
			if(crest != null)
			{
				crest.put(crestPart, data);
				int tempSize = getByteMapSize(crest);
				if(crestTotalSize > tempSize)
					_pledgeCrestLargeTemp.put(pledgeId, crest);
				else if(crestTotalSize < tempSize)
				{
					_pledgeCrestLargeTemp.remove(pledgeId);
					_log.warn("Error while save pledge large crest, clan_id: " + pledgeId + ", crest_part: " + crestPart + ", crest_total_size: " + crestTotalSize + ", temp_size: " + tempSize);
				}
				else
				{
					crestId = getCrestId(pledgeId, crest.get(0));
					_pledgeCrestLargeId.put(pledgeId, crestId);
					_pledgeCrestLarge.put(crestId, crest);
					Connection con = null;
					PreparedStatement statement = null;
					try
					{
						con = DatabaseFactory.getInstance().getConnection();
						statement = con.prepareStatement("DELETE FROM clan_largecrests WHERE clan_id=?");
						statement.setInt(1, pledgeId);
						statement.execute();
						DbUtils.closeQuietly(statement);
						TIntObjectIterator<byte[]> iterator = crest.iterator();
						while(iterator.hasNext())
						{
							iterator.advance();
							statement = con.prepareStatement("REPLACE INTO clan_largecrests(clan_id, crest_part, data) VALUES (?,?,?)");
							statement.setInt(1, pledgeId);
							statement.setInt(2, iterator.key());
							statement.setBytes(3, iterator.value());
							statement.execute();
							DbUtils.closeQuietly(statement);
						}
					}
					catch(Exception e)
					{
						_log.error("", e);
					}
					finally
					{
						DbUtils.closeQuietly(con, statement);
					}
					_pledgeCrestLargeTemp.remove(pledgeId);
				}
			}
			else
				_log.warn("Error while save pledge large crest, clan_id: " + pledgeId + ", crest_part: " + crestPart + ", crest_total_size: " + crestTotalSize);
		}
		finally
		{
			writeLock.unlock();
		}
		return crestId;
	}

	public int saveAllyCrest(int pledgeId, byte[] crest)
	{
		int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_allyCrestId.put(pledgeId, crestId);
			_allyCrest.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}

	public static int getByteMapSize(TIntObjectMap<byte[]> map)
	{
		int size = 0;
		if(map != null && !map.isEmpty())
			for(byte[] tempCrest : map.valueCollection())
				size += tempCrest.length;
		return size;
	}

	public static boolean isValidCrestData(byte[] crestData)
	{
		switch(crestData.length)
		{
			case 192:
			case 256:
			case 2176:
			{
				break;
			}
			default:
			{
				return false;
			}
		}

		if(crestData[0] != 68 || crestData[1] != 68 || crestData[2] != 83 || crestData[3] != 32 || crestData[84] != 68 || crestData[85] != 88 || crestData[86] != 84 || crestData[87] != 49)
			return false;

		switch(crestData.length)
		{
			case 192:
			{
				if(crestData[12] == 16 && crestData[16] == 8)
					break;
				return false;
			}
			case 256:
			{
				if(crestData[12] == 16 && crestData[16] == 16)
					break;
				return false;
			}
			case 2176:
			{
				if(crestData[12] == 64 && crestData[16] == 64)
					break;
				return false;
			}
		}
		return true;
	}

}
