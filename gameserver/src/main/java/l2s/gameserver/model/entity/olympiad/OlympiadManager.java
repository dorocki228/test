package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OlympiadManager implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadManager.class);

	private final Map<Integer, OlympiadGame> _olympiadInstances = new ConcurrentHashMap<>();

	public void sleep(long time)
	{
		try
		{
			Thread.sleep(time);
		}
		catch(InterruptedException interruptedException)
		{
			// empty catch block
		}
	}

	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		while(Olympiad.inCompPeriod())
		{
			if(Olympiad.getParticipantsMap().isEmpty())
			{
				sleep(60000);
				continue;
			}

			while(Olympiad.inCompPeriod())
			{
				if(Olympiad._nonClassBasedRegisters.size() >= Config.NONCLASS_GAME_MIN)
					prepareBattles(CompType.NON_CLASSED, Olympiad._nonClassBasedRegisters);

				Olympiad._classBasedRegisters.keys().forEach(key -> {
					List<Integer> list = Olympiad._classBasedRegisters.get(key);
					if(list.size() < Config.CLASS_GAME_MIN)
						return;
					prepareBattles(CompType.CLASSED, list);
				});

				sleep(5000);
			}

			sleep(30000);
		}

		Olympiad._classBasedRegisters.clear();
		Olympiad._nonClassBasedRegisters.clear();
		Olympiad._playersHWID.clear();

		boolean allGamesTerminated = false;
		while(!allGamesTerminated)
		{
			sleep(30000);

			if(_olympiadInstances.isEmpty())
				break;

			allGamesTerminated = true;

			for(OlympiadGame game : _olympiadInstances.values())
			{
				if(game.getTask() == null || game.getTask().isTerminated())
					continue;
				allGamesTerminated = false;
			}
		}

		_olympiadInstances.clear();
	}

	private void prepareBattles(CompType type, List<Integer> list)
	{
		if (list.isEmpty()) {
			return;
		}

		NobleSelector<Integer> selector = new NobleSelector<>(list.size());
		for (Integer noble : list)
			if (noble != null)
				selector.add(noble, Olympiad.getParticipantPoints(noble));

		for(int i = 0; i < Olympiad.STADIUMS.length; ++i)
			try
			{
				if(!Olympiad.STADIUMS[i].isFreeToUse())
					continue;

                if(selector.size() < type.getMinSize())
                    break;

				List<Integer> nextOpponents = nextOpponents(selector, type);
				if(nextOpponents.size() < type.getMinSize())
					break;

				OlympiadGame game = new OlympiadGame(i, type, nextOpponents);
				game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1));

				_olympiadInstances.put(i, game);

				Olympiad.STADIUMS[i].setStadiaBusy();
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
	}

	public void freeOlympiadInstance(int index)
	{
		_olympiadInstances.remove(index);
		Olympiad.STADIUMS[index].setStadiaFree();
	}

	public OlympiadGame getOlympiadInstance(int index)
	{
		return _olympiadInstances.get(index);
	}

	public Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return _olympiadInstances;
	}

	private List<Integer> nextOpponents(NobleSelector<Integer> selector, CompType type)
	{
        List<Integer> opponents = new ArrayList<>();
        Integer noble;

        selector.reset();
        for(int i = 0; i < type.getMinSize(); i++)
        {
            noble = selector.select();
            if (noble == null) // DS: error handling ?
                break;
            opponents.add(noble);
            removeOpponent(noble);
        }

        return opponents;
	}

	private void removeOpponent(int noble)
	{
		Olympiad._classBasedRegisters.forEach((key, value) -> {
			if(value == noble)
			{
				Olympiad._classBasedRegisters.remove(key, value);
			}
		});
		Olympiad._nonClassBasedRegisters.remove(Integer.valueOf(noble));
		Olympiad._playersHWID.remove(noble);
	}
}
