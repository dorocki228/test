package  l2s.Phantoms.objects.TrafficScheme;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import l2s.Phantoms.enums.PhantomType;
import  l2s.Phantoms.enums.RouteType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.utils.Location;

@XStreamAlias("route")
public class PhantomRoute
{
	
	@XStreamAlias("name")
	@XStreamAsAttribute
	private String _name;
	
	@XStreamAlias("class_id")
	@XStreamAsAttribute
	private int class_id;
	
	@XStreamAlias("phantomType")
	@XStreamAsAttribute
	private PhantomType phantomType;
	
	@XStreamAlias("lvl")
	@XStreamAsAttribute
	private int lvl;
	
	@XStreamAlias("fraction")
	@XStreamAsAttribute
	private Fraction fraction;
	
	@XStreamAlias("type")
	@XStreamAsAttribute
	private RouteType _type;
	
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int _id;
	
	@XStreamAlias("isPlayer")
	@XStreamAsAttribute
	private boolean isPlayer;

	@XStreamAlias("task")
	@XStreamImplicit
	private List<RouteTask> _task= new ArrayList<RouteTask>();
	
	public PhantomRoute(String name, RouteType type, int id, Player player)
	{
		setName(name);
		setType(type);
		setId(id);

		if (player!=null)
		{
			setName(player.getName()+"_"+System.currentTimeMillis());
			setPlayer(true);
		}
	}

	public boolean isPlayer()
	{
		return isPlayer;
	}

	public void setPlayer(boolean isPlayer)
	{
		this.isPlayer = isPlayer;
	}
	
	public void setFraction(Fraction fraction)
	{
		this.fraction = fraction;
	}
	public Fraction getFraction()
	{
		return fraction;
	}
	
	public int getClassId()
	{
		return class_id;
	}

	public void setClassId(int id)
	{
		this.class_id = id;
	}

	public PhantomType getPhantomType()
	{
		return phantomType;
	}

	public void setPhantomType(PhantomType phantomType)
	{
		this.phantomType = phantomType;
	}

	public int getLvl()
	{
		return lvl;
	}

	public void setLvl(int lvl)
	{
		this.lvl = lvl;
	}
	
	public List <Integer> getLevels()
	{
		List <Integer> levels = new ArrayList <Integer>();
		if (lvl<=3)
		{
			levels.add(0);
			return levels;
		}
		if (lvl == 85)
		{
			levels.add(85);
			return levels;
		}
		int lvl_min = lvl-4;
		int lvl_max = lvl+4;
		for(int i = lvl_min; i <= lvl_max; i++)
		{
			levels.add(i);
		}
		return levels;
	}

	public void addTask(RouteTask task)
	{
		_task.add(task);
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String _name)
	{
		this._name = _name;
	}

	public RouteType getType()
	{
		return _type;
	}

	public void setType(RouteType _type)
	{
		this._type = _type;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int _id)
	{
		this._id = _id;
	}

	public List<RouteTask> getTask()
	{
		return _task;
	}

	public List<RouteTask> getTaskGroupId(int id)
	{
		return _task.stream().filter(t-> t!=null && t.getGroupId()==id).collect(Collectors.toList());
	}
	
	public void setTask(List<RouteTask> _task)
	{
		this._task = _task;
	}
	
	public void pathClean()
	{
		
		for (Point p:getPointsFirstTask())
		{
			if(p.getDelay()!=null)
			p.getDelay().setMaxSeconds();
		}
		
		int delete_point = 0;
		int size = getPointsFirstTask().size();
		if(size > 2)
		{
			for(int i = 2; i < size; ++i)
			{
				final Point p3 = getPointsFirstTask().get(i);
				final Point p4 = getPointsFirstTask().get(i - 1);
				final Point p5 = getPointsFirstTask().get(i - 2);
				if(p4.getAttachments())
					continue;
				if(p5.getLoc().equals(p4.getLoc()) || p3.getLoc().equals(p4.getLoc()) || IsPointInLine(p5.getLoc(), p4.getLoc(), p3.getLoc()))
				{
					getPointsFirstTask().remove(i - 1);
					delete_point++;
					--size;
					i = Math.max(2, i - 2);
				}
			}
		}

	/*	for(int current = 0; current < points.size() - 2; ++current)
		{
			if (points.get(current).getAttachments())
				continue;
			
			final Location one = points.get(current).getLoc();
			for(int sub = current + 2; sub < points.size(); ++sub)
			{
				if(points.get(sub).getAttachments())
					continue;
				
				final Location two = points.get(sub).getLoc();
				if(one.equals(two) || GeoEngine.canMoveWithCollision(one.x, one.y, one.z, two.x, two.y, two.z, 0))
				{
					while(current + 1 < sub)
					{
						_log.info("TS :" +getName()+ " remove 2 " + points.get(current + 1).toString());
						points.remove(current + 1);
						delete_point++;
						--sub;
					}
				}
			}
		}*/
	
		
		//if(delete_point!=0)
		//_log.info(" delete " + delete_point + " point");
	}

	
	private static boolean IsPointInLine(final Location p1, final Location p2, final Location p3)
	{
		return (p1.x == p3.x && p3.x == p2.x) || (p1.y == p3.y && p3.y == p2.y) || (p1.x - p2.x) * (p1.y - p2.y) == (p2.x - p3.x) * (p2.y - p3.y);
	}

	public List<Point> getPointsFirstTask()
	{
		if (!_task.isEmpty())
			return _task.get(0).getPoints();
		return Collections.emptyList();
	}

	public void addPointsFirstTask(Point point)
	{
		if (getTask().isEmpty())
			addTask(new RouteTask(0));
		
		getTask().get(0).addPoints(point);
	}
	
	public boolean checkFaction()
	{
		for (Point p : getPointsFirstTask())
			if (p.getFraction()!=null ||p.getFraction()!=Fraction.NONE)
				return false;
		return true;
	}

}