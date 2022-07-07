package l2s.authserver;

import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServerManager
{
	private static final Logger _log = LoggerFactory.getLogger(GameServerManager.class);

	private static final GameServerManager _instance = new GameServerManager();

	private final Map<Integer, GameServerConnection> _gameServers;
	private final ReadWriteLock _lock;
	private final Lock _readLock;
	private final Lock _writeLock;

	public static final GameServerManager getInstance()
	{
		return _instance;
	}

	public GameServerManager()
	{
		_gameServers = new TreeMap<>();
		_lock = new ReentrantReadWriteLock();
		_readLock = _lock.readLock();
		_writeLock = _lock.writeLock();

		load();

		_log.info("Loaded " + _gameServers.size() + " registered GameServer(s).");
	}

	private void load()
	{}

	public List<GameServerConnection> getGameServers()
	{
		_readLock.lock();
		try
		{
			return List.copyOf(_gameServers.values());
		}
		finally
		{
			_readLock.unlock();
		}
	}

	public GameServerConnection getGameServerById(int id)
	{
		_readLock.lock();
		try
		{
			return _gameServers.get(id);
		}
		finally
		{
			_readLock.unlock();
		}
	}

	public boolean registerGameServer(int id, GameServerConnection gs)
	{
		_writeLock.lock();
		try
		{
			GameServerConnection gameServerConnection = _gameServers.get(id);
			if(!Config.ACCEPT_NEW_GAMESERVER && gameServerConnection == null)
				return false;
			if(gameServerConnection == null || !gameServerConnection.isAuthed())
			{
				if(gameServerConnection != null)
					gameServerConnection.getGameServerDescription().removeHost(id);
				_gameServers.put(id, gs);
				return true;
			}
		}
		finally
		{
			_writeLock.unlock();
		}
		return false;
	}
}
