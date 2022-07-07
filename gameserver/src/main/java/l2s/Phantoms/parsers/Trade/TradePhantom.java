package l2s.Phantoms.parsers.Trade;


import java.util.List;

import l2s.Phantoms.enums.SpawnLocation;
import l2s.Phantoms.enums.TypeOfShop;
import l2s.Phantoms.objects.PhantomTradeItem;

public class TradePhantom
{
	public List <PhantomTradeItem> _items;
	public boolean _ispackage;
	public String _TradeBuyname;
	public TypeOfShop _TradeShop;
	public SpawnLocation _loc;
	private ChatAdvertising _ads;

	public TradePhantom(List<PhantomTradeItem> trade_item, boolean ispackage,String TradeBuyname,TypeOfShop TradeShop,SpawnLocation loc,ChatAdvertising ads)
	{ // список итемов для последующего парсинга
		_items = trade_item;
		_ispackage = ispackage;
		_TradeBuyname = TradeBuyname;
		_TradeShop = TradeShop;
		_loc = loc;
		_ads = ads;
	}
	
	public ChatAdvertising getAds()
	{
		return _ads; 
	}
	
	public TypeOfShop getType()
	{
		return _TradeShop;
	}
	
	public SpawnLocation getLocation()
	{
		return _loc;
	}
	
	public List<PhantomTradeItem> getItems()
	{
		return _items;
	}
	
	public int getCount()
	{
		return _items.size();
	}
}
