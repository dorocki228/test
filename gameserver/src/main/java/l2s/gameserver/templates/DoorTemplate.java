package l2s.gameserver.templates;

import l2s.commons.geometry.Polygon;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.DoorAI;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoorTemplate extends CreatureTemplate
{
	private static final Logger _log;

	private static final Pattern MAP_X_Y_FROM_ID_PATTERN;

	public static final Constructor<DoorAI> DEFAULT_AI_CONSTRUCTOR;

	private final int _id;
	private final String _name;
	private final DoorType _doorType;
	private final boolean _unlockable;
	private final boolean _isHPVisible;
	private final boolean _opened;
	private final boolean _targetable;
	private final Polygon _polygon;
	private final Location _loc;
	private final int _key;
	private final int _openTime;
	private final int _rndTime;
	private final int _closeTime;
	private final int _masterDoor;
	private final int _mapX;
	private final int _mapY;
	private final StatsSet _aiParams;

	private final int angle;

	private Class<?> _classAI;
	private Constructor<?> _constructorAI;

	public DoorTemplate(StatsSet set)
	{
		super(set);
		_classAI = DoorAI.class;
		_constructorAI = DEFAULT_AI_CONSTRUCTOR;
		_id = set.getInteger("uid");
		_name = set.getString("name");
		_doorType = (DoorType) set.getEnum("door_type", (Class) DoorType.class, (Enum) DoorType.DOOR);
		_unlockable = set.getBool("unlockable", false);
		_isHPVisible = set.getBool("show_hp", false);
		_opened = set.getBool("opened", false);
		_targetable = set.getBool("targetable", true);
		_loc = (Location) set.get("pos");
		_polygon = (Polygon) set.get("shape");
		_key = set.getInteger("key", 0);
		_openTime = set.getInteger("open_time", 0);
		_rndTime = set.getInteger("random_time", 0);
		_closeTime = set.getInteger("close_time", 0);
		_masterDoor = set.getInteger("master_door", 0);
		_aiParams = (StatsSet) set.getObject("ai_params", StatsSet.EMPTY);
		Matcher mapMatcher = MAP_X_Y_FROM_ID_PATTERN.matcher(String.valueOf(_id));
		mapMatcher.find();
		_mapX = Integer.parseInt(mapMatcher.group(1));
		_mapY = Integer.parseInt(mapMatcher.group(2));
		angle = set.getInteger("angle", 0);
		setAI(set.getString("ai", "DoorAI"));
	}

	private void setAI(String ai)
	{
		Class<?> classAI;
		try
		{
			classAI = Class.forName("l2s.gameserver.ai." + ai);
		}
		catch(ClassNotFoundException e)
		{
			classAI = Scripts.getInstance().getClasses().get("ai.door." + ai);
		}
		if(classAI == null)
			_log.error("Not found ai class for ai: " + ai + ". DoorId: " + _id);
		else
		{
			_classAI = classAI;
			_constructorAI = _classAI.getConstructors()[0];
		}
		if(_classAI.isAnnotationPresent(Deprecated.class))
			_log.error("Ai type: " + ai + ", is deprecated. DoorId: " + _id);
	}

	public CharacterAI getNewAI(DoorInstance door)
	{
		try
		{
			return CharacterAI.class.cast(_constructorAI.newInstance(door));
		}
		catch(Exception e)
		{
			_log.error("Unable to create ai of doorId " + _id, e);
			return new DoorAI(door);
		}
	}

	@Override
	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public DoorType getDoorType()
	{
		return _doorType;
	}

	public boolean isUnlockable()
	{
		return _unlockable;
	}

	public boolean isHPVisible()
	{
		return _isHPVisible;
	}

	public Polygon getPolygon()
	{
		return _polygon;
	}

	public int getKey()
	{
		return _key;
	}

	public boolean isOpened()
	{
		return _opened;
	}

	public Location getLoc()
	{
		return _loc;
	}

	public int getOpenTime()
	{
		return _openTime;
	}

	public int getRandomTime()
	{
		return _rndTime;
	}

	public int getCloseTime()
	{
		return _closeTime;
	}

	public boolean isTargetable()
	{
		return _targetable;
	}

	public int getMasterDoor()
	{
		return _masterDoor;
	}

	public StatsSet getAIParams()
	{
		return _aiParams;
	}

	public int getMapX()
	{
		return _mapX;
	}

	public int getMapY()
	{
		return _mapY;
	}

	public int getAngle() {
		return angle;
	}

	static
	{
		_log = LoggerFactory.getLogger(DoorTemplate.class);
		MAP_X_Y_FROM_ID_PATTERN = Pattern.compile("([0-9]{2})([0-9]{2})[0-9]*", 32);
		DEFAULT_AI_CONSTRUCTOR = (Constructor<DoorAI>) CharacterAI.class.getConstructors()[0];
	}

	public enum DoorType
	{
		DOOR,
		WALL
    }
}