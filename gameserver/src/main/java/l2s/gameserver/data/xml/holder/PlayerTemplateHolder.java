package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.templates.player.PlayerTemplate;

public final class PlayerTemplateHolder extends AbstractHolder
{
	private static final PlayerTemplateHolder _instance = new PlayerTemplateHolder();
	private static final TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<PlayerTemplate>>> _templates = new TIntObjectHashMap<>();

	public static PlayerTemplateHolder getInstance()
	{
		return _instance;
	}

	public void addPlayerTemplate(Race race, ClassType type, Sex sex, PlayerTemplate template)
	{
		if(!_templates.containsKey(race.ordinal()))
			_templates.put(race.ordinal(), new TIntObjectHashMap<>());
		if(!_templates.get(race.ordinal()).containsKey(type.ordinal()))
			_templates.get(race.ordinal()).put(type.ordinal(), new TIntObjectHashMap<>());
		_templates.get(race.ordinal()).get(type.ordinal()).put(sex.ordinal(), template);
	}

	public PlayerTemplate getPlayerTemplate(Race race, ClassId classId, Sex sex)
	{
		ClassType type = classId.getType();
		race = classId.getRace();
		if(_templates.containsKey(race.ordinal()) && _templates.get(race.ordinal()).containsKey(type.ordinal()))
			return _templates.get(race.ordinal()).get(type.ordinal()).get(sex.ordinal());
		return null;
	}

	@Override
	public int size()
	{
		return _templates.size();
	}

	@Override
	public void clear()
	{
		_templates.clear();
	}

}
