package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class EventHolder extends AbstractHolder
{
    private static final EventHolder _instance = new EventHolder();

    private final IntObjectMap<Event> _events;

    public EventHolder()
    {
        _events = new TreeIntObjectMap<>();
    }

    public static EventHolder getInstance()
    {
        return _instance;
    }

    public void addEvent(Event event)
    {
        _events.put(getHash(event.getType(), event.getId()), event);
    }

    public <E extends Event> E getEvent(EventType type, int id)
    {
        return (E) _events.get(getHash(type, id));
    }

    public <E extends Event> Collection<Event> getEvents()
    {
        return _events.values();
    }

    public <E extends Event> List<E> getEvents(EventType type)
    {
        return _events.values().stream()
                .filter(e -> e.getType() == type)
                .map(e -> (E) e)
                .collect(Collectors.toList());
    }

    public <E extends Event> List<E> getEvents(Class<E> eventClass)
    {
        List<E> events = new ArrayList<>();
        for(Event e : _events.values())
            if(e.getClass() == eventClass)
                events.add((E) e);
            else
            {
                if(!eventClass.isAssignableFrom(e.getClass()))
                    continue;
                events.add((E) e);
            }
        return events;
    }

    public void findEvent(Player player)
    {
        _events.values().forEach(event -> event.findEvent(player));
    }

    public void callInit()
    {
        _events.values().forEach(Event::initEvent);
    }

    private static int getHash(EventType type, int id)
    {
        return type.ordinal() * 100000 + id;
    }

    @Override
    public int size()
    {
        return _events.size();
    }

    @Override
    public void clear()
    {
        _events.clear();
    }
}
