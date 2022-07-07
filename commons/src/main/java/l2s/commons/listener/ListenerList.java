package l2s.commons.listener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ListenerList<T>
{
	protected Set<Listener<T>> listeners;

	public ListenerList()
	{
        listeners = new CopyOnWriteArraySet<>();
	}

	public Collection<Listener<T>> getListeners()
	{
		return listeners;
	}

	public boolean add(@Nonnull Listener<T> listener)
	{
		return listeners.add(listener);
	}

	public boolean remove(@Nonnull Listener<T> listener)
	{
		return listeners.remove(listener);
	}
}
