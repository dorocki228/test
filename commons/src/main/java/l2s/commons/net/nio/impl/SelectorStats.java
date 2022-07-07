package l2s.commons.net.nio.impl;

import java.util.concurrent.atomic.AtomicLong;

public class SelectorStats
{
	private final AtomicLong _connectionsTotal;
	private final AtomicLong _connectionsCurrent;
	private final AtomicLong _connectionsMax;
	private final AtomicLong _incomingBytesTotal;
	private final AtomicLong _outgoingBytesTotal;
	private final AtomicLong _incomingPacketsTotal;
	private final AtomicLong _outgoingPacketsTotal;
	private final AtomicLong _bytesMaxPerRead;
	private final AtomicLong _bytesMaxPerWrite;

	public SelectorStats()
	{
		_connectionsTotal = new AtomicLong();
		_connectionsCurrent = new AtomicLong();
		_connectionsMax = new AtomicLong();
		_incomingBytesTotal = new AtomicLong();
		_outgoingBytesTotal = new AtomicLong();
		_incomingPacketsTotal = new AtomicLong();
		_outgoingPacketsTotal = new AtomicLong();
		_bytesMaxPerRead = new AtomicLong();
		_bytesMaxPerWrite = new AtomicLong();
	}

	public void increaseOpenedConnections()
	{
		if(_connectionsCurrent.incrementAndGet() > _connectionsMax.get())
			_connectionsMax.incrementAndGet();
		_connectionsTotal.incrementAndGet();
	}

	public void decreaseOpenedConnections()
	{
		_connectionsCurrent.decrementAndGet();
	}

	public void increaseIncomingBytes(int size)
	{
		if(size > _bytesMaxPerRead.get())
			_bytesMaxPerRead.set(size);
		_incomingBytesTotal.addAndGet(size);
	}

	public void increaseOutgoingBytes(int size)
	{
		if(size > _bytesMaxPerWrite.get())
			_bytesMaxPerWrite.set(size);
		_outgoingBytesTotal.addAndGet(size);
	}

	public void increaseIncomingPacketsCount()
	{
		_incomingPacketsTotal.incrementAndGet();
	}

	public void increaseOutgoingPacketsCount()
	{
		_outgoingPacketsTotal.incrementAndGet();
	}

	public long getTotalConnections()
	{
		return _connectionsTotal.get();
	}

	public long getCurrentConnections()
	{
		return _connectionsCurrent.get();
	}

	public long getMaximumConnections()
	{
		return _connectionsMax.get();
	}

	public long getIncomingBytesTotal()
	{
		return _incomingBytesTotal.get();
	}

	public long getOutgoingBytesTotal()
	{
		return _outgoingBytesTotal.get();
	}

	public long getIncomingPacketsTotal()
	{
		return _incomingPacketsTotal.get();
	}

	public long getOutgoingPacketsTotal()
	{
		return _outgoingPacketsTotal.get();
	}

	public long getMaxBytesPerRead()
	{
		return _bytesMaxPerRead.get();
	}

	public long getMaxBytesPerWrite()
	{
		return _bytesMaxPerWrite.get();
	}
}
