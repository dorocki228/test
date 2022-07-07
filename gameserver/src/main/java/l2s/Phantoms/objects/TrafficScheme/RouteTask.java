package  l2s.Phantoms.objects.TrafficScheme;

import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import  l2s.Phantoms.objects.RouteTargetCheck;
import l2s.commons.util.Rnd;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.Config;
import  l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;

@XStreamAlias("task")
public class RouteTask
{
	@XStreamOmitField
	private static final Logger _log = LoggerFactory.getLogger(RouteTask.class);
	
	@XStreamAlias("group")
	@XStreamAsAttribute
	private int _group = 0;

	@XStreamAlias("point")
	@XStreamImplicit
	private List<Point> points= new ArrayList<Point>();
	
	private RouteTargetCheck rtc;
	
	public RouteTask(int group)
	{
		setGroupId(group);
	}

	public int getGroupId()
	{
		return _group;
	}

	public void setGroupId(int _group)
	{
		this._group = _group;
	}


	public void setTargetCheck(RouteTargetCheck _target)
	{
		rtc = _target;
	}
	
	public RouteTargetCheck getTargetCheck()
	{
		return rtc;
	}
	//TODO дергать состояние нпс прямиком с осады
	public boolean targetCheck(Player player)
	{
		List<Creature> found_targets = player.getAroundCharacters(3000, 1500).stream().filter(c -> c!=null && c.isDoor() ? ((DoorInstance) c).getDoorId() == rtc.id:c.getNpcId() == rtc.id).collect(Collectors.toList());
		
		switch (rtc.tvalid)
		{
			case DEAD_NPC:
			if (found_targets == null || found_targets.isEmpty())
					return true;
				break;
			case LIVE_NPC:
				if (found_targets != null && found_targets.size()>0)
					return true;
				break;
			case CLASS_ID:
				for (Player member : player.getParty().getPartyMembers())
					if (member.getClassId().getId() == rtc.id && !member.isSkillDisabled(SkillHolder.getInstance().getSkillEntry(13, 1)))
						return true;
				break;
			default:
				break;
		}
		return false;
	}

	public List<Point> getPoints()
	{
		return points;
	}
	
	//TODO использовать только для чтения конкретно боту
	 // возможно применить другие рандомы
	public List<Point> getPointsRnd()
	{
		List <Point> tmp = points;
		tmp.forEach(p -> 
		{
			if (p!=null)
			{
				if (p.getRnd() !=0)
				{
					//применить рандом по точкам
					p.setX(p.getX() + Rnd.get(p.getRnd()));
					p.setY(p.getY() + Rnd.get(p.getRnd()));
				}
				if(p.getDelay() !=null)
				p.getDelay().setSeconds(p.getDelay().getSeconds() + Rnd.get(-p.getDelay().getRnd(),p.getDelay().getRnd()));
			}
		});
		return tmp;
	}
	
	public void setPoints(List<Point> points)
	{
		this.points = points;
	}
	
	public void addPoints(Point p)
	{
		points.add(p);
	}
	
	public boolean incorrectStartPoint() 
	{
		if (points==null || points.isEmpty())
			return true;
		Location s_point = points.get(0).getLoc();
		for (String zone_name : Config.Phantom_locked_zones) 
		{
			ZoneTemplate tmp_zone = ZoneHolder.getInstance().getTemplate(zone_name);
			if (tmp_zone!=null)
			{
				if (tmp_zone.getTerritory().isInside(s_point))
					return true;
			}
		}
		return false;
	}
}