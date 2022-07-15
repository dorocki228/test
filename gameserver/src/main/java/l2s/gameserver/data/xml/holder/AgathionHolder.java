package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.cubic.AgathionTemplate;

/**
 * @author Bonux
**/
public final class AgathionHolder extends AbstractHolder
{
	private static AgathionHolder _instance = new AgathionHolder();

	private final TIntObjectHashMap<AgathionTemplate> _agathions = new TIntObjectHashMap<AgathionTemplate>();
	private final TIntObjectHashMap<AgathionTemplate> _agathionsByItemId = new TIntObjectHashMap<AgathionTemplate>();

	public static AgathionHolder getInstance()
	{
		return _instance;
	}

	private AgathionHolder()
	{
		//
	}

	public void addAgathionTemplate(AgathionTemplate template)
	{
		_agathions.put(template.getId(), template);
		for(int itemId : template.getItemIds())
			_agathionsByItemId.put(itemId, template);
	}

	public AgathionTemplate getTemplate(int id)
	{
		return _agathions.get(id);
	}

	public AgathionTemplate getTemplateByItemId(int itemId)
	{
		return _agathionsByItemId.get(itemId);
	}

	@Override
	public int size()
	{
		return _agathions.size();
	}

	@Override
	public void clear()
	{
		_agathions.clear();
	}
}
