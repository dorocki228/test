package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.npc.BuyListTemplate;

import java.util.Collection;
import java.util.Collections;

public final class BuyListHolder extends AbstractHolder
{
	private static final BuyListHolder _instance;
	private final TIntObjectMap<BuyListTemplate> _buyListsByListId;
	private final TIntObjectMap<TIntObjectMap<BuyListTemplate>> _buyListsByNpcId;

	public BuyListHolder()
	{
		_buyListsByListId = new TIntObjectHashMap<>();
		_buyListsByNpcId = new TIntObjectHashMap<>();
	}

	public static BuyListHolder getInstance()
	{
		return _instance;
	}

	public void addBuyList(BuyListTemplate buyList)
	{
		_buyListsByListId.put(buyList.getListId(), buyList);
		TIntObjectMap<BuyListTemplate> buyLists = _buyListsByNpcId.get(buyList.getNpcId());
		if(buyLists == null)
		{
			buyLists = new TIntObjectHashMap<>();
			_buyListsByNpcId.put(buyList.getNpcId(), buyLists);
		}
		buyLists.put(buyList.getId(), buyList);
	}

	public BuyListTemplate getBuyList(int listId)
	{
		return _buyListsByListId.get(listId);
	}

	public Collection<BuyListTemplate> getBuyLists(int npcId)
	{
		TIntObjectMap<BuyListTemplate> buyLists = _buyListsByNpcId.get(npcId);
		if(buyLists == null)
			return Collections.emptyList();
		return buyLists.valueCollection();
	}

	public BuyListTemplate getBuyList(int npcId, int buyListId)
	{
		TIntObjectMap<BuyListTemplate> buyLists = _buyListsByNpcId.get(npcId);
		if(buyLists == null)
			return null;
		return buyLists.get(buyListId);
	}

	@Override
	public int size()
	{
		return _buyListsByListId.size();
	}

	@Override
	public void clear()
	{
		_buyListsByListId.clear();
		_buyListsByNpcId.clear();
	}

	static
	{
		_instance = new BuyListHolder();
	}
}
