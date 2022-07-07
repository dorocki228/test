package l2s.authserver.accounts;

import l2s.authserver.ThreadPoolManager;
import l2s.authserver.network.l2.SessionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SessionManager
{
	//	private static final Logger _log = LoggerFactory.getLogger(SessionManager.class);
	private static final SessionManager _instance = new SessionManager();
	private final Map<SessionKey, Session> sessions;
	private final Lock lock;

	public static final SessionManager getInstance()
	{
		return _instance;
	}

	private SessionManager()
	{
		sessions = new HashMap<>();
		lock = new ReentrantLock();

		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			lock.lock();
			try
			{
				long currentMillis = System.currentTimeMillis();
				sessions.values().removeIf(session -> session.getExpireTime() < currentMillis);
			}
			finally
			{
				lock.unlock();
			}
		}, 30000L, 30000L);
	}

	public Session openSession(Account account)
	{
		lock.lock();
		try
		{
			Session session = new Session(account);
			sessions.put(session.getSessionKey(), session);
			return session;
		}
		finally
		{
			lock.unlock();
		}
	}

	public Session closeSession(SessionKey skey)
	{
		lock.lock();
		try
		{
			return sessions.remove(skey);
		}
		finally
		{
			lock.unlock();
		}
	}

	public Session getSessionByName(String name)
	{
		for(Session session : sessions.values())
			if(session.account.getLogin().equalsIgnoreCase(name))
				return session;
		return null;
	}

	public final class Session
	{
		private final Account account;
		private final SessionKey skey;
		private final long expireTime;

		private Session(Account account)
		{
			this.account = account;
			skey = SessionKey.create();
			expireTime = System.currentTimeMillis() + 60000L;
		}

		public SessionKey getSessionKey()
		{
			return skey;
		}

		public Account getAccount()
		{
			return account;
		}

		public long getExpireTime()
		{
			return expireTime;
		}
	}
}
