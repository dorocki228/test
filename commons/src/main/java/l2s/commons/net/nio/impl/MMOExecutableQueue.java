package l2s.commons.net.nio.impl;

import l2s.commons.util.concurrent.Lockable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MMOExecutableQueue<T extends MMOClient> implements Queue<ReceivablePacket<T>>, Lockable, Runnable
{
	private static final int NONE = 0;
	private static final int QUEUED = 1;
	private static final int RUNNING = 2;

	private final IMMOExecutor<T> _executor;
	private final Queue<ReceivablePacket<T>> _queue = new ArrayDeque<>();

	private final Lock _lock = new ReentrantLock();

	private final AtomicInteger _state = new AtomicInteger(NONE);

	public MMOExecutableQueue(IMMOExecutor<T> executor)
	{
		_executor = executor;
	}

	@Override
	public void lock()
	{
		_lock.lock();
	}

	@Override
	public void unlock()
	{
		_lock.unlock();
	}

	@Override
	public void run()
	{
		while(_state.compareAndSet(QUEUED, RUNNING))
			try
			{
				for(;;)
				{
					Runnable t = poll();
					if(t == null)
						break;

					t.run();
				}
			}
			finally
			{
				_state.compareAndSet(RUNNING, NONE);
			}
	}

	@Override
	public int size()
	{
		return _queue.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _queue.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<ReceivablePacket<T>> iterator()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E> E[] toArray(E[] a)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends ReceivablePacket<T>> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		lock();
        _queue.clear();
        unlock();
	}

	@Override
	public boolean add(ReceivablePacket<T> e)
	{
		lock();
        if(!_queue.add(e))
        {
            unlock();
            return false;
        }
        unlock();

		if(_state.getAndSet(QUEUED) == NONE)
			_executor.execute(this);

		return true;
	}

	@Override
	public boolean offer(ReceivablePacket<T> e)
	{
        lock();
        boolean result = _queue.offer(e);
        unlock();
		return result;
	}

	@Override
	public ReceivablePacket<T> remove()
	{
        lock();
        ReceivablePacket<T> result = _queue.remove();
        unlock();
		return result;
	}

	@Override
	public ReceivablePacket<T> poll()
	{
        lock();
        ReceivablePacket<T> result = _queue.poll();
        unlock();
		return result;
	}

	@Override
	public ReceivablePacket<T> element()
	{
        lock();
        ReceivablePacket<T> result = _queue.element();
        unlock();
		return result;
	}

	@Override
	public ReceivablePacket<T> peek()
	{
        lock();
        ReceivablePacket<T> result = _queue.peek();
        unlock();
		return result;
	}
}
