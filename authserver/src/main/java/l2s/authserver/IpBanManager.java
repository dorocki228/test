package l2s.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IpBanManager
{
	private static final Logger _log = LoggerFactory.getLogger(IpBanManager.class);
	private static final IpBanManager _instance = new IpBanManager();
	private final Map<String, IpSession> ips;
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public static final IpBanManager getInstance()
	{
		return _instance;
	}

	private IpBanManager()
	{
		ips = new HashMap<>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		if(Config.IP_BAN_MANAGER_ENABLE)
		{
			ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				long currentMillis = System.currentTimeMillis();
				writeLock.lock();
				try
				{
					ips.values().removeIf(session -> session.banExpire < currentMillis
							&& session.lastTry < currentMillis - Config.LOGIN_TRY_TIMEOUT);
				}
				finally
				{
					writeLock.unlock();
				}
			}, 1000L, 1000L);
		}
	}

	public boolean isIpBanned(String ip)
	{
		if(!Config.IP_BAN_MANAGER_ENABLE)
			return false;

		readLock.lock();
		try
		{
			IpSession ipsession;
			return (ipsession = ips.get(ip)) != null && ipsession.banExpire > System.currentTimeMillis();
		}
		finally
		{
			readLock.unlock();
		}
	}

	public boolean tryLogin(String ip, boolean success)
	{
		if(!Config.IP_BAN_MANAGER_ENABLE)
			return true;

		writeLock.lock();
		try
		{
			IpSession ipsession;
			if((ipsession = ips.get(ip)) == null)
				ips.put(ip, ipsession = new IpSession());
			long currentMillis = System.currentTimeMillis();
			if(currentMillis - ipsession.lastTry < Config.LOGIN_TRY_TIMEOUT)
				success = false;
			if(success)
			{
				if(ipsession.tryCount > 0)
				{
					IpSession ipSession = ipsession;
					--ipSession.tryCount;
				}
			}
			else if(ipsession.tryCount < Config.LOGIN_TRY_BEFORE_BAN)
			{
				IpSession ipSession2 = ipsession;
				++ipSession2.tryCount;
			}
			ipsession.lastTry = currentMillis;
			if(ipsession.tryCount == Config.LOGIN_TRY_BEFORE_BAN)
			{
				_log.warn("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME / 1000L + " seconds.");
				ipsession.banExpire = currentMillis + Config.IP_BAN_TIME;
				return false;
			}
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private class IpSession
	{
		public int tryCount;
		public long lastTry;
		public long banExpire;
	}
}
