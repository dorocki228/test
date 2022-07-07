package l2s.gameserver.model.entity.events.impl.brevent;

import com.google.common.base.Enums;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Announcements;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.bbs.EventRegistrationCommunityBoardEntry;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;
import l2s.gameserver.model.entity.events.impl.brevent.handlers.*;
import l2s.gameserver.model.entity.events.impl.brevent.listeners.BREventDeathListener;
import l2s.gameserver.model.entity.events.impl.brevent.listeners.BREventPlayerLeaveListener;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRCircleZone;
import l2s.gameserver.model.entity.events.impl.brevent.model.BREventCore;
import l2s.gameserver.model.entity.events.impl.brevent.model.IBREventHandler;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 20:02
 * <p/>
 */
public class BREvent extends SingleMatchEvent {
    private static final long VAR_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(60);

    public static final String REGISTRATION = "registration";
    public static final String TELEPORTATION = "teleportation";
    public static final String PREPARATION = "preparation";
    public static final String ENGAGE = "engage";

    private static final Logger LOGGER = LogManager.getLogger(BREvent.class);

    private final Map<EBREventState, IBREventHandler> handlers = new HashMap<>();

    private Instant startTime;
    private final SchedulingPattern pattern;

    private BREventDeathListener killListener;
    private BREventPlayerLeaveListener playerLeaveListener;

    private final List<ClassId> prohibitedClasses;

    private EBREventState state = EBREventState.SCHEDULED;
    private boolean eventStarted;

    private List<Player> registeredList = new CopyOnWriteArrayList<>();

    private BREventCore eventCore;

    public BREvent(MultiValueSet<String> set)
    {
        super(set);
        pattern = new SchedulingPattern(set.getString("pattern"));
        prohibitedClasses = Arrays.stream(set.getIntegerArray("prohibited_classes", ArrayUtils.EMPTY_INT_ARRAY, ","))
                .mapToObj(ClassId::valueOf)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableList());
    }

    public Map<EBREventState, IBREventHandler> getHandlers()
    {
        return handlers;
    }

    public EBREventState getState()
    {
        return state;
    }

    public BRCircleZone getNextSafeZone()
    {
        return eventCore.getNextSafeZone();
    }

    public int getSecond()
    {
        return eventCore.getSecond();
    }

    @Override
    public void reCalcNextTime(boolean onInit)
    {
        clearActions();

        startTime = pattern.next(Instant.now());

        registerActions();

        if(!onInit)
            printInfo();
    }

    @Override
    public EventType getType()
    {
        return EventType.CUSTOM_PVP_EVENT;
    }

    @Override
    protected long startTimeMillis()
    {
        return startTime.toEpochMilli();
    }

    @Override
    public void initEvent()
    {
        registerHandler(new BRRegistration());
        registerHandler(new BRPreparation());
        registerHandler(new BRTeleportation());
        registerHandler(new BRBattle());
        registerHandler(new BREnd());
        LOGGER.info("Battle Royal Event: Controller loaded");
        BREventConfig.getInstance();
        LOGGER.info("Battle Royal Event: Config loaded");

        killListener = new BREventDeathListener(this);
        playerLeaveListener = new BREventPlayerLeaveListener(this);

        super.initEvent();
    }

    @Override
    public void startEvent()
    {
        eventStarted = true;
        super.startEvent();
    }

    @Override
    public void stopEvent(boolean force)
    {
        clearActions();

        if(eventCore != null)
            eventCore.stopEvent(this);
        registeredList.clear();

        removeBanishItems();

        state = EBREventState.SCHEDULED;

        reCalcNextTime(false);

        super.stopEvent(force);
    }

    @Override
    public boolean overrideOnScreenMessage(Player player)
    {
        return player.containsEvent(this);
    }

    @Override
    public Optional<String> getOnScreenMessage(Player player)
    {
        if(state == EBREventState.REGISTRATION)
        {
            return Optional.of('[' + getName() + "] " + "Registration");
        }

        if(player.containsEvent(this) && (state == EBREventState.PREPARATION || state == EBREventState.ENGAGE))
        {
            var str = getName() + ':';
            var playersCount = getPlayersCount();
            var playersLeft = getPlayersLeft();
            str += "\nAlive: " + playersLeft + "/" + playersCount;
            if(getSecond() < 0)
            {
                str += "\nTimer: 0:0";
            }
            else
            {
                var duration = Duration.ofSeconds(getSecond());
                str += "\nTimer: " + duration.toMinutesPart() + ":" + duration.toSecondsPart();
            }

            var playerObject = getEventPlayerObject(player);
            str += "\nPoints: " + playerObject
                    .map(temp -> temp.getPoints("BATTLE_ROYAL_POINTS")).orElse(0);
            str += "\nKills: " + playerObject
                    .map(temp -> temp.getPoints("BATTLE_ROYAL_KILLS")).orElse(0);

            if(getNextSafeZone() != null)
            {
                var circle = getNextSafeZone().getCircle();
                var distance = player.getLoc().distance(circle.getCenter()) - circle.getRadius();
                distance = Math.max(distance / 14, 0);
                str += "\n" + (int) Math.round(distance) + " m";
            }
            return Optional.ofNullable(str);
        }

        return Optional.empty();
    }

    @Override
    public long onScreenMessageUpdateTime(TimeUnit timeUnit)
    {
        return timeUnit.convert(1, TimeUnit.SECONDS);
    }

    @Override
    public void action(String name, boolean start)
    {
        if(!eventStarted)
        {
            stopEvent(true);
            return;
        }

        var newState = Enums.getIfPresent(EBREventState.class, name.toUpperCase()).orNull();
        if(newState != null)
            state = newState;

        switch(name)
        {
            case REGISTRATION:
                Announcements.announceToAll("Registration have been opened for " + 100
                        + " seconds to Battle Royal event: you can register by using '.joinbr' voice command.");

                eventCore = new BREventCore();

                addCommunityBoardEntry("registration", new EventRegistrationCommunityBoardEntry(this));

                break;
            case TELEPORTATION:
                removeCommunityBoardEntry("registration");

                for(Player player : registeredList)
                {
                    var playerObject = new EventPlayerObject(player, TeamType.NONE, false, false);
                    eventCore.addPlayer(playerObject);
                    player.addEvent(this);

                    getEventHandler(EBREventState.TELEPORTATION).invoke(this, playerObject);

                    if(!Objects.equals(player.getReflection(), getReflection()))
                    {
                        eventCore.removePlayer(playerObject);
                        player.removeEvent(this);
                        playerObject.teleportBack();
                        playerObject.clear();
                        registeredList.remove(player);
                        continue;
                    }

                    playerObject.store(true);

                    player.block();
                    player.startFrozen();
                }

                break;
            case PREPARATION:
                eventCore.getPlayersStream().forEach(playerObject ->
                {
                    if(playerObject.getPlayer() == null)
                    {
                        eventCore.removePlayer(playerObject);
                        return;
                    }

                    getEventHandler(EBREventState.PREPARATION).invoke(this, playerObject);
                });

                break;
            case ENGAGE:
                eventCore.startEvent(this);

                eventCore.getPlayersStream().forEach(playerObject ->
                {
                    if(playerObject.getPlayer() == null)
                    {
                        eventCore.removePlayer(playerObject);
                        return;
                    }

                    getEventHandler(EBREventState.ENGAGE).invoke(this, playerObject);
                });

                break;
            default:
                super.action(name, start);
                break;
        }
    }

    @Override
    public void onAddEvent(GameObject o)
    {
        if(o.isPlayer())
        {
            o.getPlayer().addListener(killListener);
            o.getPlayer().addListener(playerLeaveListener);
        }
    }

    @Override
    public void onRemoveEvent(GameObject o)
    {
        if(o.isPlayer())
        {
            Player player = o.getPlayer();

            player.removeListener(killListener);
            player.removeListener(playerLeaveListener);

            if(player.isBlocked())
                player.unblock();
            if(player.isFrozen())
                player.stopFrozen();
        }
    }

    @Override
    public boolean isInProgress()
    {
        return state != EBREventState.SCHEDULED && state != EBREventState.REGISTRATION;
    }

    @Override
    public boolean isRegistrationOver()
    {
        return state != EBREventState.REGISTRATION;
    }

    public boolean isPlayerRegistered(Player player)
    {
        return registeredList.contains(player);
    }

    @Override
    public synchronized boolean registerPlayer(Player player)
    {
        if(!checkPlayer(player))
            return false;

        registeredList.add(player);

        return true;
    }

    @Override
    public boolean unregisterPlayer(Player player)
    {
        if(isRegistrationOver())
        {
            return false;
        }
        if(!registeredList.contains(player))
        {
            return false;
        }

        if(player.containsEvent(this))
        {
            player.sendMessage("You are already participating and cannot cancel registration.");
            return false;
        }

        registeredList.remove(player);

        return true;
    }

    @Override
    public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
    {
        r.clear();
    }

    @Override
    public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
    {
        if(!canAttack(target, attacker, null, force, false))
            return SystemMsg.INVALID_TARGET;

        return null;
    }

    @Override
    public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force, boolean nextAttackCheck)
    {
        return true;
    }

    @Override
    public SystemMsg canUseItem(Player player, ItemInstance item)
    {
        return null;
    }

    @Override
    public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
    {
        return false;
    }

    @Override
    public boolean canUseTeleport(Player player)
    {
        return false;
    }

    @Override
    public boolean canUseCommunityFunctions(Player player)
    {
        return false;
    }

    @Override
    public boolean canJoinParty(Player inviter, Player invited)
    {
        return false;
    }

    @Override
    public String getVisibleName(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer))
            return null;

        return "Player";
    }

    @Override
    public String getVisibleTitle(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer))
            return null;

        return "";
    }

    @Override
    public Integer getVisibleNameColor(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer))
            return null;

        return 16777215;
    }

    @Override
    public Integer getVisibleTitleColor(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer))
            return null;

        return 16777079;
    }

    @Override
    public boolean isPledgeVisible(Player player, Player observer)
    {
        return !isInProgress() || Objects.equals(player, observer);
    }

    @Override
    public Reflection getReflection()
    {
        return eventCore.getReflection();
    }

    public void nextCircle() {
        eventCore.nextCircle(this);
    }

    public void teleportPlayer(EventPlayerObject player) {
        eventCore.teleportPlayerToEvent(player);
    }

    public void showSafeZoneCircle(EventPlayerObject player) {
        eventCore.showSafeZoneCircle(player);
    }

    public void hideSafeZoneCircle(EventPlayerObject player) {
        eventCore.hideSafeZoneCircle(player);
    }

    public void showNextSafeZoneCircle(EventPlayerObject player) {
        eventCore.showNextSafeZoneCircle(player);
    }

    public void hideNextSafeZoneCircle(EventPlayerObject player) {
        eventCore.hideNextSafeZoneCircle(player);
    }

    public int[] getCircleIds()
    {
        return eventCore.getCircleIds();
    }

    /**
     * Проверяет игрока
     * @param player
     * @return
     */
    public boolean checkPlayer(Player player) {
        if(player == null) {
            return false;
        }

        if(player.isInOfflineMode()) {
            return false;
        }

        if(state != EBREventState.REGISTRATION) {
            player.sendMessage("It is not a time for registration");
            return false;
        }

        if(registeredList.size() >= BREventConfig.MAX_PLAYERS) {
            player.sendMessage(BREventConfig.MAX_PLAYERS + " players is registered already and you cannot participate");
            return false;
        }

        if(registeredList.contains(player)) {
            player.sendMessage("You have registered already");
            return false;
        }

        if(player.getClassLevel() != ClassLevel.THIRD) {
            player.sendMessage("You need to have 3rd profession.");
            return false;
        }

        if(prohibitedClasses.contains(player.getClassId())) {
            player.sendMessage("Your class can not participate in the event.");
            return false;
        }

        if(player.isInOlympiadMode()) {
            player.sendMessage("You are in Grand Olympiad mode");
            return false;
        }

        if(player.isInSiegeZone()) {
            player.sendMessage("You are on siege field");
            return false;
        }

        if(player.getReflectionId() != 0) {
            player.sendMessage("You are in instance zone");
            return false;
        }

        if(player.containsEvent(SingleMatchEvent.class))
        {
            player.sendMessage("You cannot register while being in another event.");
            return false;
        }

        return true;
    }

    public int getPlayersCount() {
        return eventCore.getPlayersCount();
    }

    public int getPlayersLeft() {
        return eventCore.getPlayersLeft();
    }

    public Stream<EventPlayerObject> getPlayersStream()
    {
        return eventCore.getPlayersStream();
    }

    public Optional<EventPlayerObject> getEventPlayerObject(Player player)
    {
        return eventCore.getEventPlayerObject(player);
    }

    public void removePlayer(EventPlayerObject player)
    {
        eventCore.removePlayer(player);
    }

    /**
     * Убирает игрока из списка зарегистрировавшихся
     * @param player
     */
    public void removeRegistered(Player player) {
        if(player == null) {
            return;
        }
        registeredList.remove(player);
    }

    private void registerHandler(IBREventHandler effect_handler) {
        if (getHandlers().containsKey(effect_handler.getState())) {
            LOGGER.warn("BREvent: trying to duplicate bypass registered! First handler: " + getHandlers().get(effect_handler.getState()).getClass().getSimpleName() + " second: " + effect_handler.getClass().getSimpleName());
            return;
        }
        getHandlers().put(effect_handler.getState(), effect_handler);
    }

    public IBREventHandler getEventHandler(EBREventState state) {
        if (getHandlers().isEmpty()) {
            return null;
        }
        for (Map.Entry<EBREventState, IBREventHandler> entry : getHandlers().entrySet()) {
            if (state == entry.getKey()) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void announceToParticipator(EventPlayerObject player, String message) {
        player.ifPlayerExist(temp -> announceToParticipator(temp, message));
    }

    public void announceToParticipator(Player player, String message) {
        player.sendPacket(new SayPacket2(0, ChatType.CRITICAL_ANNOUNCE, "", message));
    }

    public List<ItemInstance> addItem(Player player, int itemId, long count, int enchantLevel) {
        var items = ItemFunctions.addItem(player, itemId, count, enchantLevel, true);
        items.forEach(item ->
        {
            addBanishItem(item);
            item.setCustomFlags(ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_TRADE
                    | ItemInstance.FLAG_NO_STORE | ItemInstance.FLAG_NO_CRYSTALLIZE | ItemInstance.FLAG_NO_ENCHANT
                    | ItemInstance.FLAG_NO_DESTROY | ItemInstance.FLAG_NO_FREIGHT | ItemInstance.FLAG_LIFE_TIME);
            item.setLifeTime(Math.toIntExact(getExpirationTime().getEpochSecond()));
        });
        return items;
    }

    public Instant getExpirationTime()
    {
        return startTime.plusMillis(VAR_EXPIRATION_TIME);
    }
}
