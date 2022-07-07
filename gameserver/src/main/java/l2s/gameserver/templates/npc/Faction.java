package l2s.gameserver.templates.npc;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

public class Faction
{
	public static final String none = "none";
	public static final Faction NONE;
	private final String _namesStr;
	private final List<String> _names;
	private final int _range;
	private final TIntList _ignoreNpcIds;

	public Faction(String names, int range)
	{
		_names = new ArrayList<>();
		_ignoreNpcIds = new TIntArrayList();
		_namesStr = names;
		for(String name : names.split(";"))
			if(name != null && !name.isEmpty())
				if(!"none".equals(name))
					_names.add(name.toLowerCase());
		_range = range;
	}

	public int getRange()
	{
		return _range;
	}

	public void addIgnoreNpcId(int npcId)
	{
		_ignoreNpcIds.add(npcId);
	}

	public boolean isIgnoreNpcId(int npcId)
	{
		return _ignoreNpcIds.contains(npcId);
	}

	public boolean isNone()
	{
		return _names.isEmpty();
	}

	public boolean containsName(String name)
	{
		for(String n : _names)
			if(n.equalsIgnoreCase(name))
				return true;
		return false;
	}

	public boolean equals(Faction faction)
	{
		if(isNone())
			return false;
		for(String name : _names)
			if(faction.containsName(name))
				return true;
		return false;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o != null && o.getClass() == getClass() && equals((Faction) o);
	}

	@Override
	public int hashCode()
	{
		return 7 * _namesStr.hashCode() + 23210;
	}

	@Override
	public String toString()
	{
		return _namesStr;
	}

	static
	{
		NONE = new Faction("none", 0);
	}
}
