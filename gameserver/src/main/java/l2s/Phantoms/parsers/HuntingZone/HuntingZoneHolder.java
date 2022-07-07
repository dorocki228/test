package  l2s.Phantoms.parsers.HuntingZone;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.Phantoms.Utils.PhantomUtils;
import  l2s.commons.geometry.Shape;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.geodata.GeoEngine;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Territory;
import  l2s.gameserver.model.World;
import  l2s.gameserver.model.items.ItemInstance;
import  l2s.gameserver.utils.Location;
import  l2s.gameserver.utils.Util;

public class HuntingZoneHolder
{
	protected final Logger _log = LoggerFactory.getLogger(HuntingZoneHolder.class);
	
	private Map <Integer,HuntingZone> territories = new HashMap <Integer,HuntingZone>();
	
	private SortedSet<Integer> level = new TreeSet<Integer>();
	
	public void addItems(HuntingZone huntingZone)
	{
		for(int i = huntingZone.getLvlMin(); i <= huntingZone.getLvlMax(); i++)
			level.add(i);
		try
		{
			territories.put(huntingZone.getId(),huntingZone.clone());
		}
		catch(CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void updateLevelList()
	{
 	level.clear();
 	for (HuntingZone ter:territories.values())
		for(int i = ter.getLvlMin(); i <= ter.getLvlMax(); i++)
			level.add(i);
	}
	
	public List<HuntingZone> getAll()
	{
		return new ArrayList<HuntingZone>(territories.values());
	}
	
	public Map<Integer, HuntingZone> getAllMap()
	{
		return territories;
	}
	
	public Location getRandomPoint(Player player)
	{
		ItemInstance weapon = player.getPhantomWeapon();
		if (weapon == null)
		{
			_log.info("HuntingZoneHolder: " + player+" "+" ActiveWeapon = null");
			return null;
		}
	
		int temp_level = PhantomUtils.FindNum(level.stream().mapToInt(Integer::intValue).toArray(), player.getLevel()); // поиск ближайшего уровня Util.nearest(player.getLevel(),level);
		
		List <HuntingZone> Filtered_territories = new ArrayList<>();
		for (HuntingZone ter : territories.values())
		{
		/*	if (!ter.getClassId().isEmpty())
				if(!ter.getClassId().contains(player.getClassId().getId()))
					continue;*/
			
			if (!ter.getPenalty().isEmpty())
				if(!ter.getPenalty().contains(weapon.getItemType()))
					continue;
			if (temp_level > ter.getLvlMax() || temp_level < ter.getLvlMin())
				continue;
			if (ter.getRace()!=null)
				if (ter.getRace()!= player.getRace())
					continue;
			Filtered_territories.add(ter);
		}
		if (Filtered_territories == null || Filtered_territories.isEmpty())
			return null;
		List <Location> lst_loc = new ArrayList<Location>();
		
		for (HuntingZone _ter : Filtered_territories)
		{
			if (_ter.getTerritoryList()!=null &&!_ter.getTerritoryList().isEmpty())
			{
				for (Territory tmp_ter : _ter.getTerritoryList())
				{
					Location found_lok = getRandomLoc(tmp_ter,player.getGeoIndex(), _ter.getCheckRadius());
					if (found_lok!=null)
						lst_loc.add(found_lok);
				}
			}
			
			if(_ter.getLoc()!=null && !_ter.getLoc().isEmpty())
			{
				for (Location tmp_loc : _ter.getLoc())
				{
					// проверим точки на игроков
					if (_ter.getCheckRadius() > 0)
					{
						if((World.getAroundPhantom(tmp_loc, _ter.getCheckRadius()/2, 400).size()<=0)&& World.getAroundRealPlayers(tmp_loc, _ter.getCheckRadius(), 400).size()<=0)
							lst_loc.add(tmp_loc);
					}else
						lst_loc.add(tmp_loc);
				}
			}
		}	
		
		if (lst_loc!=null&& !lst_loc.isEmpty())
			return Rnd.get(lst_loc);
		
		return null;
	}


	
	public static HuntingZoneHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static HuntingZoneHolder instance = new HuntingZoneHolder();
	}

	public static Location getRandomLoc(Territory territory, int geoIndex, int checkRadius)
	{
		Location pos = new Location();
		
		List <Shape> territories = territory.getTerritories();
		
		loop:for(int i = 0; i < 100; i++)
		{
			Shape shape = territories.get(Rnd.get(territories.size()));
			
			pos.x = Rnd.get(shape.getXmin(), shape.getXmax());
			pos.y = Rnd.get(shape.getYmin(), shape.getYmax());
			pos.z = shape.getZmin()+(shape.getZmax()-shape.getZmin())/2;
			
			if (territory.isInside(pos.x, pos.y))
			{
				// Не спаунить в колонны, стены и прочее.
				int tempz = GeoEngine.getHeight(pos, geoIndex);
				if (shape.getZmin() != shape.getZmax())
				{
					if (tempz < shape.getZmin() || tempz > shape.getZmax())
						continue;
				}
				else if (tempz < shape.getZmin()-200 || tempz > shape.getZmin()+200)
					continue;
				
				pos.z = tempz;
				
				int geoX = pos.x-World.MAP_MIN_X >> 4;
				int geoY = pos.y-World.MAP_MIN_Y >> 4;
				
				// Если местность подозрительная - пропускаем
				for(int x = geoX-1; x <= geoX+1; x++)
					for(int y = geoY-1; y <= geoY+1; y++)
						if (GeoEngine.NgetNSWE(x, y, tempz, geoIndex) != GeoEngine.NSWE_ALL)
							continue loop;
				
						if(checkRadius <= 0 ||  World.getAroundRealPlayers(pos, checkRadius, 400).size() > 0 || World.getAroundPhantom(pos, checkRadius/2, 400).size() > 0)
							continue loop;
							
				return pos;
			}
		}
		pos.z = pos.z+10;
		return pos;
	}
	
	public HuntingZone getRandomPartyPoint()
	{
	/*	List <HuntingZone> tmp = territories.stream().filter(t->t !=null && t.isUseForParty() && t.getLvl().size() > 0 && t.getLvlMin() <76&& t.getLvlMin() >45).collect(Collectors.toList());
		if(tmp!=null)
			return Rnd.get(tmp);*/
		return null;
	}

	public void removeHZ(int id)
	{
		HuntingZone remove = territories.remove(id);
		if (remove!=null)
   	updateLevelList();
	}

	public HuntingZone getHZbyId(int id)
	{
		return territories.get(id);
	}
	
}