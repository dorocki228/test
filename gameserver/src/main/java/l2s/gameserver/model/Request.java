package l2s.gameserver.model;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Request extends MultiValueSet<String>
{
	private static final long serialVersionUID = 1L;
	private static final Logger _log;
	private static final AtomicInteger _nextId;
	private final int _id;
	private final L2RequestType _type;
	private final HardReference<Player> _requestor;
	private final HardReference<Player> _reciever;
	private boolean _isRequestorConfirmed;
	private boolean _isRecieverConfirmed;
	private boolean _isCancelled;
	private boolean _isDone;
	private long _timeout;
	private Future<?> _timeoutTask;

	public Request(L2RequestType type, Player requestor, Player reciever)
	{
		_id = _nextId.incrementAndGet();
		_requestor = requestor.getRef();
		_reciever = (HardReference<Player>) (reciever != null ? reciever.getRef() : HardReferences.emptyRef());
		_type = type;
		requestor.setRequest(this);
		if(reciever != null)
			reciever.setRequest(this);
	}

	public Request setTimeout(long timeout)
	{
		_timeout = timeout > 0L ? System.currentTimeMillis() + timeout : 0L;
		_timeoutTask = ThreadPoolManager.getInstance().schedule(() -> timeout(), timeout);
		return this;
	}

	public int getId()
	{
		return _id;
	}

	private void cancel0(IBroadcastPacket... packets)
	{
		if(_timeoutTask != null)
			_timeoutTask.cancel(false);
		_timeoutTask = null;
		Player player = getRequestor();
		if(player != null && player.getRequest() == this)
		{
			player.setRequest(null);
			player.sendPacket(packets);
		}
		player = getReciever();
		if(player != null && player.getRequest() == this)
		{
			player.setRequest(null);
			player.sendPacket(packets);
		}
	}

	public void cancel(IBroadcastPacket... packets)
	{
		_isCancelled = true;
		cancel0(packets);
	}

	public void done(IBroadcastPacket... packets)
	{
		_isDone = true;
		cancel0(packets);
	}

	public void timeout(IBroadcastPacket... packets)
	{
		Player player = getReciever();
		if(player != null && player.getRequest() == this)
			player.sendPacket(SystemMsg.TIME_EXPIRED);
		cancel(packets);
	}

	public Player getOtherPlayer(Player player)
	{
		if(player == getRequestor())
			return getReciever();
		if(player == getReciever())
			return getRequestor();
		return null;
	}

	public Player getRequestor()
	{
		return _requestor.get();
	}

	public Player getReciever()
	{
		return _reciever.get();
	}

	public boolean isInProgress()
	{
		return !_isCancelled && !_isDone && (_timeout == 0L || _timeout > System.currentTimeMillis());
	}

	public boolean isTypeOf(L2RequestType type)
	{
		return _type == type;
	}

	public void confirm(Player player)
	{
		if(player == getRequestor())
			_isRequestorConfirmed = true;
		else if(player == getReciever())
			_isRecieverConfirmed = true;
	}

	public boolean isConfirmed(Player player)
	{
		if(player == getRequestor())
			return _isRequestorConfirmed;
		return player == getReciever() && _isRecieverConfirmed;
	}

	static
	{
		_log = LoggerFactory.getLogger(Request.class);
		_nextId = new AtomicInteger();
	}

	public enum L2RequestType
	{
		CUSTOM,
		PARTY,
		PARTY_ROOM,
		CLAN,
		CLAN_WAR_START,
		CLAN_WAR_STOP,
		CLAN_WAR_SURRENDER,
		ALLY,
		TRADE,
		TRADE_REQUEST,
		FRIEND,
		CHANNEL,
		DUEL,
		COUPLE_ACTION,
		MENTEE,
		PARTY_MEMBER_SUBSTITUTE
    }
}
