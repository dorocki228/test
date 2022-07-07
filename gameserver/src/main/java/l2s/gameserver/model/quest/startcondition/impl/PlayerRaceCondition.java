package l2s.gameserver.model.quest.startcondition.impl;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.quest.startcondition.ICheckStartCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerRaceCondition implements ICheckStartCondition
{
	private final boolean _classRace;
	private final List<Race> _races;

	public PlayerRaceCondition(boolean human, boolean elf, boolean delf, boolean orc, boolean dwarf)
	{
		_races = new ArrayList<>(Race.VALUES.length);
		_classRace = false;
		if(human)
			_races.add(Race.HUMAN);
		if(elf)
			_races.add(Race.ELF);
		if(delf)
			_races.add(Race.DARKELF);
		if(orc)
			_races.add(Race.ORC);
		if(dwarf)
			_races.add(Race.DWARF);
	}

	public PlayerRaceCondition(boolean classRace, Race[] races)
	{
		_races = new ArrayList<>(Race.VALUES.length);
		_classRace = classRace;
		Collections.addAll(_races, races);
	}

	@Override
	public boolean checkCondition(Player player)
	{
		if(_races.isEmpty())
			return true;
		Race race = player.getClassId().getRace();
		if(!_classRace || race == null)
			race = player.getRace();
		return _races.contains(race);
	}
}
