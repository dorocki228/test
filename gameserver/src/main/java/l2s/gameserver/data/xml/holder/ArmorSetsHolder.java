package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.ArmorSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ArmorSetsHolder extends AbstractHolder
{
	private static final ArmorSetsHolder _instance = new ArmorSetsHolder();
	private final TIntObjectHashMap<List<ArmorSet>> _armorSets;

	public ArmorSetsHolder()
	{
		_armorSets = new TIntObjectHashMap<>();
	}

	public static ArmorSetsHolder getInstance()
	{
		return _instance;
	}

	public void addArmorSet(ArmorSet armorset)
	{
		Collection<List<ArmorSet>> lists = _armorSets.valueCollection();
		for(List<ArmorSet> list : lists)
		{
			if(list.contains(armorset))
			{
				warn("Found duplicate armor set: " + Arrays.toString(armorset.getChestIds()));
				return;
			}
		}


		for(int id : armorset.getChestIds())
		{
			List<ArmorSet> sets = _armorSets.get(id);
			if(sets == null)
				sets = new ArrayList<>();
			sets.add(armorset);
			_armorSets.put(id, sets);
		}
		for(int id : armorset.getLegIds())
		{
			List<ArmorSet> sets = _armorSets.get(id);
			if(sets == null)
				sets = new ArrayList<>();
			sets.add(armorset);
			_armorSets.put(id, sets);
		}
		for(int id : armorset.getHeadIds())
		{
			List<ArmorSet> sets = _armorSets.get(id);
			if(sets == null)
				sets = new ArrayList<>();
			sets.add(armorset);
			_armorSets.put(id, sets);
		}
		for(int id : armorset.getGlovesIds())
		{
			List<ArmorSet> sets = _armorSets.get(id);
			if(sets == null)
				sets = new ArrayList<>();
			sets.add(armorset);
			_armorSets.put(id, sets);
		}
		for(int id : armorset.getFeetIds())
		{
			List<ArmorSet> sets = _armorSets.get(id);
			if(sets == null)
				sets = new ArrayList<>();
			sets.add(armorset);
			_armorSets.put(id, sets);
		}
		for(int id : armorset.getShieldIds())
		{
			List<ArmorSet> sets = _armorSets.get(id);
			if(sets == null)
				sets = new ArrayList<>();
			sets.add(armorset);
			_armorSets.put(id, sets);
		}
	}

	public List<ArmorSet> getArmorSets(int id)
	{
		return _armorSets.get(id);
	}

	@Override
	public int size()
	{
		return _armorSets.size();
	}

	@Override
	public void clear()
	{
		_armorSets.clear();
	}
}
