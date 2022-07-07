package l2s.gameserver.model;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.EventTriggersManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class World
{
	private static final Logger _log = LoggerFactory.getLogger(World.class);

	public static final int MAP_MIN_X = (Config.GEO_X_FIRST - 20 << 15);
	public static final int MAP_MAX_X = ((Config.GEO_X_LAST - 20 + 1 << 15) - 1);
	public static final int MAP_MIN_Y = (Config.GEO_Y_FIRST - 18 << 15);
	public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 18 + 1 << 15) - 1;
	public static final int MAP_MIN_Z = Config.MAP_MIN_Z;
	public static final int MAP_MAX_Z = Config.MAP_MAX_Z;

	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;
	public static final int SHIFT_BY = Config.SHIFT_BY;
	public static final int SHIFT_BY_Z = Config.SHIFT_BY_Z;
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z >> SHIFT_BY_Z);

	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	private static final int REGIONS_Z = (MAP_MAX_Z >> SHIFT_BY_Z) + OFFSET_Z;

	private static final WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];

	public static void init()
	{
		_log.info("World: Creating regions: [" + (REGIONS_X + 1) + "][" + (REGIONS_Y + 1) + "][" + (REGIONS_Z + 1) + "].");
	}

	private static WorldRegion[][][] getRegions()
	{
		return _worldRegions;
	}

	private static int validX(int x)
	{
		if(x < 0)
			x = 0;
		else if(x > REGIONS_X)
			x = REGIONS_X;
		return x;
	}

	private static int validY(int y)
	{
		if(y < 0)
			y = 0;
		else if(y > REGIONS_Y)
			y = REGIONS_Y;
		return y;
	}

	private static int validZ(int z)
	{
		if(z < 0)
			z = 0;
		else if(z > REGIONS_Z)
			z = REGIONS_Z;
		return z;
	}

	public static int validCoordX(int x)
	{
		if(x < MAP_MIN_X)
			x = MAP_MIN_X + 1;
		else if(x > MAP_MAX_X)
			x = MAP_MAX_X - 1;
		return x;
	}

	public static int validCoordY(int y)
	{
		if(y < MAP_MIN_Y)
			y = MAP_MIN_Y + 1;
		else if(y > MAP_MAX_Y)
			y = MAP_MAX_Y - 1;
		return y;
	}

	public static int validCoordZ(int z)
	{
		if(z < MAP_MIN_Z)
			z = MAP_MIN_Z + 1;
		else if(z > MAP_MAX_Z)
			z = MAP_MAX_Z - 1;
		return z;
	}

	private static int regionX(int x)
	{
		return (x >> SHIFT_BY) + OFFSET_X;
	}

	private static int regionY(int y)
	{
		return (y >> SHIFT_BY) + OFFSET_Y;
	}

	private static int regionZ(int z)
	{
		return (z >> SHIFT_BY_Z) + OFFSET_Z;
	}

	private static int regionToCordX(int x)
	{
		return x - OFFSET_X << SHIFT_BY;
	}

	private static int regionToCordY(int y)
	{
		return y - OFFSET_Y << SHIFT_BY;
	}

	private static int regionToCordZ(int z)
	{
		return z - OFFSET_Z << SHIFT_BY_Z;
	}

	static boolean isNeighbour(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return x1 <= x2 + 1 && x1 >= x2 - 1 && y1 <= y2 + 1 && y1 >= y2 - 1 && z1 <= z2 + 1 && z1 >= z2 - 1;
	}

	public static WorldRegion getRegion(Location loc)
	{
		return getRegion(validX(regionX(loc.x)), validY(regionY(loc.y)), validZ(regionZ(loc.z)));
	}

	public static WorldRegion getRegion(GameObject obj)
	{
		return getRegion(validX(regionX(obj.getX())), validY(regionY(obj.getY())), validZ(regionZ(obj.getZ())));
	}

	private static WorldRegion getRegion(int x, int y, int z)
	{
		WorldRegion[][][] regions = getRegions();
		WorldRegion region = regions[x][y][z];
		if(region == null)
			synchronized (regions)
			{
				region = regions[x][y][z];
				if(region == null)
				{
					WorldRegion[] array = regions[x][y];
					WorldRegion worldRegion = new WorldRegion(x, y, z);
					array[z] = worldRegion;
					region = worldRegion;
				}
			}
		return region;
	}

	public static void addVisibleObject(GameObject object, Creature dropper)
	{
		if(object == null || !object.isVisible())
			return;
		WorldRegion region = getRegion(object);
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == region)
			return;
		if(currentRegion == null)
		{
			object.setCurrentRegion(region);
			region.addObject(object);
			for(int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); ++x)
				for(int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); ++y)
					for(int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); ++z)
						getRegion(x, y, z).addToPlayers(object, dropper);
		}
		else
		{
			currentRegion.removeObject(object);
			object.setCurrentRegion(region);
			region.addObject(object);
			for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
				for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
					for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
						if(!isNeighbour(region.getX(), region.getY(), region.getZ(), x, y, z))
							getRegion(x, y, z).removeFromPlayers(object);
			for(int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); ++x)
				for(int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); ++y)
					for(int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); ++z)
						if(!isNeighbour(currentRegion.getX(), currentRegion.getY(), currentRegion.getZ(), x, y, z))
							getRegion(x, y, z).addToPlayers(object, dropper);
		}
		if(object.isPlayer() && object.getReflection().isMain())
		{
			int regionMapX = MapUtils.regionX(regionToCordX(region.getX()));
			int regionMapY = MapUtils.regionY(regionToCordY(region.getY()));
			int currentRegionMapX = currentRegion == null ? 0 : MapUtils.regionX(regionToCordX(currentRegion.getX()));
			int currentRegionMapY = currentRegion == null ? 0 : MapUtils.regionY(regionToCordY(currentRegion.getY()));
			if(regionMapX != currentRegionMapX || regionMapY != currentRegionMapY)
				for(int triggerId : EventTriggersManager.getInstance().getTriggers(regionMapX, regionMapY))
					object.getPlayer().sendPacket(new EventTriggerPacket(triggerId, true));
		}
	}

	public static void removeVisibleObject(GameObject object)
	{
		if(object == null || object.isVisible())
			return;
		WorldRegion currentRegion;
		if((currentRegion = object.getCurrentRegion()) == null)
			return;
		object.setCurrentRegion(null);
		currentRegion.removeObject(object);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					getRegion(x, y, z).removeFromPlayers(object);
	}

	public static GameObject getAroundObjectById(GameObject object, int objId)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return null;
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.getObjectId() == objId)
							return obj;
		return null;
	}

	public static List<GameObject> getAroundObjects(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<GameObject> result = new ArrayList<>(128);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							result.add(obj);
						}
		return result;
	}

	public static List<GameObject> getAroundObjects(GameObject object, int radius, int height)
	{
		if(radius == -1)
			return new ArrayList<>(GameObjectsStorage.getObjects());
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		List<GameObject> result = new ArrayList<>(128);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add(obj);
						}
		return result;
	}

	public static List<MonsterInstance> getAroundMonsters(Location loc)
	{
		WorldRegion currentRegion = getRegion(loc);
		if(currentRegion == null)
			return Collections.emptyList();
		List<MonsterInstance> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isMonster())
						{
							if(obj.getReflectionId() != 0)
								continue;
							result.add((MonsterInstance) obj);
						}
		return result;
	}
	
	public static List<Creature> getAroundCharacters(Location loc, int objectId, int reflectionId)
	{
		WorldRegion currentRegion = getRegion(loc);
		if(currentRegion == null)
			return Collections.emptyList();
		List<Creature> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isCreature() && obj.getObjectId() != objectId)
						{
							if(obj.getReflectionId() != reflectionId)
								continue;
							result.add((Creature) obj);
						}
		return result;
	}

	public static List<Creature> getAroundCharacters(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<Creature> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isCreature() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							result.add((Creature) obj);
						}
		return result;
	}

	public static List<Creature> getAroundCharacters(Location loc, int objectId, int reflectionId, int radius, int height)
	{
		if(radius == -1)
		{
			List<Creature> characters = new ArrayList<>();
			for(GameObject object : GameObjectsStorage.getObjects())
				if(object.isCreature())
					characters.add((Creature) object);
			return characters;
		}
		WorldRegion currentRegion = getRegion(loc);
		if(currentRegion == null)
			return Collections.emptyList();
		int ox = loc.getX();
		int oy = loc.getY();
		int oz = loc.getZ();
		int sqrad = radius * radius;
		List<Creature> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isCreature() && obj.getObjectId() != objectId)
						{
							if(obj.getReflectionId() != reflectionId)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((Creature) obj);
						}
		return result;
	}

	public static List<Creature> getAroundCharacters(GameObject object, int radius, int height)
	{
		if(radius == -1)
		{
			List<Creature> characters = new ArrayList<>();
			for(GameObject o : GameObjectsStorage.getObjects())
				if(o.isCreature())
					characters.add((Creature) o);
			return characters;
		}
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		List<Creature> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isCreature() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((Creature) obj);
						}
		return result;
	}

	public static List<NpcInstance> getAroundNpc(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<NpcInstance> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isNpc() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							result.add((NpcInstance) obj);
						}
		return result;
	}

	public static List<NpcInstance> getAroundNpc(GameObject object, int radius, int height)
	{
		if(radius == -1)
			return new ArrayList<>(GameObjectsStorage.getNpcs());
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		List<NpcInstance> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isNpc() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((NpcInstance) obj);
						}
		return result;
	}

	public static List<NpcInstance> getAroundNpc(Location loc, WorldRegion region, int reflect, int radius, int height)
	{
		if(radius == -1)
			return new ArrayList<>(GameObjectsStorage.getNpcs());
		if(region == null)
			return Collections.emptyList();
		int ox = loc.x;
		int oy = loc.y;
		int oz = loc.z;
		int sqrad = radius * radius;
		List<NpcInstance> result = new ArrayList<>(64);
		for(int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); ++x)
			for(int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); ++y)
				for(int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isNpc())
						{
							if(obj.getReflectionId() != reflect)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((NpcInstance) obj);
						}
		return result;
	}

	public static List<Playable> getAroundPlayables(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<Playable> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isPlayable() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							result.add((Playable) obj);
						}
		return result;
	}

	public static List<Playable> getAroundPlayables(GameObject object, int radius, int height)
	{
		if(radius == -1)
		{
			List<Playable> playables = new ArrayList<>();
			for(GameObject o : GameObjectsStorage.getObjects())
				if(o.isPlayable())
					playables.add((Playable) o);
			return playables;
		}
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		List<Playable> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isPlayable() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((Playable) obj);
						}
		return result;
	}

	public static List<Player> getAroundPlayers(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<Player> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isPlayer() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							result.add((Player) obj);
						}
		return result;
	}

	public static List<Player> getAroundPhantom(GameObject object, int radius, int height)
	{
		if(radius == -1)
			return new ArrayList<>(GameObjectsStorage.getPlayers());
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		List<Player> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isPlayer()&& obj.isPhantom() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((Player) obj);
						}
		return result;
	}
	
	public static List<Player> getAroundPlayers(GameObject object, int radius, int height)
	{
		if(radius == -1)
			return new ArrayList<>(GameObjectsStorage.getPlayers());
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius * radius;
		List<Player> result = new ArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					for(GameObject obj : getRegion(x, y, z))
						if(obj.isPlayer() && obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(Math.abs(obj.getZ() - oz) > height)
								continue;
							int dx = Math.abs(obj.getX() - ox);
							if(dx > radius)
								continue;
							int dy = Math.abs(obj.getY() - oy);
							if(dy > radius)
								continue;
							if(dx * dx + dy * dy > sqrad)
								continue;
							result.add((Player) obj);
						}
		return result;
	}

	public static List<Player> getAroundObservers(Location loc)
	{
		WorldRegion currentRegion = getRegion(loc);
		if(currentRegion == null)
			return Collections.emptyList();
		List<Player> result = new ArrayList<>(64);
		int x1 = validX(currentRegion.getX() + 1);
		int y0 = validY(currentRegion.getY() - 1);
		int y2 = validY(currentRegion.getY() + 1);
		int z0 = validZ(currentRegion.getZ() - 1);
		int z2 = validZ(currentRegion.getZ() + 1);
		for(int x2 = validX(currentRegion.getX() - 1); x2 <= x1; ++x2)
			for(int y3 = y0; y3 <= y2; ++y3)
				for(int z3 = z0; z3 <= z2; ++z3)
					for(GameObject obj : getRegion(x2, y3, z3))
						if(obj.isObservePoint() || obj.isPlayer())
						{
							if(obj.isPlayer() && ((Player) obj).isInObserverMode())
								continue;
							result.add(obj.getPlayer());
						}
		return result;
	}

	public static List<Player> getAroundObservers(GameObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		List<Player> result = new ArrayList<>(64);
		int x1 = validX(currentRegion.getX() + 1);
		int y0 = validY(currentRegion.getY() - 1);
		int y2 = validY(currentRegion.getY() + 1);
		int z0 = validZ(currentRegion.getZ() - 1);
		int z2 = validZ(currentRegion.getZ() + 1);
		for(int x2 = validX(currentRegion.getX() - 1); x2 <= x1; ++x2)
			for(int y3 = y0; y3 <= y2; ++y3)
				for(int z3 = z0; z3 <= z2; ++z3)
					for(GameObject obj : getRegion(x2, y3, z3))
						if(obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							if(!obj.isObservePoint() && !obj.isPlayer())
								continue;
							if(obj.isPlayer() && ((Player) obj).isInObserverMode())
								continue;
							result.add(obj.getPlayer());
						}
		return result;
	}

	public static List<Player> getPlayersOnMap(int mapX, int mapY)
	{
		return getPlayersOnMap(mapX, mapY, 0, null);
	}

	public static List<Player> getPlayersOnMap(int mapX, int mapY, int offset)
	{
		return getPlayersOnMap(mapX, mapY, offset, null);
	}

	public static List<Player> getPlayersOnMap(int mapX, int mapY, int offset, Reflection reflection)
	{
		List<Player> list = new ArrayList<>();
		for(Player player : GameObjectsStorage.getPlayers())
		{
			if(reflection != null && player.getReflection() != reflection)
				continue;
			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);
			if(tx < mapX - offset || tx > mapX + offset || ty < mapY - offset || ty > mapY + offset)
				continue;
			list.add(player);
		}
		return list;
	}

	public static boolean isNeighborsEmpty(WorldRegion region)
	{
		for(int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); ++x)
			for(int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); ++y)
				for(int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); ++z)
					if(!getRegion(x, y, z).isEmpty())
						return false;
		return true;
	}

	public static void activate(WorldRegion currentRegion)
	{
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					getRegion(x, y, z).setActive(true);
	}

	public static void deactivate(WorldRegion currentRegion)
	{
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x)
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y)
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z)
					if(isNeighborsEmpty(getRegion(x, y, z)))
						getRegion(x, y, z).setActive(false);
	}

	public static void showObjectsToPlayer(Player player)
	{
		WorldRegion currentRegion = player.getCurrentRegion();
		if(currentRegion == null)
			return;
		int oid = player.getObjectId();
		int rid = player.getReflectionId();
		int x1 = validX(currentRegion.getX() + 1);
		int y0 = validY(currentRegion.getY() - 1);
		int y2 = validY(currentRegion.getY() + 1);
		int z0 = validZ(currentRegion.getZ() - 1);
		int z2 = validZ(currentRegion.getZ() + 1);
		for(int x2 = validX(currentRegion.getX() - 1); x2 <= x1; ++x2)
			for(int y3 = y0; y3 <= y2; ++y3)
				for(int z3 = z0; z3 <= z2; ++z3)
					for(GameObject obj : getRegion(x2, y3, z3))
						if(obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							player.sendPacket(player.addVisibleObject(obj, null));
						}
	}

	public static void removeObjectsFromPlayer(Player player)
	{
		WorldRegion currentRegion = player.getCurrentRegion();
		if(currentRegion == null)
			return;
		int oid = player.getObjectId();
		int rid = player.getReflectionId();
		int x1 = validX(currentRegion.getX() + 1);
		int y0 = validY(currentRegion.getY() - 1);
		int y2 = validY(currentRegion.getY() + 1);
		int z0 = validZ(currentRegion.getZ() - 1);
		int z2 = validZ(currentRegion.getZ() + 1);
		for(int x2 = validX(currentRegion.getX() - 1); x2 <= x1; ++x2)
			for(int y3 = y0; y3 <= y2; ++y3)
				for(int z3 = z0; z3 <= z2; ++z3)
					for(GameObject obj : getRegion(x2, y3, z3))
						if(obj.getObjectId() != oid)
						{
							if(obj.getReflectionId() != rid)
								continue;
							player.sendPacket(player.removeVisibleObject(obj, null));
						}
	}

	public static void removeObjectFromPlayers(GameObject object)
	{
		List<L2GameServerPacket> d = null;
		for(Player p : getAroundObservers(object))
			p.sendPacket(p.removeVisibleObject(object, d == null ? (d = object.deletePacketList(p)) : d));
	}

	static void addZone(Zone zone)
	{
		Reflection reflection = zone.getReflection();
		Territory territory = zone.getTerritory();
		if(territory == null)
		{
			_log.info("World: zone - " + zone.getName() + " not has territory.");
			return;
		}
		for(int x = validX(regionX(territory.getXmin())); x <= validX(regionX(territory.getXmax())); ++x)
			for(int y = validY(regionY(territory.getYmin())); y <= validY(regionY(territory.getYmax())); ++y)
				for(int z = validZ(regionZ(territory.getZmin())); z <= validZ(regionZ(territory.getZmax())); ++z)
				{
					WorldRegion region = getRegion(x, y, z);
					region.addZone(zone);
					for(GameObject obj : region)
						if(obj.isCreature())
						{
							if(obj.getReflection() != reflection)
								continue;
							((Creature) obj).updateZones();
						}
				}
		if(zone.getTemplate().getEventTriggerId() != 0)
			for(int x = MapUtils.regionX(territory.getXmin()); x <= MapUtils.regionX(territory.getXmax()); ++x)
				for(int y = MapUtils.regionY(territory.getYmin()); y <= MapUtils.regionY(territory.getYmax()); ++y)
					EventTriggersManager.getInstance().addTrigger(x, y, zone.getTemplate().getEventTriggerId());
	}

	static void removeZone(Zone zone)
	{
		Reflection reflection = zone.getReflection();
		Territory territory = zone.getTerritory();
		if(territory == null)
		{
			_log.info("World: zone - " + zone.getName() + " not has territory.");
			return;
		}
		for(int x = validX(regionX(territory.getXmin())); x <= validX(regionX(territory.getXmax())); ++x)
			for(int y = validY(regionY(territory.getYmin())); y <= validY(regionY(territory.getYmax())); ++y)
				for(int z = validZ(regionZ(territory.getZmin())); z <= validZ(regionZ(territory.getZmax())); ++z)
				{
					WorldRegion region = getRegion(x, y, z);
					region.removeZone(zone);
					for(GameObject obj : region)
						if(obj.isCreature())
						{
							if(obj.getReflection() != reflection)
								continue;
							((Creature) obj).updateZones();
						}
				}
		if(zone.getTemplate().getEventTriggerId() != 0)
			for(int x = MapUtils.regionX(territory.getXmin()); x <= MapUtils.regionX(territory.getXmax()); ++x)
				for(int y = MapUtils.regionY(territory.getYmin()); y <= MapUtils.regionY(territory.getYmax()); ++y)
					EventTriggersManager.getInstance().removeTrigger(x, y, zone.getTemplate().getEventTriggerId());
	}

	public static void getZones(Collection<Zone> inside, Location loc, Reflection reflection)
	{
		WorldRegion region = getRegion(loc);
		Zone[] zones = region.getZones();
		if(zones.length == 0)
			return;
		for(Zone zone : zones)
			if(zone.checkIfInZone(loc.x, loc.y, loc.z, reflection))
				inside.add(zone);
	}

	public static void getZones(Collection<Zone> inside, int x, int y, Reflection reflection)
	{
		WorldRegion[][] regionsByX = _worldRegions[validX(regionX(x))];
		if(regionsByX == null)
			return;
		WorldRegion[] regionsByXY = regionsByX[validY(regionY(y))];
		if(regionsByXY == null)
			return;
		for(WorldRegion region : regionsByXY)
			if(region != null)
			{
				Zone[] zones = region.getZones();
				if(zones.length != 0)
					for(Zone zone : zones)
						if(zone.isActive() && zone.getReflection() == reflection && zone.checkIfInZone(x, y))
							inside.add(zone);
			}
	}

	public static boolean isWater(Location loc, Reflection reflection)
	{
		return getWater(loc, reflection) != null;
	}

	public static Zone getWater(Location loc, Reflection reflection)
	{
		WorldRegion region = getRegion(loc);
		Zone[] zones = region.getZones();
		if(zones.length == 0)
			return null;
		for(Zone zone : zones)
			if(zone != null && zone.getType() == Zone.ZoneType.water && zone.checkIfInZone(loc.x, loc.y, loc.z, reflection))
				return zone;
		return null;
	}

	@Deprecated
	public static int[] getStats()
	{
		int[] ret = new int[32];
		for(int x = 0; x <= REGIONS_X; ++x)
			for(int y = 0; y <= REGIONS_Y; ++y)
				for(int z = 0; z <= REGIONS_Z; ++z)
				{
					int n = 0;
					++ret[n];
					WorldRegion region = _worldRegions[x][y][z];
					if(region != null)
					{
						if(region.isActive())
						{
							int n2 = 1;
							++ret[n2];
						}
						else
						{
							int n3 = 2;
							++ret[n3];
						}
						for(GameObject obj : region)
						{
							int n4 = 10;
							++ret[n4];
							if(obj.isCreature())
							{
								int n5 = 11;
								++ret[n5];
								if(obj.isPlayer())
								{
									Player p = (Player) obj;

									int n6 = 12;
									++ret[n6];

									if(!p.isInOfflineMode())
										if(obj.getFraction() == Fraction.FIRE)
											++ret[21];
										else if(obj.getFraction() == Fraction.WATER)
											++ret[22];

									if(!p.isInOfflineMode())
										continue;
									int n7 = 13;
									++ret[n7];
								}
								else if(obj.isNpc())
								{
									int n8 = 14;
									++ret[n8];
									if(obj.isMonster())
									{
										int n9 = 16;
										++ret[n9];
										if(obj.isMinion())
										{
											int n10 = 17;
											++ret[n10];
										}
									}
									NpcInstance npc = (NpcInstance) obj;
									if(!npc.hasAI() || !npc.getAI().isActive())
										continue;
									int n11 = 15;
									++ret[n11];
								}
								else if(obj.isPlayable())
								{
									int n12 = 18;
									++ret[n12];
								}
								else
								{
									if(!obj.isDoor())
										continue;
									int n13 = 19;
									++ret[n13];
								}
							}
							else
							{
								if(!obj.isItem())
									continue;
								int n14 = 20;
								++ret[n14];
							}
						}
					}
					else
					{
						int n15 = 3;
						++ret[n15];
					}
				}
		return ret;
	}

	/**
	 * Поиск игроков(без фантомов) относительно данной локации
	 * 
	 * @param Location
	 * @param radius
	 * @param height
	 */
	public static List <Player> getAroundRealPlayers(Location loc, int radius, int height)
	{
		WorldRegion currentRegion = getRegion(loc);
		if (currentRegion == null)
			return Collections.emptyList();
		
		int ox = loc.getX();
		int oy = loc.getY();
		int oz = loc.getZ();
		int sqrad = radius*radius;
		
		List <Player> result = new ArrayList <Player>(64);
		
		for(int x = validX(currentRegion.getX()-1); x <= validX(currentRegion.getX()+1); x++)
			for(int y = validY(currentRegion.getY()-1); y <= validY(currentRegion.getY()+1); y++)
				for(int z = validZ(currentRegion.getZ()-1); z <= validZ(currentRegion.getZ()+1); z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if (!obj.isPlayer())
							continue;
						if (obj.getPlayer().isPhantom())
							continue;
						if (Math.abs(obj.getZ()-oz) > height)
							continue;
						int dx = Math.abs(obj.getX()-ox);
						if (dx > radius)
							continue;
						int dy = Math.abs(obj.getY()-oy);
						if (dy > radius)
							continue;
						if (dx*dx+dy*dy > sqrad)
							continue;
						
						result.add((Player) obj);
					}
		return result;
	}

	/**
	 * Поиск игроков относительно данной локации
	 * 
	 * @param Location
	 * @param radius
	 * @param height
	 */
	public static List <Player> getAroundPlayers(Location loc, int radius, int height)
	{
		WorldRegion currentRegion = getRegion(loc);
		if (currentRegion == null)
			return Collections.emptyList();
		
		int ox = loc.getX();
		int oy = loc.getY();
		int oz = loc.getZ();
		int sqrad = radius*radius;
		
		List <Player> result = new ArrayList <Player>(64);
		
		for(int x = validX(currentRegion.getX()-1); x <= validX(currentRegion.getX()+1); x++)
			for(int y = validY(currentRegion.getY()-1); y <= validY(currentRegion.getY()+1); y++)
				for(int z = validZ(currentRegion.getZ()-1); z <= validZ(currentRegion.getZ()+1); z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if (!obj.isPlayer())
							continue;
						if (Math.abs(obj.getZ()-oz) > height)
							continue;
						int dx = Math.abs(obj.getX()-ox);
						if (dx > radius)
							continue;
						int dy = Math.abs(obj.getY()-oy);
						if (dy > radius)
							continue;
						if (dx*dx+dy*dy > sqrad)
							continue;
						
						result.add((Player) obj);
					}
		return result;
	}

	/**
	 * Поиск NPC относительно данной локации
	 * 
	 * @param Location
	 * @param radius
	 * @param height
	 */
	public static List <NpcInstance> getAroundNpc(Location loc, int radius, int height)
	{
		WorldRegion currentRegion = getRegion(loc);
		if (currentRegion == null)
			return Collections.emptyList();
		
		int ox = loc.getX();
		int oy = loc.getY();
		int oz = loc.getZ();
		int sqrad = radius*radius;
		
		List <NpcInstance> result = new ArrayList <NpcInstance>(64);
		
		for(int x = validX(currentRegion.getX()-1); x <= validX(currentRegion.getX()+1); x++)
			for(int y = validY(currentRegion.getY()-1); y <= validY(currentRegion.getY()+1); y++)
				for(int z = validZ(currentRegion.getZ()-1); z <= validZ(currentRegion.getZ()+1); z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if (!obj.isNpc())
							continue;
						if (Math.abs(obj.getZ()-oz) > height)
							continue;
						int dx = Math.abs(obj.getX()-ox);
						if (dx > radius)
							continue;
						int dy = Math.abs(obj.getY()-oy);
						if (dy > radius)
							continue;
						if (dx*dx+dy*dy > sqrad)
							continue;
						
						result.add((NpcInstance) obj);
					}
		return result;
	}
	
	/**
	 * Поиск игроков(без фантомов) относительно данной локации
	 * 
	 * @param Location
	 * @param radius
	 * @param height
	 */
	public static List <Player> getAroundPhantom(Location loc, int radius, int height)
	{
		WorldRegion currentRegion = getRegion(loc);
		if (currentRegion == null)
			return Collections.emptyList();
		
		int ox = loc.getX();
		int oy = loc.getY();
		int oz = loc.getZ();
		int sqrad = radius*radius;
		
		List <Player> result = new ArrayList <Player>(64);
		
		for(int x = validX(currentRegion.getX()-1); x <= validX(currentRegion.getX()+1); x++)
			for(int y = validY(currentRegion.getY()-1); y <= validY(currentRegion.getY()+1); y++)
				for(int z = validZ(currentRegion.getZ()-1); z <= validZ(currentRegion.getZ()+1); z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if (!obj.isPlayer())
							continue;
						if (!obj.getPlayer().isPhantom())
							continue;
						if (Math.abs(obj.getZ()-oz) > height)
							continue;
						int dx = Math.abs(obj.getX()-ox);
						if (dx > radius)
							continue;
						int dy = Math.abs(obj.getY()-oy);
						if (dy > radius)
							continue;
						if (dx*dx+dy*dy > sqrad)
							continue;
						
						result.add((Player) obj);
					}
		return result;
	}

	public static List <MonsterInstance> getAroundMonsters(GameObject object, int radius, int height)
	{
		WorldRegion currentRegion = object.getCurrentRegion();
		if (currentRegion == null)
			return Collections.emptyList();
		
		int oid = object.getObjectId();
		int rid = object.getReflectionId();
		int ox = object.getX();
		int oy = object.getY();
		int oz = object.getZ();
		int sqrad = radius*radius;
		
		List <MonsterInstance> result = new ArrayList <MonsterInstance>(64);
		
		for(int x = validX(currentRegion.getX()-1); x <= validX(currentRegion.getX()+1); x++)
			for(int y = validY(currentRegion.getY()-1); y <= validY(currentRegion.getY()+1); y++)
				for(int z = validZ(currentRegion.getZ()-1); z <= validZ(currentRegion.getZ()+1); z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if (!obj.isCreature() || !obj.isMonster() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
							continue;
						if (Math.abs(obj.getZ()-oz) > height)
							continue;
						int dx = Math.abs(obj.getX()-ox);
						if (dx > radius)
							continue;
						int dy = Math.abs(obj.getY()-oy);
						if (dy > radius)
							continue;
						if (dx*dx+dy*dy > sqrad)
							continue;
						
						result.add((MonsterInstance) obj);
					}
		return result;
	}
	

	public static List <MonsterInstance> getAroundMonsters(Location loc, int radius, int height)
	{
		WorldRegion currentRegion = getRegion(loc);
		if (currentRegion == null)
			return Collections.emptyList();
		
		int ox = loc.getX();
		int oy = loc.getY();
		int oz = loc.getZ();
		
		//int oid = object.getObjectId();
		//int rid = object.getReflectionId();

		int sqrad = radius*radius;
		
		List <MonsterInstance> result = new ArrayList <MonsterInstance>(64);
		
		for(int x = validX(currentRegion.getX()-1); x <= validX(currentRegion.getX()+1); x++)
			for(int y = validY(currentRegion.getY()-1); y <= validY(currentRegion.getY()+1); y++)
				for(int z = validZ(currentRegion.getZ()-1); z <= validZ(currentRegion.getZ()+1); z++)
					for(GameObject obj : getRegion(x, y, z))
					{
						if (!obj.isCreature() || !obj.isMonster() || obj.getReflection() != ReflectionManager.MAIN)
							continue;
						if (Math.abs(obj.getZ()-oz) > height)
							continue;
						int dx = Math.abs(obj.getX()-ox);
						if (dx > radius)
							continue;
						int dy = Math.abs(obj.getY()-oy);
						if (dy > radius)
							continue;
						if (dx*dx+dy*dy > sqrad)
							continue;
						
						result.add((MonsterInstance) obj);
					}
		return result;
	}
}
