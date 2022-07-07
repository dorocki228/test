package l2s.gameserver.data.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.HennaTemplate;

import java.util.ArrayList;
import java.util.List;

public final class HennaHolder extends AbstractHolder
{
	private static final HennaHolder _instance;
	private final TIntObjectHashMap<HennaTemplate> _hennas;

	public HennaHolder()
	{
		_hennas = new TIntObjectHashMap();
	}

	public static HennaHolder getInstance()
	{
		return _instance;
	}

	public void addHenna(HennaTemplate h)
	{
		_hennas.put(h.getSymbolId(), h);
	}

	public HennaTemplate getHenna(int symbolId)
	{
		return _hennas.get(symbolId);
	}

	public List<HennaTemplate> generateList(Player player)
	{
		List<HennaTemplate> list = new ArrayList<>();
		TIntObjectIterator<HennaTemplate> iterator = _hennas.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			HennaTemplate h = iterator.value();
			list.add(h);
		}
		return list;
	}

	@Override
	public int size()
	{
		return _hennas.size();
	}

	@Override
	public void clear()
	{
		_hennas.clear();
	}

	static
	{
		_instance = new HennaHolder();
	}
}
