package l2s.gameserver.model.entity.events;

import l2s.gameserver.model.Creature;
import org.jooq.lambda.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class EventOwner
{
	private final Set<Event> _events;

	public EventOwner()
	{
		_events = new ConcurrentSkipListSet<>(EventComparator.getInstance());
	}

	public <E extends Event> E getEvent(Class<E> eventClass)
	{
		for(Event e : _events)
		{
			if(e.getClass() == eventClass)
				return eventClass.cast(e);
			if(eventClass.isAssignableFrom(e.getClass()))
				return eventClass.cast(e);
		}
		return null;
	}

	public <E extends Event> List<E> getEvents(Class<E> eventClass)
	{
		List<E> events = new ArrayList<>();
		for(Event e : _events)
			if(e.getClass() == eventClass)
				events.add(eventClass.cast(e));
			else
			{
				if(!eventClass.isAssignableFrom(e.getClass()))
					continue;
				events.add(eventClass.cast(e));
			}
		return events;
	}

	public boolean containsEvent(Event event)
	{
		return _events.contains(event);
	}

	public boolean containsEvent(Class<? extends Event> eventClass)
	{
		for(Event e : _events)
		{
			if(e.getClass() == eventClass)
				return true;
			if(eventClass.isAssignableFrom(e.getClass()))
				return true;
		}

		return false;
	}

	public boolean containsEvent(Class<? extends Event> eventClass, Set<Integer> excludeIds)
	{
		for (Event e : _events) {
			if (excludeIds.contains(e.getId())) {
				continue;
			}
			if (e.getClass() == eventClass)
				return true;
			if (eventClass.isAssignableFrom(e.getClass()))
				return true;
		}

		return false;
	}

	public void addEvent(Event event)
	{
		_events.add(event);
	}

	public void removeEvent(Event event)
	{
		_events.remove(event);
	}

	public void removeEvents(Class<? extends Event> eventClass)
	{
		for(Event e : _events)
			if(e.getClass() == eventClass)
				_events.remove(e);
			else
			{
				if(!eventClass.isAssignableFrom(e.getClass()))
					continue;
				_events.remove(e);
			}
	}

	public Set<Event> getEvents()
	{
		return _events;
	}

	public <E extends Event> E isInSameEvent(final Creature attacker, Class<E> eventClass) {
		if(attacker == null)
			return null;

		return Seq.seq(attacker.getEvents(eventClass))
				.retainAll(getEvents(eventClass))
				.findAny()
				.orElse(null);
	}
}
