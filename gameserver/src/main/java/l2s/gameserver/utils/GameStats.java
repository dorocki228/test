package l2s.gameserver.utils;

import java.util.concurrent.atomic.AtomicLong;

public class GameStats
{
	private static final AtomicLong _updatePlayerBase;
	private static final AtomicLong _playerEnterGameCounter;

	public static void increaseUpdatePlayerBase()
	{
		_updatePlayerBase.incrementAndGet();
	}

	public static long getUpdatePlayerBase()
	{
		return _updatePlayerBase.get();
	}

	public static void incrementPlayerEnterGame()
	{
		_playerEnterGameCounter.incrementAndGet();
	}

	public static long getPlayerEnterGame()
	{
		return _playerEnterGameCounter.get();
	}

	static
	{
		_updatePlayerBase = new AtomicLong(0L);
		_playerEnterGameCounter = new AtomicLong(0L);
	}
}
