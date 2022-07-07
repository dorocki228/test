package  l2s.Phantoms.parsers.Craft;


import java.util.List;

import  l2s.Phantoms.enums.SpawnLocation;

public class CraftPhantom
{
	public List <Integer> _recipes;
	public List <Integer> _prices;
	public int _count;
	public String _Craftname;
	public SpawnLocation _loc;
	
	public CraftPhantom(List <Integer> recipes,List <Integer> prices,int count,String Craftname,SpawnLocation loc)
	{
		_recipes = recipes;
		_prices = prices;
		_count = count;
		_Craftname = Craftname;
		_loc = loc;
	}
	
	public SpawnLocation getLocation()
	{
		return _loc;
	}
}
