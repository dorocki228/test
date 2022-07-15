package l2s.commons.listener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс реализующий список слушателей для каждого типа интерфейса.
 * 
 * @author G1ta0
 *
 * @param <T> базовый интерфейс слушателя
 */
public class ListenerList<T> implements Iterable<Listener<T>>
{
	protected Set<Listener<T>> listeners = ConcurrentHashMap.newKeySet();

	public Collection<Listener<T>> getListeners()
	{
		return listeners;
	}

	@Override
	public Iterator<Listener<T>> iterator()
	{
		return listeners.iterator();
	}
	
	/**
	 * Добавить слушатель в список
	 * @param listener
	 * @return возвращает true, если слушатель был добавлен
	 */
	public boolean add(Listener<T> listener)
	{
		return listeners.add(listener);
	}

	/**
	 * Удалить слушатель из списока
	 * @param listener
	 * @return возвращает true, если слушатель был удален
	 */
	public boolean remove(Listener<T> listener)
	{
		return listeners.remove(listener);
	}

}
