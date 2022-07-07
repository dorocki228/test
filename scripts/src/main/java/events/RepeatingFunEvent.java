package events;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import java.util.ArrayList;
import java.util.List;

public class RepeatingFunEvent extends FunEvent
{
	private final SchedulingPattern _repeatPattern;
	private final int _random;
	private IntObjectMap<List<EventAction>> _storedOnTimeActions;

	public RepeatingFunEvent(MultiValueSet<String> set)
	{
		super(set);

		String repeat = set.getString("repeat_time_pattern", null);
		_repeatPattern = repeat != null ? new SchedulingPattern(repeat) : null;
		_random = set.getInteger("random", 0);
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		long stopTime = _stopPattern.getTimeInMillis();
		long currentTime = System.currentTimeMillis();
		if(currentTime >= stopTime) // event already finished ?
			return;

		long startTime = _startPattern.getTimeInMillis();
		if(startTime < currentTime && _repeatPattern != null) // event already started
			startTime = _repeatPattern.next(currentTime + 60000L);

		if(_random > 0)
			startTime += Rnd.get(-_random, _random) * 1000L;
		if(startTime <= currentTime)
			startTime = currentTime + 60000L;

		long dff = stopTime - startTime;
		if(dff <= 0) // next start time after event finish
			return;

		if(onInit)
		{
			if(!_onTimeActions.isEmpty())
			{
				_storedOnTimeActions = new TreeIntObjectMap<>();
				for(IntObjectPair<List<EventAction>> actionList : _onTimeActions.entrySet())
				{
					if(actionList.getValue() != null && !actionList.getValue().isEmpty())
					{
						List<EventAction> newList = new ArrayList<>(actionList.getValue().size());
                        newList.addAll(actionList.getValue());
						_storedOnTimeActions.put(actionList.getKey(), newList);
					}
				}
			}
		}
		else
		{
			clearActions();
			_onTimeActions.clear();

			if(_storedOnTimeActions != null)
				for(IntObjectPair<List<EventAction>> actionList : _storedOnTimeActions.entrySet())
				{
					List<EventAction> newList = new ArrayList<>(actionList.getValue().size());
                    newList.addAll(actionList.getValue());
					_onTimeActions.put(actionList.getKey(), newList);
				}
		}

		addOnTimeAction(0, new StartStopAction(StartStopAction.EVENT, true));
		addOnTimeAction((int) (dff / 1000L), new StartStopAction(StartStopAction.EVENT, false));

		_startTime = startTime;
		registerActions();
	}
}