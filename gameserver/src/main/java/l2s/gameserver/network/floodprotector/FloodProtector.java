package l2s.gameserver.network.floodprotector;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.GameServer;
import l2s.gameserver.network.floodprotector.config.FloodProtectorConfig;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2s.gameserver.network.l2.GameClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flood protector implementation.
 *
 * @author fordfrog
 * @author Java-man
 */
public final class FloodProtector
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();
	/**
	 * Client for this instance of flood protector.
	 */
	private final GameClient _client;
	/**
	 * Configuration of this instance of flood protector.
	 */
	private final FloodProtectorConfig _config;
	/**
	 * Next game tick when new request is allowed.
	 */
	private volatile long _nextTime = System.nanoTime();
	/**
	 * Request counter.
	 */
	private final AtomicInteger counter = new AtomicInteger(0);
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	private boolean _logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply punisment multiple times (flooding).
	 */
	private final AtomicBoolean _punishmentInProgress = new AtomicBoolean();
	
	/**
	 * Creates new instance of FloodProtector.
	 * @param client the game client for which flood protection is being created
	 * @param config flood protector configuration
	 */
	public FloodProtector(final GameClient client, final FloodProtectorConfig config)
	{
		_client = client;
		_config = config;
	}
	
	/**
	 * Checks whether the request is flood protected or not.
	 * @param command command issued or short command description
	 * @return true if action is allowed, otherwise false
	 */
	public boolean tryPerformAction(final String command)
	{
		final long curTime = System.nanoTime();
		
		if(_client.getActiveChar() != null && _client.getActiveChar().getPlayerAccess().CanIgnoreFloodProtector)
		{
			return true;
		}

		if (!"ignore".equals(_config.PUNISHMENT_TYPE) && _punishmentInProgress.get()) {
			return false;
		}

		long timeToNextUse = _nextTime - curTime;
		if(timeToNextUse > 0)
		{
			if(_config.LOG_FLOODING && !_logged)
			{
				log("called command ", command, " ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL.minusNanos(timeToNextUse).toMillis()), " ms after previous command");
				_logged = true;
			}

			int incrementedCount = counter.incrementAndGet();
			boolean limitExceed = _config.PUNISHMENT_LIMIT > 0 && incrementedCount >= _config.PUNISHMENT_LIMIT;
			if(limitExceed)
			{
				if ("ignore".equals(_config.PUNISHMENT_TYPE)) {
					return false;
				}

				if (!_punishmentInProgress.compareAndSet(false, true)) {
					return false;
				}

				if ("kick".equals(_config.PUNISHMENT_TYPE)) {
					kickPlayer();
				} else if ("ban".equals(_config.PUNISHMENT_TYPE)) {
					banAccount();
				} else if ("jail".equals(_config.PUNISHMENT_TYPE)) {
					jailChar();
				}

				_punishmentInProgress.set(false);

				return false;
			}

			return true;
		}

		int count = counter.get();
		boolean halfLimitExceed = _config.PUNISHMENT_LIMIT > 0 && count >= _config.PUNISHMENT_LIMIT / 2;
		if(count > 0 && halfLimitExceed)
		{
			if(_config.LOG_FLOODING)
			{
				log("issued ", String.valueOf(counter), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL.toMillis()), " ms");
			}
		}
		
		_nextTime = curTime + _config.FLOOD_PROTECTION_INTERVAL.toNanos();
		_logged = false;
		counter.set(0);
		return true;
	}
	
	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer()
	{
		Player player = _client.getActiveChar();
		if(player != null)
		{
			player.kick();
		} else {
			_client.closeNow();
		}

		log("kicked for flooding");
	}
	
	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount()
	{
		int accessLevel = 0;
		int banExpire = 0;

		if(_config.PUNISHMENT_TIME > 0)
			banExpire = (int) ((System.currentTimeMillis() + _config.PUNISHMENT_TIME) / 1000L);
		else
			accessLevel = -100;

		GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangeAccessLevel(_client.getLogin(), accessLevel, banExpire));
		Player player = _client.getActiveChar();
		if(player != null) {
			player.kick();
		} else {
			_client.closeNow();
		}
		log("banned for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME / 60000 + " mins");
	}

	/**
	 * Jails char.
	 */
	private void jailChar()
	{
		Player player = _client.getActiveChar();
		if(player != null)
		{
			player.toJail((int) (_config.PUNISHMENT_TIME / 60000));
			log("jailed for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME / 60000 + " mins");
		} else {
			_client.closeNow();
		}
	}
	
	private void log(String... lines)
	{
		final StringBuilder output = new StringBuilder(100);
		output.append(_config.FLOOD_PROTECTOR_TYPE);
		output.append(": ");
		for (String line : lines) {
			output.append(line);
		}

		FloodProtectorLogger.INSTANCE.getLogger().atInfo().with(FloodProtectorLogger.INSTANCE.getClientKey(), _client).log(output.toString());
	}
}