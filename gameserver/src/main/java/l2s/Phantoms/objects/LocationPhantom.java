package  l2s.Phantoms.objects;


import  l2s.Phantoms.enums.SpawnLocation;
import  l2s.gameserver.model.Territory;
import l2s.gameserver.model.base.Fraction;

public class LocationPhantom
{
	public SpawnLocation _name;
	public Territory _loc;
	public Fraction _fraction;
	
	public Fraction getFraction()
	{
		return _fraction;
	}

	public LocationPhantom(SpawnLocation name,Territory loc,Fraction fraction)
	{
		_name = name;
		_loc = loc;
		_fraction = fraction;
	}
	
	public Territory getLocation()
	{
		return _loc;
	}
	
	public SpawnLocation getName()
	{
		return _name;
	}
}
