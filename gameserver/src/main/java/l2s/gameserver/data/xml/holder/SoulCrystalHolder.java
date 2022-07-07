package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.SoulCrystal;

public final class SoulCrystalHolder extends AbstractHolder
{
	private static final SoulCrystalHolder _instance;
	private final TIntObjectHashMap<SoulCrystal> _crystals;

	public SoulCrystalHolder()
	{
		_crystals = new TIntObjectHashMap();
	}

	public static SoulCrystalHolder getInstance()
	{
		return _instance;
	}

	public void addCrystal(SoulCrystal crystal)
	{
		_crystals.put(crystal.getItemId(), crystal);
	}

	public SoulCrystal getCrystal(int item)
	{
		return _crystals.get(item);
	}

	public SoulCrystal[] getCrystals()
	{
		return _crystals.values(new SoulCrystal[_crystals.size()]);
	}

	@Override
	public int size()
	{
		return _crystals.size();
	}

	@Override
	public void clear()
	{
		_crystals.clear();
	}

	static
	{
		_instance = new SoulCrystalHolder();
	}
}
