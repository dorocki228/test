package l2s.gameserver.model;

import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ObservableArena
{
	private final List<ObservePoint> _observers = new CopyOnWriteArrayList<>();

	public abstract Reflection getReflection();

	public abstract Location getObserverEnterPoint(Player var1);

	public abstract boolean showObservableArenasList(Player var1);

	public void onAppearObserver(ObservePoint observer)
	{}

	public void onAddObserver(ObservePoint observer)
	{}

	public void onRemoveObserver(ObservePoint observer)
	{}

	public void onEnterObserverArena(Player player)
	{}

	public void onChangeObserverArena(Player player)
	{}

	public void onExitObserverArena(Player player)
	{}

	public final List<ObservePoint> getObservers()
	{
		return _observers;
	}

	public final void addObserver(ObservePoint observer)
	{
		if(_observers.add(observer))
			onAddObserver(observer);
	}

	public final void removeObserver(ObservePoint observer)
	{
		if(_observers.remove(observer))
			onRemoveObserver(observer);
	}

	public final void clearObservers()
	{
		for(ObservePoint observer : _observers)
		{
			Player player = observer.getPlayer();

			if(!player.isInObserverMode())
				continue;

			player.leaveObserverMode();
		}
		_observers.clear();
	}
}
