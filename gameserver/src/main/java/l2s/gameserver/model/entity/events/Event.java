package l2s.gameserver.model.entity.events;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.logging.LoggerObject;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.event.OnStartStopListener;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.bbs.CommunityBoardEntry;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.IRegistrationEvent;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.InitableObject;
import l2s.gameserver.model.entity.events.objects.SpawnableObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TimeUtils;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

public abstract class Event extends LoggerObject implements IRegistrationEvent
{
    public static final String EVENT = "event";

    protected final IntObjectMap<List<EventAction>> _onTimeActions;
    protected final List<EventAction> _onStartActions;
    protected final List<EventAction> _onStopActions;
    protected final List<EventAction> _onInitActions;

    protected final Map<Object, List<Object>> _objects;

    protected final int _id;
    protected final String _name;

    protected final ListenerListImpl _listenerList;

    protected IntObjectMap<ItemInstance> _banishedItems;

    protected List<Future<?>> _tasks;

    private final List<Future<?>> _spawnTasks;

    private Map<String, CommunityBoardEntry> communityBoardEntries;

    protected Event(MultiValueSet<String> set)
    {
        this(set.getInteger("id"), set.getString("name"));
    }

    protected Event(int id, String name)
    {
        _onTimeActions = new TreeIntObjectMap<>();
        _onStartActions = new ArrayList<>(0);
        _onStopActions = new ArrayList<>(0);
        _onInitActions = new ArrayList<>(0);
        _objects = new HashMap<>(0);
        _listenerList = new ListenerListImpl();
        _tasks = null;
        _spawnTasks = new ArrayList<>(0);
        _id = id;
        _name = name;
        communityBoardEntries = new HashMap<>(1);
        GameServer.getInstance().getListeners().add((OnShutdownListener) this::shutdownServer);
    }

    public void beforeInitialization() {

    }

    public void afterInitialization() {

    }

    public void initEvent()
    {
        callActions(_onInitActions);
        reCalcNextTime(true);
        printInfo();
    }

    public void startEvent()
    {
        callActions(_onStartActions);
        _listenerList.onStart();
    }

    public void stopEvent(boolean force)
    {
        callActions(_onStopActions);
        _listenerList.onStop();
    }

    public void printInfo()
    {
        long startSiegeMillis = startTimeMillis();
        if(startSiegeMillis == 0L)
            info(getName() + " time - undefined");
        else
            info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
    }

    public boolean overrideOnScreenMessage(Player player)
    {
        return false;
    }

    public Optional<String> getOnScreenMessage(Player player)
    {
        return Optional.empty();
    }

    public long onScreenMessageUpdateTime(TimeUnit timeUnit)
    {
        return timeUnit.convert(30, TimeUnit.SECONDS);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
    }

    protected void callActions(List<EventAction> actions)
    {
        for(EventAction action : actions)
            action.call(this);
    }

    public void addOnStartActions(List<EventAction> start)
    {
        _onStartActions.addAll(start);
    }

    public void addOnStopActions(List<EventAction> start)
    {
        _onStopActions.addAll(start);
    }

    public void addOnInitActions(List<EventAction> start)
    {
        _onInitActions.addAll(start);
    }

    public void addOnTimeAction(int time, EventAction action)
    {
        List<EventAction> list = _onTimeActions.get(time);
        if(list != null)
            list.add(action);
        else
        {
            List<EventAction> actions = new ArrayList<>(1);
            actions.add(action);
            _onTimeActions.put(time, actions);
        }
    }

    public void addOnTimeActions(int time, List<EventAction> actions)
    {
        if(actions.isEmpty())
            return;
        for(EventAction action : actions)
            addOnTimeAction(time, action);
    }

    public void timeActions(int time)
    {
        List<EventAction> actions = _onTimeActions.get(time);
        if(actions == null)
        {
            info("Undefined time : " + time);
            return;
        }
        callActions(actions);
    }

    public int[] timeActions()
    {
        return _onTimeActions.keySet().toArray();
    }

    public synchronized void registerActions()
    {
        registerActions(true);
    }

    public synchronized void registerActions(boolean useStartTimeMillis)
    {
        long t = useStartTimeMillis ? startTimeMillis() : System.currentTimeMillis();
        if(t == 0L)
            return;
        if(_tasks == null)
            _tasks = new ArrayList<>(_onTimeActions.size());
        long c = System.currentTimeMillis();
        for(int key : _onTimeActions.keySet().toArray())
        {
            long time = t + key * 1000L;
            EventTimeTask wrapper = new EventTimeTask(this, key);
            if(time <= c)
                ThreadPoolManager.getInstance().execute(wrapper);
            else
            {
                ScheduledFuture<?> task = ThreadPoolManager.getInstance().schedule(wrapper, time - c);
                if(task != null)
                {
                    _tasks.add(task);
                }
            }
        }
    }

    public synchronized void clearActions()
    {
        if(_tasks == null)
            return;
        for(Future<?> f : _tasks)
            f.cancel(false);
        _tasks.clear();
    }

    public boolean containsObjects(Object name)
    {
        return _objects.get(name) != null;
    }

    public <O> List<O> getObjects(Object name)
    {
        List<Object> objects = _objects.get(name);
        return (List<O>) (objects == null ? Collections.emptyList() : objects);
    }

    public <O> O getFirstObject(Object name)
    {
        List<Object> objects = getObjects(name);
        return (O) (!objects.isEmpty() ? objects.get(0) : null);
    }

    public void addObject(Object name, Object object)
    {
        if(object == null)
            return;
        List<Object> list = _objects.get(name);
        if(list != null)
            list.add(object);
        else
        {
            list = new CopyOnWriteArrayList<>();
            list.add(object);
            _objects.put(name, list);
        }
    }

    public void removeObject(Object name, Object o)
    {
        if(o == null)
            return;
        List<Object> list = _objects.get(name);
        if(list != null)
            list.remove(o);
    }

    public <O> List<O> removeObjects(Object name)
    {
        List<Object> objects = _objects.remove(name);
        return (List<O>) (objects == null ? Collections.emptyList() : objects);
    }

    public void addObjects(Object name, List<?> objects)
    {
        if(objects.isEmpty())
            return;
        List<Object> list = _objects.get(name);
        if(list != null)
            list.addAll(objects);
        else
            _objects.put(name, new CopyOnWriteArrayList<>(objects));
    }

    public Map<Object, List<Object>> getObjects()
    {
        return _objects;
    }

    public void spawnAction(Object name, boolean spawn)
    {
        spawnAction(name, 0, spawn);
    }

    public void spawnAction(Object name, int delay, boolean spawn)
    {
        List<Object> objects = getObjects(name);
        if(objects.isEmpty())
        {
            info("Undefined objects: " + name, new Exception());
            return;
        }
        for(Object object : objects)
            if(object instanceof SpawnableObject)
            {
                if(delay > 0)
                {
                    if(spawn)
                        _spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> ((SpawnableObject) object).spawnObject(this), TimeUnit.SECONDS.toMillis(delay)));
                    else
                        _spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> ((SpawnableObject) object).despawnObject(this), TimeUnit.SECONDS.toMillis(delay)));
                }
                else
                {
                    if(spawn)
                        ((SpawnableObject) object).spawnObject(this);
                    else
                        ((SpawnableObject) object).despawnObject(this);

                }
            }
    }

    public synchronized void clearSpawnActions()
    {
        if(_spawnTasks == null)
            return;
        for(Future<?> f : _spawnTasks)
            f.cancel(false);
        _spawnTasks.clear();
    }

    public void respawnAction(Object name)
    {
        List<Object> objects = getObjects(name);
        if(objects.isEmpty())
        {
            info("Undefined objects: " + name, new Exception());
            return;
        }
        for(Object object : objects)
            if(object instanceof SpawnableObject)
                ((SpawnableObject) object).respawnObject(this);
    }

    public void doorAction(Object name, boolean open)
    {
        List<Object> objects = getObjects(name);
        if(objects.isEmpty())
        {
            info("Undefined objects: " + name, new Exception());
            return;
        }
        for(Object object : objects)
            if(object instanceof DoorObject)
                if(open)
                    ((DoorObject) object).open(this);
                else
                    ((DoorObject) object).close(this);
    }

    public void zoneAction(Object name, boolean active)
    {
        List<Object> objects = getObjects(name);
        if(objects.isEmpty())
        {
            info("Undefined objects: " + name, new Exception());
            return;
        }
        for(Object object : objects)
            if(object instanceof ZoneObject)
                ((ZoneObject) object).setActive(active, this);
            else if(containsObjects(object))
            {
                for(Object object2 : getObjects(object))
                    if(object2 instanceof ZoneObject)
                        ((ZoneObject) object2).setActive(active, this);
            }
    }

    public void initAction(Object name)
    {
        List<Object> objects = getObjects(name);
        if(objects.isEmpty())
        {
            info("Undefined objects: " + name, new Exception());
            return;
        }
        for(Object object : objects)
            if(object instanceof InitableObject)
                ((InitableObject) object).initObject(this);
    }

    public void action(String name, boolean start)
    {
        if("event".equalsIgnoreCase(name))
            if(start)
                startEvent();
            else
                stopEvent(false);
    }

    public void refreshAction(Object name)
    {
        List<Object> objects = getObjects(name);
        if(objects.isEmpty())
        {
            info("Undefined objects: " + name, new Exception());
            return;
        }
        for(Object object : objects)
            if(object instanceof SpawnableObject)
                ((SpawnableObject) object).refreshObject(this);
    }

    public abstract void reCalcNextTime(boolean p0);

    public abstract EventType getType();

    protected abstract long startTimeMillis();

    public ZonedDateTime startDateTime()
    {
        Instant instant = Instant.ofEpochMilli(startTimeMillis());
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public void broadcastToWorld(IBroadcastPacket packet)
    {
        for(Player player : GameObjectsStorage.getPlayers())
            if(player != null)
                player.sendPacket(packet);
    }

    public void broadcastToWorld(L2GameServerPacket packet)
    {
        for(Player player : GameObjectsStorage.getPlayers())
            if(player != null)
                player.sendPacket(packet);
    }

    public int getId()
    {
        return _id;
    }

    public String getName()
    {
        return _name;
    }

    public GameObject getCenterObject()
    {
        return null;
    }

    public Reflection getReflection()
    {
        return ReflectionManager.MAIN;
    }

    public int getRelation(Player thisPlayer, Player target, int oldRelation)
    {
        return oldRelation;
    }

    public int getUserRelation(Player thisPlayer, int oldRelation)
    {
        return oldRelation;
    }

    public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
    {
    }

    public EventRestartLoc getRestartLoc(Player player, RestartType type)
    {
        return null;
    }

    public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force, boolean nextAttackCheck)
    {
        return false;
    }

    public boolean canUseAcp(Player actor) {
        return false;
    }

    public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
    {
        return null;
    }

    public SystemMsg canUseItem(Player player, ItemInstance item)
    {
        return null;
    }

    public boolean canUseSkill(Creature caster, Creature target, Skill skill)
    {
        return true;
    }

    public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
    {
        throw new UnsupportedOperationException(getClass().getName() + " not implemented canResurrect");
    }

    public boolean canUseTeleport(Player player)
    {
        return true;
    }

    public boolean canUseCommunityFunctions(Player player)
    {
        return true;
    }

    public boolean canJoinParty(Player inviter, Player invited)
    {
        return true;
    }

    public boolean isInProgress()
    {
        return false;
    }

    public void findEvent(Player player)
    {
    }

    public void announce(int a, SystemMsg msgId)
    {
        throw new UnsupportedOperationException(getClass().getName() + " not implemented announce");
    }

    public void teleportPlayers(String teleportWho)
    {
        throw new UnsupportedOperationException(getClass().getName() + " not implemented teleportPlayers");
    }

    public void teleportPlayerToEvent(Player player)
    {
    }

    public boolean ifVar(String name)
    {
        throw new UnsupportedOperationException(getClass().getName() + " not implemented ifVar");
    }

    public List<Player> itemObtainPlayers()
    {
        throw new UnsupportedOperationException(getClass().getName() + " not implemented itemObtainPlayers");
    }

    public void giveItem(Player player, int itemId, long count)
    {
        switch(itemId)
        {
            case -300:
            {
                player.setFame(player.getFame() + (int) count, toString(), true);
                break;
            }
            default:
            {
                ItemFunctions.addItem(player, itemId, count);
                break;
            }
        }
    }

    public List<Player> broadcastPlayers(int range)
    {
        throw new UnsupportedOperationException(getClass().getName() + " not implemented broadcastPlayers");
    }

    public void onAddEvent(GameObject o)
    {
    }

    public void onRemoveEvent(GameObject o)
    {
    }

    public void addBanishItem(ItemInstance item)
    {
        if(_banishedItems == null)
            _banishedItems = new CHashIntObjectMap<>();
        _banishedItems.put(item.getObjectId(), item);
    }

    public void removeBanishItems()
    {
        if(_banishedItems == null)
            return;

        Iterator<IntObjectPair<ItemInstance>> iterator = _banishedItems.entrySet().iterator();
        while(iterator.hasNext())
        {
            IntObjectPair<ItemInstance> entry = iterator.next();
            iterator.remove();
            ItemInstance item = ItemsDAO.getInstance().load(entry.getKey());
            if(item != null)
            {
                if(item.getOwnerId() > 0)
                {
                    GameObject object = GameObjectsStorage.findObject(item.getOwnerId());
                    if(object != null && object.isPlayable())
                    {
                        ((Playable) object).getInventory().destroyItem(item);
                        object.getPlayer().sendPacket(SystemMessagePacket.removeItems(item));
                    }
                }
                item.delete();
            }
            else
                item = entry.getValue();
            item.deleteMe();
        }
    }

    public void addListener(Listener<Event> l)
    {
        _listenerList.add(l);
    }

    public void removeListener(Listener<Event> l)
    {
        _listenerList.remove(l);
    }

    public void cloneTo(Event e)
    {
        e._onInitActions.addAll(_onInitActions);
        e._onStartActions.addAll(_onStartActions);
        e._onStopActions.addAll(_onStopActions);
        for(IntObjectPair<List<EventAction>> entry : _onTimeActions.entrySet())
            e.addOnTimeActions(entry.getKey(), entry.getValue());
    }

    public String getVisibleName(Player player, Player observer)
    {
        return null;
    }

    public String getVisibleTitle(Player player, Player observer)
    {
        return null;
    }

    public Integer getVisibleNameColor(Player player, Player observer)
    {
        return null;
    }

    public Integer getVisibleTitleColor(Player player, Player observer)
    {
        return null;
    }

    public boolean isPledgeVisible(Player player, Player observer)
    {
        return true;
    }

    public boolean checkCondition(Creature creature, Class<? extends Condition> conditionClass)
    {
        return true;
    }

    public boolean isInZoneBattle(Creature creature)
    {
        return false;
    }

    public boolean isInZoneBattle(int x, int y, int z)
    {
        return false;
    }

    public void giveOwnerCrp(int count)
    {
    }

    protected void addCommunityBoardEntry(String name, CommunityBoardEntry communityBoardEntry) {
        if (communityBoardEntries.containsKey(name)) {
            removeCommunityBoardEntry(name);
        }

        communityBoardEntries.put(name, communityBoardEntry);
        communityBoardEntry.register();
    }

    protected void removeCommunityBoardEntry(String name) {
        CommunityBoardEntry communityBoardEntry = communityBoardEntries.remove(name);
        if (communityBoardEntry != null)
            communityBoardEntry.unregister();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(!(o instanceof Event))
            return false;
        Event event = (Event) o;
        return _id == event._id &&
                getType() == event.getType();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(_id, getType());
    }

    public boolean canParty(Player requestor, Player activeChar) {
        return true;
    }

    public boolean handleRevive(Player player) {
        return false;
    }

    private class ListenerListImpl extends ListenerList<Event>
    {
        public void onStart()
        {
            for(Listener<Event> listener : getListeners())
                if(OnStartStopListener.class.isInstance(listener))
                    ((OnStartStopListener) listener).onStart(Event.this);
        }

        public void onStop()
        {
            for(Listener<Event> listener : getListeners())
                if(OnStartStopListener.class.isInstance(listener))
                    ((OnStartStopListener) listener).onStop(Event.this);
        }
    }

    protected void shutdownServer() {

    }
}
