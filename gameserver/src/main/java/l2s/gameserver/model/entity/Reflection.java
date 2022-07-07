package l2s.gameserver.model.entity;

import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.database.mysql;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.EventTriggersManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.door.impl.MasterOnOpenCloseListenerImpl;
import l2s.gameserver.listener.reflection.OnReflectionCollapseListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.listener.zone.impl.*;
import l2s.gameserver.model.*;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Reflection
{
	private static final Logger _log = LoggerFactory.getLogger(Reflection.class);
	private static final AtomicInteger _nextId = new AtomicInteger();
	private final int _id;
	private String _name;
	private InstantZone _instance;
	private int _geoIndex;
	private Location _resetLoc;
	private Location _returnLoc;
	private Location _teleportLoc;
	protected Set<Spawner> _spawns;
	protected Set<GameObject> _objects;
	protected IntObjectMap<DoorInstance> _doors;
	protected Map<String, Zone> _zones;
	protected Map<String, List<Spawner>> _spawners;
	protected TIntHashSet _visitors;
	protected final Lock lock;
	protected int _playerCount;
	protected Party _party;
	private int _collapseIfEmptyTime;
	private boolean _isCollapseStarted;
	private ScheduledFuture<?> _collapseTask;
	private ScheduledFuture<?> _collapse1minTask;
	private ScheduledFuture<?> _hiddencollapseTask;
	private final ReflectionListenerList listeners;
	private StatsSet _variables;

	public Reflection()
	{
		this(_nextId.incrementAndGet());
	}

	protected Reflection(int id)
	{
		_name = "";
		_spawns = new HashSet<>();
		_objects = new HashSet<>();
		_doors = Containers.emptyIntObjectMap();
		_zones = Collections.emptyMap();
		_spawners = Collections.emptyMap();
		_visitors = new TIntHashSet();
		lock = new ReentrantLock();
		listeners = new ReflectionListenerList();
		_variables = StatsSet.EMPTY;
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public int getInstancedZoneId()
	{
		return _instance == null ? -1 : _instance.getId();
	}

	public void setParty(Party party)
	{
		_party = party;
	}

	public Party getParty()
	{
		return _party;
	}

	public void setCollapseIfEmptyTime(int value)
	{
		_collapseIfEmptyTime = value;
	}

	public String getName()
	{
		return _name;
	}

	protected void setName(String name)
	{
		_name = name;
	}

	public InstantZone getInstancedZone()
	{
		return _instance;
	}

	protected void setInstancedZone(InstantZone iz)
	{
		_instance = iz;
	}

	protected void setGeoIndex(int geoIndex)
	{
		_geoIndex = geoIndex;
	}

	public int getGeoIndex()
	{
		return _geoIndex;
	}

	public void setCoreLoc(Location l)
	{
		_resetLoc = l;
	}

	public Location getCoreLoc()
	{
		return _resetLoc;
	}

	public void setReturnLoc(Location l)
	{
		_returnLoc = l;
	}

	public Location getReturnLoc()
	{
		return _returnLoc;
	}

	public void setTeleportLoc(Location l)
	{
		_teleportLoc = l;
	}

	public Location getTeleportLoc()
	{
		return _teleportLoc;
	}

	public Collection<Spawner> getSpawns()
	{
		return _spawns;
	}

	public Collection<DoorInstance> getDoors()
	{
		return _doors.values();
	}

	public DoorInstance getDoor(int id)
	{
		return _doors.get(id);
	}

	public Zone getZone(String name)
	{
		return _zones.get(name);
	}

	public void startCollapseTimer(long timeInMillis)
	{
		if(isDefault())
		{
			new Exception("Basic reflection " + _id + " could not be collapsed!").printStackTrace();
			return;
		}
		lock.lock();
		try
		{
			if(_collapseTask != null)
			{
				_collapseTask.cancel(false);
				_collapseTask = null;
			}
			if(_collapse1minTask != null)
			{
				_collapse1minTask.cancel(false);
				_collapse1minTask = null;
			}
			_collapseTask = ThreadPoolManager.getInstance().schedule(() -> collapse(), timeInMillis);
			if(timeInMillis >= 60000L)
				_collapse1minTask = ThreadPoolManager.getInstance().schedule(() -> minuteBeforeCollapse(), timeInMillis - 60000L);
		}
		finally
		{
			lock.unlock();
		}
	}

	public void stopCollapseTimer()
	{
		lock.lock();
		try
		{
			if(_collapseTask != null)
			{
				_collapseTask.cancel(false);
				_collapseTask = null;
			}
			if(_collapse1minTask != null)
			{
				_collapse1minTask.cancel(false);
				_collapse1minTask = null;
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public long getDelayToCollapse()
	{
		if(_collapseTask != null)
			return _collapseTask.getDelay(TimeUnit.MILLISECONDS);
		return -1L;
	}

	public void minuteBeforeCollapse()
	{
		if(_isCollapseStarted)
			return;
		lock.lock();
		try
		{
			for(GameObject o : _objects)
				if(o.isPlayer())
					((Player) o).sendPacket(new SystemMessage(2107).addNumber(1));
		}
		finally
		{
			lock.unlock();
		}
	}

	public void collapse()
	{
		if(_id <= 0)
		{
			new Exception("Basic reflection " + _id + " could not be collapsed!").printStackTrace();
			return;
		}
		lock.lock();
		try
		{
			if(_isCollapseStarted)
				return;
			_isCollapseStarted = true;
			listeners.onCollapse();
			try
			{
				stopCollapseTimer();
				if(_hiddencollapseTask != null)
				{
					_hiddencollapseTask.cancel(false);
					_hiddencollapseTask = null;
				}
				for(Spawner s : _spawns)
					s.deleteAll();
				for(String group : _spawners.keySet())
					despawnByGroup(group);
				for(DoorInstance d : _doors.values())
					d.deleteMe();
				_doors.clear();
				for(Zone zone : _zones.values())
					zone.setActive(false);
				_zones.clear();
				EventTriggersManager.getInstance().removeTriggers(this);
				List<Player> teleport = new ArrayList<>();
				List<ObservePoint> observers = new ArrayList<>();
				List<GameObject> delete = new ArrayList<>();
				for(GameObject o : _objects)
					if(o.isPlayer())
						teleport.add((Player) o);
					else if(o.isObservePoint())
						observers.add((ObservePoint) o);
					else
					{
						if(o.isPlayable())
							continue;
						delete.add(o);
					}
				for(Player player : teleport)
				{
					if(player.getParty() != null && equals(player.getParty().getReflection()))
						player.getParty().setReflection(null);
					if(equals(player.getReflection()))
						if(getReturnLoc() != null)
						{
							ThreadPoolManager.getInstance().execute(() -> player.teleToLocation(getReturnLoc(), ReflectionManager.MAIN));
						}
						else
							player.setReflection(ReflectionManager.MAIN);
					onPlayerExit(player);
				}

				for(ObservePoint o2 : observers)
				{
					Player observer = o2.getPlayer();
					if(observer != null)
						observer.leaveObserverMode();
				}

				if(_party != null)
				{
					_party.setReflection(null);
					_party = null;
				}
				for(GameObject o : delete)
					o.deleteMe();
				_spawns.clear();
				_objects.clear();
				_visitors.clear();
				_doors.clear();
				_playerCount = 0;
				onCollapse();
			}
			finally
			{
				ReflectionManager.getInstance().remove(this);
				GeoEngine.FreeGeoIndex(getGeoIndex());
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	protected void onCollapse()
	{}

	public void addObject(GameObject o)
	{
		if(_isCollapseStarted)
			return;
		lock.lock();
		try
		{
			if(!_objects.add(o))
				return;
			if(o.isPlayer())
			{
				++_playerCount;
				_visitors.add(o.getObjectId());
				Player player = o.getPlayer();
				onPlayerEnter(player);
			}
			if(_hiddencollapseTask != null)
			{
				_hiddencollapseTask.cancel(false);
				_hiddencollapseTask = null;
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void removeObject(GameObject o)
	{
		if(_isCollapseStarted)
			return;
		lock.lock();
		try
		{
			if(!_objects.remove(o))
				return;
			if(o.isPlayer())
			{
				--_playerCount;
				onPlayerExit(o.getPlayer());
				if(_playerCount <= 0 && !isDefault() && _hiddencollapseTask == null && _collapseIfEmptyTime >= 0)
					if(_collapseIfEmptyTime == 0)
						collapse();
					else
						_hiddencollapseTask = ThreadPoolManager.getInstance().schedule(() -> collapse(), _collapseIfEmptyTime * 60 * 1000L);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void onPlayerEnter(Player player)
	{
		player.getInventory().validateItems();
		for(int triggerId : EventTriggersManager.getInstance().getTriggers(this, false))
			player.sendPacket(new EventTriggerPacket(triggerId, true));

		if(Arrays.stream(Olympiad.INSTANCES).noneMatch(id -> id == getInstancedZoneId()) && getInstancedZoneId() != 1003 && getInstancedZoneId() != 1004)
			HtmlUtils.sendHtmFile(player, "on-reflection-enter.htm");

		player.getListeners().onReflectionEnter(this);
	}

	public void onPlayerExit(Player player)
	{
		for(int triggerId : EventTriggersManager.getInstance().getTriggers(this, true))
			player.sendPacket(new EventTriggerPacket(triggerId, false));
		if(player.getActiveSubClass() != null)
			for(Servitor servitor : player.getServitors())
				if(servitor != null && (servitor.getNpcId() == 14916 || servitor.getNpcId() == 14917))
					servitor.unSummon(false);

		player.getListeners().onReflectionExit(this);
	}

	public List<Player> getPlayers()
	{
		lock.lock();
		List<Player> result = new ArrayList<>();
		try
		{
			for(GameObject o : _objects)
				if(o.isPlayer())
					result.add((Player) o);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public List<Creature> getPlayersAndObservers()
	{
		lock.lock();
		List<Creature> result = new ArrayList<>();
		try
		{
			for(GameObject o : _objects)
				if(o.isPlayer() || o.isObservePoint())
					result.add((Creature) o);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public List<Creature> getObservers()
	{
		lock.lock();
		List<Creature> result = new ArrayList<>();
		try
		{
			for(GameObject o : _objects)
				if(o.isObservePoint())
					result.add((Creature) o);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public List<NpcInstance> getNpcs()
	{
		lock.lock();
		List<NpcInstance> result = new ArrayList<>();
		try
		{
			for(GameObject o : _objects)
				if(o.isNpc())
					result.add((NpcInstance) o);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public List<NpcInstance> getAllByNpcId(int npcId, boolean onlyAlive)
	{
		lock.lock();
		List<NpcInstance> result = new ArrayList<>();
		try
		{
			for(GameObject o : _objects)
				if(o.isNpc())
				{
					NpcInstance npc = (NpcInstance) o;
					if(npcId != npc.getNpcId() || onlyAlive && npc.isDead())
						continue;
					result.add(npc);
				}
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	public boolean canChampions()
	{
		return _id <= 0;
	}

	public boolean isAutolootForced()
	{
		return false;
	}

	public boolean isCollapseStarted()
	{
		return _isCollapseStarted;
	}

	public void addSpawn(SimpleSpawner spawn)
	{
		if(spawn != null)
			_spawns.add(spawn);
	}

	public void fillSpawns(List<InstantZone.SpawnInfo> si)
	{
		if(si == null)
			return;
		for(InstantZone.SpawnInfo s : si)
			switch(s.getSpawnType())
			{
				case 0:
				{
					for(Location loc : s.getCoords())
					{
						SimpleSpawner c = new SimpleSpawner(s.getNpcId());
						c.setReflection(this);
						c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
						c.setAmount(s.getCount());
						c.setLoc(loc);
						c.doSpawn(true);
						if(s.getRespawnDelay() == 0)
							c.stopRespawn();
						else
							c.startRespawn();
						addSpawn(c);
					}
					continue;
				}
				case 1:
				{
					SimpleSpawner c = new SimpleSpawner(s.getNpcId());
					c.setReflection(this);
					c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
					c.setAmount(1);
					c.setLoc(s.getCoords().get(Rnd.get(s.getCoords().size())));
					c.doSpawn(true);
					if(s.getRespawnDelay() == 0)
						c.stopRespawn();
					else
						c.startRespawn();
					addSpawn(c);
					continue;
				}
				case 2:
				{
					SimpleSpawner c = new SimpleSpawner(s.getNpcId());
					c.setReflection(this);
					c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
					c.setAmount(s.getCount());
					c.setTerritory(s.getLoc());
					for(int j = 0; j < s.getCount(); ++j)
						c.doSpawn(true);
					if(s.getRespawnDelay() == 0)
						c.stopRespawn();
					else
						c.startRespawn();
					addSpawn(c);
					continue;
				}
			}
	}

	@SuppressWarnings("incomplete-switch")
	public void init(IntObjectMap<DoorTemplate> doors, Map<String, ZoneTemplate> zones)
	{
		if(!doors.isEmpty())
			_doors = new HashIntObjectMap<>(doors.size());
		for(DoorTemplate template : doors.values())
		{
			DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), template);
			door.setReflection(this);
			door.setInvul(true);
			door.spawnMe(template.getLoc());
			if(template.isOpened())
				door.openMe();
			_doors.put(template.getId(), door);
		}
		initDoors();
		if(!zones.isEmpty())
			_zones = new HashMap<>(zones.size());
		for(ZoneTemplate template : zones.values())
		{
			Zone zone = new Zone(template);
			zone.setReflection(this);
			switch(zone.getType())
			{
				case no_landing:
				case SIEGE:
				{
					zone.addListener(NoLandingZoneListener.STATIC);
					zone.addListener(SiegeEnterLeaveListener.STATIC);
					break;
				}
				case epic:
				{
					zone.addListener(EpicZoneListener.STATIC);
					break;
				}
				case RESIDENCE:
				{
					zone.addListener(ResidenceEnterLeaveListener.STATIC);
					break;
				}
				case FISHING:
				{
					zone.addListener(FishingZoneListener.STATIC);
					break;
				}
				case no_party:
				{
					zone.addListener(NoPartyZoneListener.STATIC);
					break;
				}
				case block_siege_summon:
				{
					zone.addListener(BlockSiegeSummonZoneListener.STATIC);
					break;
				}
			}

			String impl = template.getParams().getString("listener", null);

			if(impl != null)
			{
				try
				{
					Constructor<?> constructor = Class.forName("l2s.gameserver.listener.zone.impl." + impl + "Listener").getConstructors()[0];
					if(constructor != null)
					{
						OnZoneEnterLeaveListener listener = (OnZoneEnterLeaveListener) constructor.newInstance();
						zone.addListener(listener);
					}
				}
				catch(Exception e)
				{
					_log.warn("Reflection: can't find zone listener: " + impl + ". ", e);
				}
			}

			if(template.getPresentSceneMovie() != null)
				zone.addListener(new PresentSceneMovieZoneListener(template.getPresentSceneMovie()));
			if(template.isEnabled())
				zone.setActive(true);
			_zones.put(template.getName(), zone);
		}
		onCreate();
	}

	@SuppressWarnings("incomplete-switch")
	private void init0(IntObjectMap<InstantZone.DoorInfo> doors, Map<String, InstantZone.ZoneInfo> zones)
	{
		if(!doors.isEmpty())
			_doors = new HashIntObjectMap<>(doors.size());
		for(InstantZone.DoorInfo info : doors.values())
		{
			DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), info.getTemplate());
			door.setReflection(this);
			if(info.isInvul() && !door.isInvul())
				door.setInvul(true);
			else if(!info.isInvul() && door.isInvul())
				door.setInvul(false);
			door.spawnMe(info.getTemplate().getLoc());
			if(info.isOpened())
				door.openMe();
			_doors.put(info.getTemplate().getId(), door);
		}
		initDoors();
		if(!zones.isEmpty())
			_zones = new HashMap<>(zones.size());
		for(InstantZone.ZoneInfo t : zones.values())
		{
			Zone zone = new Zone(t.getTemplate());
			zone.setReflection(this);
			switch(zone.getType())
			{
				case no_landing:
				case SIEGE:
				{
					zone.addListener(NoLandingZoneListener.STATIC);
					zone.addListener(SiegeEnterLeaveListener.STATIC);
					break;
				}
				case epic:
				{
					zone.addListener(EpicZoneListener.STATIC);
					break;
				}
				case RESIDENCE:
				{
					zone.addListener(ResidenceEnterLeaveListener.STATIC);
					break;
				}
				case FISHING:
				{
					zone.addListener(FishingZoneListener.STATIC);
					break;
				}
				case no_party:
				{
					zone.addListener(NoPartyZoneListener.STATIC);
					break;
				}
				case block_siege_summon:
				{
					zone.addListener(BlockSiegeSummonZoneListener.STATIC);
					break;
				}
			}

			String impl = zone.getParams().getString("listener", null);

			if(impl != null)
			{
				try
				{
					Constructor<?> constructor = Class.forName("l2s.gameserver.listener.zone.impl." + impl + "Listener").getConstructors()[0];
					if(constructor != null)
					{
						OnZoneEnterLeaveListener listener = (OnZoneEnterLeaveListener) constructor.newInstance();
						zone.addListener(listener);
					}
				}
				catch(Exception e)
				{
					_log.warn("Reflection: can't find zone listener: " + impl + ". ", e);
				}
			}

			if(t.getTemplate().getPresentSceneMovie() != null)
				zone.addListener(new PresentSceneMovieZoneListener(t.getTemplate().getPresentSceneMovie()));
			if(t.isActive())
				zone.setActive(true);
			_zones.put(t.getTemplate().getName(), zone);
		}
	}

	private void initDoors()
	{
		for(DoorInstance door : _doors.values())
			if(door.getTemplate().getMasterDoor() > 0)
			{
				DoorInstance masterDoor = getDoor(door.getTemplate().getMasterDoor());
				masterDoor.addListener(new MasterOnOpenCloseListenerImpl(door));
			}
	}

	public void openDoor(int doorId)
	{
		DoorInstance door = _doors.get(doorId);
		if(door != null)
			door.openMe();
	}

	public void closeDoor(int doorId)
	{
		DoorInstance door = _doors.get(doorId);
		if(door != null)
			door.closeMe();
	}

	public void clearReflection(int timeInMinutes, boolean message)
	{
		if(isDefault())
			return;
		for(NpcInstance n : getNpcs())
			n.deleteMe();
		startCollapseTimer(timeInMinutes * 60 * 1000L);
		if(message)
			for(Player pl : getPlayers())
				if(pl != null)
					pl.sendPacket(new SystemMessage(2106).addNumber(timeInMinutes));
	}

	public NpcInstance addSpawnWithoutRespawn(int npcId, Location loc, int randomOffset)
	{
		if(_isCollapseStarted)
			return null;
		Location newLoc;
		if(randomOffset > 0)
			newLoc = Location.findPointToStay(loc, 0, randomOffset, getGeoIndex()).setH(loc.h);
		else
			newLoc = loc;
		return NpcUtils.spawnSingle(npcId, newLoc, this);
	}

	public NpcInstance addSpawnWithRespawn(int npcId, Location loc, int randomOffset, int respawnDelay)
	{
		if(_isCollapseStarted)
			return null;
		SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(npcId));
		sp.setLoc(randomOffset > 0 ? Location.findPointToStay(loc, 0, randomOffset, getGeoIndex()) : loc);
		sp.setReflection(this);
		sp.setAmount(1);
		sp.setRespawnDelay(respawnDelay);
		sp.doSpawn(true);
		sp.startRespawn();
		return sp.getLastSpawn();
	}

	public boolean isMain()
	{
		return getId() == 0;
	}

	public boolean isDefault()
	{
		return getId() <= 0;
	}

	public int[] getVisitors()
	{
		return _visitors.toArray();
	}

	public void removeVisitors(Player player)
	{
		_visitors.remove(player.getObjectId());
	}

	public boolean isVisitor(Player player)
	{
		return Arrays.stream(getVisitors())
				.anyMatch(charId -> player.getObjectId() == charId);
	}

	public void setReenterTime(long time)
	{
		lock.lock();
		int[] players = null;
		try
		{
			players = _visitors.toArray();
		}
		finally
		{
			lock.unlock();
		}
		if(players != null)
			for(int objectId : players)
				try
				{
					Player player = GameObjectsStorage.getPlayer(objectId);
					if(player != null)
						player.setInstanceReuse(getInstancedZoneId(), time);
					else
						mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", objectId, getInstancedZoneId(), time);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	}

	protected void onCreate()
	{
		ReflectionManager.getInstance().add(this);
	}

	public static Reflection createReflection(int id)
	{
		if(id > 0)
			throw new IllegalArgumentException("id should be <= 0");
		return new Reflection(id);
	}

	public void init(InstantZone instantZone)
	{
		setName(instantZone.getName());
		setInstancedZone(instantZone);

		if(instantZone.getMapX() >= 0)
		{
			int geoIndex = GeoEngine.NextGeoIndex(instantZone.getMapX(), instantZone.getMapY(), getId());
			setGeoIndex(geoIndex);
		}

		setTeleportLoc(instantZone.getTeleportCoord());
		if(instantZone.getReturnCoords() != null)
			setReturnLoc(instantZone.getReturnCoords());
		fillSpawns(instantZone.getSpawnsInfo());
		if(!instantZone.getSpawns().isEmpty())
		{
			_spawners = new HashMap<>(instantZone.getSpawns().size());
			for(Map.Entry<String, InstantZone.SpawnInfo2> entry : instantZone.getSpawns().entrySet())
			{
				List<Spawner> spawnList = new ArrayList<>(entry.getValue().getTemplates().size());
				_spawners.put(entry.getKey(), spawnList);
				for(SpawnTemplate template : entry.getValue().getTemplates())
				{
					HardSpawner spawner = new HardSpawner(template);
					spawnList.add(spawner);
					spawner.setAmount(template.getCount());
					spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
					spawner.setReflection(this);
					spawner.setRespawnTime(0);
				}
				if(entry.getValue().isSpawned())
					spawnByGroup(entry.getKey());
			}
		}
		init0(instantZone.getDoors(), instantZone.getZones());
		setCollapseIfEmptyTime(instantZone.getCollapseIfEmpty());
		if(instantZone.getTimelimit() > 0)
			startCollapseTimer(instantZone.getTimelimit() * 60 * 1000L);
		onCreate();
	}

	public List<Spawner> spawnByGroup(String name)
	{
		List<Spawner> list = _spawners.get(name);
		if(list == null)
		{
			if(_spawners.isEmpty())
				_spawners = new HashMap<>(1);
			List<SpawnTemplate> templates = SpawnHolder.getInstance().getSpawn(name);
			List<Spawner> spawnList = new ArrayList<>(templates.size());
			_spawners.put(name, spawnList);
			for(SpawnTemplate template : templates)
			{
				HardSpawner spawner = new HardSpawner(template);
				spawnList.add(spawner);
				spawner.setAmount(template.getCount());
				spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
				spawner.setReflection(this);
				spawner.setRespawnTime(0);
				spawner.init();
			}
			return spawnList;
		}
		for(Spawner s : list)
			s.init();
		return list;
	}

	public void despawnByGroup(String name)
	{
		List<Spawner> list = _spawners.get(name);
		if(list != null)
			for(Spawner s : list)
				s.deleteAll();
	}

	public void despawnAll()
	{
		for(List<Spawner> list : _spawners.values())
			for(Spawner s : list)
				s.deleteAll();
	}

	public List<Spawner> getSpawners(String group)
	{
		List<Spawner> list = _spawners.get(group);
		return list == null ? Collections.emptyList() : list;
	}

	public Collection<Zone> getZones()
	{
		return _zones.values();
	}

	public <T extends Listener<Reflection>> boolean addListener(T listener)
	{
		return listeners.add(listener);
	}

	public <T extends Listener<Reflection>> boolean removeListener(T listener)
	{
		return listeners.remove(listener);
	}

	public void clearVisitors()
	{
		_visitors.clear();
	}

	public void broadcastPacket(L2GameServerPacket... packets)
	{
		for(Player player : getPlayers())
			if(player != null)
				player.sendPacket(packets);
	}

	public void broadcastPacket(List<L2GameServerPacket> packets)
	{
		for(Player player : getPlayers())
			if(player != null)
				player.sendPacket(packets);
	}

	public final StatsSet getVariables()
	{
		return _variables;
	}

	public final void setVariable(String name, Object value)
	{
		if(_variables == StatsSet.EMPTY)
			_variables = new StatsSet();
		_variables.set(name, value);
	}

	public boolean addEventTrigger(int triggerId)
	{
		return EventTriggersManager.getInstance().addTrigger(this, triggerId);
	}

	public boolean removeEventTrigger(int triggerId)
	{
		return EventTriggersManager.getInstance().removeTrigger(this, triggerId);
	}

	public boolean canModifyParty()
	{
		// TODO засунуть в парсер
		return getInstancedZoneId() != 79;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(!(o instanceof Reflection))
			return false;
		Reflection that = (Reflection) o;
		return _id == that._id;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_id);
	}

	public class ReflectionListenerList extends ListenerList<Reflection>
	{
		public void onCollapse()
		{
			if(!getListeners().isEmpty())
				for(Listener<Reflection> listener : getListeners())
					((OnReflectionCollapseListener) listener).onReflectionCollapse(Reflection.this);
		}
	}
}
