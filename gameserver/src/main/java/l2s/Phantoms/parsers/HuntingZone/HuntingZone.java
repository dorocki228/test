package  l2s.Phantoms.parsers.HuntingZone;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Territory;
import  l2s.gameserver.model.World;
import l2s.gameserver.model.base.Race;
import  l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import  l2s.gameserver.utils.Location;

@XStreamAlias("zone")
public class HuntingZone implements Cloneable
{
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int id;
	
	@XStreamAlias("name")
	@XStreamAsAttribute
	private String name;
	
	@XStreamAlias("lvlMin")
	@XStreamAsAttribute
	private int lvlMin;
	
	@XStreamAlias("lvlMax")
	@XStreamAsAttribute
	private int lvlMax;
	
	@XStreamAlias("maxPartySize")
	@XStreamAsAttribute
	private int maxPartySize;
	
	@XStreamAlias("playerCheckRadius")
	@XStreamAsAttribute
	private int playerCheckRadius;
	
	@XStreamAlias("penaltyWeapon")
	@XStreamAsAttribute
	private SortedSet<WeaponType> penaltyWeapon = new TreeSet<WeaponType>();
	
	@XStreamAlias("classId")
	@XStreamAsAttribute
	private SortedSet<Integer> classId = new TreeSet<Integer>();
	
	@XStreamAlias("race")
	@XStreamAsAttribute
	private Race race;
	
	@XStreamImplicit(itemFieldName = "point")
	private List <Location> _loc= new ArrayList<Location>();
	
	@XStreamImplicit(itemFieldName = "polygon")
	private List <HuntingZonePolygon> polygon = new ArrayList<HuntingZonePolygon>();
	
	@XStreamOmitField
	private List <Territory> _list_territory = new ArrayList<Territory>();
	
	public HuntingZone()
	{
		id = 0;
		name="";
		lvlMin = 0;
		lvlMax = 0;
		maxPartySize = 0;
		playerCheckRadius = 0;
		penaltyWeapon = new TreeSet<WeaponType>();
		classId = new TreeSet<Integer>();
		race = null;
		_loc = new ArrayList<Location>();
		polygon = new ArrayList<HuntingZonePolygon>();
		_list_territory = new ArrayList<Territory>();
	}
	
	public List <Territory> getTerritoryList()
	{
		return _list_territory;
	}
	
	public void setTerritoryList(List <Territory> ter)
	{
		_list_territory = ter;
	}
	
	public void removeClassId(Integer _classid)
	{
		classId.remove(_classid);
	}
	public List<HuntingZonePolygon> getPolygon()
	{		
		if (polygon==null)
		polygon = new ArrayList<HuntingZonePolygon>();
		return polygon;
	}
	
	public void addPolygon(HuntingZonePolygon value)
	{
		if (polygon==null)
			polygon = new ArrayList<HuntingZonePolygon>();
		
		polygon.add(value);
	}
	
	
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String _name)
	{
		name = _name;
	}
	
	public SortedSet<WeaponType> getPenalty()
	{
		if (penaltyWeapon==null)
			penaltyWeapon = new TreeSet<WeaponType>();
		
		return penaltyWeapon;
	}
	
	public Race getRace()
	{
		return race;
	}
	public void setRace(Race param)
	{
		race = param;
	}
	
	public List <Location> getLoc()
	{
		if (_loc == null)
			_loc = new ArrayList<Location>();
		return _loc;
	}

	
	public void addLoc(Location loc)
	{
		if (_loc == null)
			_loc = new ArrayList<Location>();
		_loc.add(loc);
	}
	
	public int getCheckRadius()
	{
		return playerCheckRadius;
	}

	public void setCheckRadius(int param)
	{
		playerCheckRadius = param;
	}
	
	public int getLvlMin()
	{
		return lvlMin;
	}

	public int getLvlMax()
	{
		return lvlMax;
	}
	
	public void setLvlMin(int _lvl)
	{
		lvlMin = _lvl;
	}

	public void setLvlMax(int _lvl)
	{
		lvlMax = _lvl;
	}
	
	public int getMaxPartySize()
	{
		return maxPartySize;
	}
	
	public void setMaxPartySize(int param)
	{
		maxPartySize = param;
	}
	
	public Location getRandomPoint()
	{
		boolean _loc = true;
		boolean _terr = true;
		
		Location found_location= null;
		
		if (getLoc()== null||getLoc().isEmpty())
			_loc=false;
		
		if (getTerritoryList()== null||getTerritoryList().isEmpty())
			_terr=false;
		
		if(_loc==true&&_terr==false)
			found_location= Rnd.get(getLoc());
		
		if(_loc==false&&_terr==true)
			found_location= Rnd.get(getTerritoryList()).getRandomLoc(0);
		
		if(_loc==true&&_terr==true)
		{
			boolean chance = Rnd.nextBoolean();
			if (chance)
				found_location = Rnd.get(getTerritoryList()).getRandomLoc(0);
			else
				found_location = Rnd.get(getLoc());
		}
		if (getCheckRadius()!=0&& found_location!=null)
		{
			if(World.getAroundRealPlayers(found_location, getCheckRadius(), 300).size()<=0)
				return found_location;
		}else
			return found_location;
		return null;
	}

	public void setId(int i)
	{
		id=i;
	}

	public SortedSet<Integer> getClassId()
	{
		if (classId==null)
			classId = new TreeSet<Integer>();
		return classId;
	}

	public void setClassId(SortedSet <Integer> classId)
	{
		if (classId==null)
			classId = new TreeSet<Integer>();
		this.classId = classId;
	}

	public void removePoint(int x,int y,int z) 
	{
		Iterator<Location> numListIter = getLoc().iterator();
		while (numListIter.hasNext()) 
		{
			Location n = numListIter.next();
			if (n.getX()==x && n.getY()==y && n.getZ()==z) 
				numListIter.remove();
		}
	}

	public void removePoligon(int index) 
	{
		getPolygon().remove(index);
	}

  @Override
  public HuntingZone clone() throws CloneNotSupportedException 
  {
      return (HuntingZone) super.clone();
  }
}
