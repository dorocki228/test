package events;

import com.google.common.collect.Iterators;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.bbs.EventRegistrationCommunityBoardEntry;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.*;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author VISTALL
 * @date 15:10/03.04.2012
 */
public abstract class CustomInstantTeamEvent extends SingleMatchEvent implements Iterable<EventPlayerObject>
{
    private class PlayerListeners implements OnPlayerExitListener, OnTeleportListener
    {
        @Override
        public void onPlayerExit(Player player)
        {
            player.setReflection(ReflectionManager.MAIN);

            exitPlayer(player, true);
        }

        @Override
        public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
        {
            if(_state != State.NONE && reflection != _reflection)
                exitPlayer(player, false);
        }

        private void exitPlayer(Player player, boolean exit)
        {
            for(TeamType team : TeamType.VALUES)
            {
                List<EventPlayerObject> objects = getObjects(team);

                for(EventPlayerObject d : objects)
                    if(Objects.equals(d.getPlayer(), player))
                    {
                        if(isInProgress())
                            onTeleportOutsideOrExit(objects, d, exit);
                        else
                            objects.remove(d);
                        break;
                    }
            }

            player.removeListener(_playerListeners);
            player.removeEvent(CustomInstantTeamEvent.this);
        }
    }

    private class EventReflection extends Reflection
    {
        EventReflection(int val)
        {
            super(val);

            init(InstantZoneHolder.getInstance().getInstantZone(getInstantId()));
        }

        @Override
        public void startCollapseTimer(long timeInMillis)
        {
        }
    }

    public class EventReflectionImpl extends Reflection {
        EventReflectionImpl(int val) {
            super(val);
        }
    }

    public class ItemHolder
    {
        private final int id;
        private final long minCount;
        private final long maxCount;
        private final int chance;

        public ItemHolder(int id, long minCount, long maxCount, int chance)
        {
            this.id = id;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.chance = chance;
        }

        public ItemHolder(String str)
        {
            String[] split = str.split(",");
            id = Integer.parseInt(split[0]);
            String countString = split[1];
            if(countString.contains("-"))
            {
                var parts = countString.split("-");
                minCount = Long.parseLong(parts[0]);
                maxCount = Long.parseLong(parts[1]);
            }
            else
            {
                long count = Long.parseLong(countString);
                minCount = count;
                maxCount = count;
            }
            chance = split.length == 3 ? Integer.parseInt(split[2]) : 100;
        }

        public int getId()
        {
            return id;
        }

        public long getCount()
        {
            if(!Rnd.chance(chance))
                return 0;

            if(minCount == maxCount)
                return maxCount;

            return Rnd.get(minCount, maxCount);
        }
    }

    public enum State
    {
        NONE,
        TELEPORT_PLAYERS,
        STARTED
    }

    public static final ItemHolder[] EMPTY_ITEM_ID_COUNT_HOLDER = new ItemHolder[0];

    public static final String REGISTRATION = "registration";

    // times
    private Instant _startTime;
    private final SchedulingPattern _pattern;
    // rewards
    protected final int _minLevel;
    protected final int _maxLevel;
    protected final ItemHolder[] winnerTeamRewards;
    protected final ItemHolder[] loserTeamRewards;
    protected final ItemHolder[] topKillerRewards;
    protected final ItemHolder[] tieRewards;
    private final List<SkillEntry> magBuffs;
    private final List<SkillEntry> fighterBuffs;
    protected final int minKillCountForLoserTeamReward;
    protected final boolean checkHwid;
    protected final boolean resetSkills;
    protected final boolean disableHeroAndClanSkills;
    protected final boolean hideNick;

    private boolean _registrationOver = true;

    protected State _state = State.NONE;
    protected TeamType _winner = TeamType.NONE;
    protected Reflection _reflection = new EventReflection(-getId());

    private final PlayerListener _playerListeners = new PlayerListeners();

    protected CustomInstantTeamEvent(MultiValueSet<String> set)
    {
        super(set);
        _pattern = new SchedulingPattern(set.getString("pattern"));
        _minLevel = set.getInteger("min_level");
        _maxLevel = set.getInteger("max_level");

        String rewards = set.getString("winner_team_rewards");
        winnerTeamRewards = parseRewards(rewards);
        rewards = set.getString("loser_team_rewards");
        loserTeamRewards = parseRewards(rewards);
        rewards = set.getString("top_killer_rewards");
        topKillerRewards = parseRewards(rewards);
        rewards = set.getString("tie_rewards");
        tieRewards = parseRewards(rewards);

        String buffs = set.getString("mag_buffs");
        magBuffs = parseBuffs(buffs);
        buffs = set.getString("fighter_buffs");
        fighterBuffs = parseBuffs(buffs);

        minKillCountForLoserTeamReward = set.getInteger("min_kill_count_for_loser_team_reward", 0);
        checkHwid = set.getBool("check_hwid", true);
        resetSkills = set.getBool("reset_skills", true);
        disableHeroAndClanSkills = set.getBool("disable_hero_and_clan_skills", true);
        hideNick = set.getBool("hide_nick", false);
    }

    private List<SkillEntry> parseBuffs(String buffs)
    {
        return buffs.isEmpty()
                        ? Collections.emptyList()
                        : Arrays.stream(buffs.split(";"))
                                .map(str ->
                                {
                                    String[] split = str.split(",");
                                    int skillId = Integer.parseInt(split[0]);
                                    int skillLevel = Integer.parseInt(split[1]);
                                    return SkillHolder.getInstance().getSkillEntry(skillId, skillLevel);
                                }).collect(Collectors.toUnmodifiableList());
    }

    private ItemHolder[] parseRewards(String rewards)
    {
        return rewards.isEmpty()
                           ? EMPTY_ITEM_ID_COUNT_HOLDER
                           : Arrays.stream(rewards.split(";"))
                                   .map(ItemHolder::new)
                                   .toArray(ItemHolder[]::new);
    }

    //region Abstracts
    protected abstract int getInstantId();

    protected abstract Location getTeleportLoc(TeamType team);

    protected abstract void checkForWinner();

    protected abstract boolean canWalkInWaitTime();

    protected abstract void onRevive(Player player);

    protected abstract void onTeleportOutsideOrExit(List<EventPlayerObject> objects, EventPlayerObject eventPlayerObject, boolean exit);
    //endregion

    //region Start&Stop and player actions
    @Override
    public void teleportPlayers(String name)
    {
        _registrationOver = true;

        _state = State.TELEPORT_PLAYERS;

        for(TeamType team : TeamType.VALUES)
        {
            List<EventPlayerObject> list = getObjects(team);
            for(EventPlayerObject object : list)
            {
                Player player = object.getPlayer();
                if(!checkPlayer(player, false))
                {
                    player.sendPacket(new HtmlMessage(0).setFile("events/custom_event_cancel.htm"));
                    list.remove(object);
                }
            }
        }

        if(checkHwid)
        {
            List<EventPlayerObject> blue = getObjects(TeamType.BLUE);
            List<EventPlayerObject> red = getObjects(TeamType.RED);
            List<HwidHolder> hwidHolders = Stream.concat(blue.stream(), red.stream())
                    .map(EventPlayerObject::getPlayer)
                    .map(Player::getHwidHolder)
                    .collect(Collectors.toList());

            iterator().forEachRemaining(eventPlayerObject ->
            {
                var player = eventPlayerObject.getPlayer();
                var hwid = player.getHwidHolder();
                if(hwidHolders.stream().filter(temp -> Objects.equals(hwid, temp)).count() > 1)
                {
                    player.sendPacket(new HtmlMessage(0).setFile("events/custom_event_cancel.htm"));
                    blue.remove(eventPlayerObject);
                    red.remove(eventPlayerObject);
                }
            });
        }

        if(getObjects(TeamType.RED).isEmpty() || getObjects(TeamType.BLUE).isEmpty())
        {
            reCalcNextTime(false);

            announceToPlayersWithValidLevel(getClass().getSimpleName() + ".Cancelled");

            return;
        }

        setRegistrationOver(true); // посылаем мессагу

        for(TeamType team : TeamType.VALUES)
        {
            List<EventPlayerObject> objects = getObjects(team);

            for(EventPlayerObject object : objects)
            {
                Player player = object.getPlayer();

                if(!canWalkInWaitTime())
                    player.startFrozen();

                object.store();

                player.setStablePoint(object.getReturnLoc());
                player.teleToLocation(getTeleportLoc(team), _reflection);

                player.addEvent(this);

                onRevive(player);
            }
        }
    }

    @Override
    public void startEvent()
    {
        if(_state != State.TELEPORT_PLAYERS)
            return;

        _state = State.STARTED;

        _winner = TeamType.NONE;

        for(EventPlayerObject object : this)
        {
            Player player = object.getPlayer();
            if(!checkPlayer(player, true))
            {
                removeObject(object.getTeam(), object);

                player.removeEvent(this);

                // если игрок ищо ТПшится - его невозможно ищо раз сТПшить , переносим на арену, ток без отражения
                if(player.isTeleporting())
                {
                    //player.setXYZ(object.getLoc().x, object.getLoc().y, object.getLoc().z);  // возможен крит
                    player.setReflection(ReflectionManager.MAIN);
                    _log.debug("TvT: player teleporting error: {}", player);
                }
                else
                    object.teleportBack();
            }
        }

        if(getObjects(TeamType.RED).isEmpty() || getObjects(TeamType.BLUE).isEmpty())
        {
            reCalcNextTime(false);

            announceToPlayersWithValidLevel(getClass().getSimpleName() + ".Cancelled");

            return;
        }

        updatePlayers(true, false);

        sendPackets(PlaySoundPacket.B04_S01, SystemMsg.LET_THE_DUEL_BEGIN);

        super.startEvent();
    }

    @Override
    public void stopEvent(boolean force)
    {
        if(_state != State.STARTED)
            return;

        clearActions();

        _state = State.NONE;

        updatePlayers(false, false);

        switch(_winner)
        {
            case NONE:
                sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);

                List<EventPlayerObject> objects = getObjects(TeamType.BLUE);
                objects.
                        forEach(d -> Arrays.stream(tieRewards)
                                .filter(reward -> d.getPlayer() != null)
                                .forEach(reward -> ItemFunctions.addItem(d.getPlayer(), reward.getId(), reward.getCount())));

                objects = getObjects(TeamType.RED);
                objects.
                        forEach(d -> Arrays.stream(tieRewards)
                                .filter(reward -> d.getPlayer() != null)
                                .forEach(reward -> ItemFunctions.addItem(d.getPlayer(), reward.getId(), reward.getCount())));

                break;
            case RED:
            case BLUE:
                sendPacket(new SystemMessagePacket(_winner == TeamType.RED ? SystemMsg.THE_RED_TEAM_IS_VICTORIOUS : SystemMsg.THE_BLUE_TEAM_IS_VICTORIOUS));

                List<EventPlayerObject> winners = getObjects(_winner);
                List<EventPlayerObject> losers = getObjects(_winner.revert());
                reward(winners, losers);
                break;
        }

        updatePlayers(false, true);

        if(!force)
            reCalcNextTime(false);

        super.stopEvent(force);
    }

    @Override
    public Optional<String> getOnScreenMessage(Player player)
    {
        if(isRegistrationOver())
            return Optional.empty();

        return Optional.of('[' + getName() + "] " + "Registration");
    }

    protected void reward(List<EventPlayerObject> winners, List<EventPlayerObject> losers)
    {
        winners.
                forEach(d -> Arrays.stream(winnerTeamRewards)
                        .filter(reward -> d.getPlayer() != null)
                        .forEach(reward -> ItemFunctions.addItem(d.getPlayer(), reward.getId(), reward.getCount())));

        losers
                .forEach(d -> Arrays.stream(loserTeamRewards)
                        .filter(reward -> d.getPlayer() != null)
                        .forEach(reward -> ItemFunctions.addItem(d.getPlayer(), reward.getId(), reward.getCount())));
    }

    protected void updatePlayers(boolean start, boolean teleport)
    {
        for(EventPlayerObject $snapshot : this)
        {
            if($snapshot.getPlayer() == null)
                continue;

            if(teleport)
                $snapshot.teleportBack();
            else
            {
                Player $player = $snapshot.getPlayer();
                if(start)
                {
                    $player.stopFrozen();
                    $player.setTeam($snapshot.getTeam());

                    $player.setCurrentMp($player.getMaxMp());
                    $player.setCurrentCp($player.getMaxCp());
                    $player.setCurrentHp($player.getMaxHp(), true);

                    resetSkills($player);
                    disableHeroAndClanSkills($player);
                    buff($player);
                }
                else
                {
                    if($player.isMoveBlocked())
                        $player.stopMoveBlock();

                    $player.startFrozen();
                    $player.removeEvent(this);

                    GameObject target = $player.getTarget();
                    if(target != null)
                        $player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target);

                    $snapshot.restore();
                    $player.setTeam(TeamType.NONE);

                    if(disableHeroAndClanSkills)
                    {
                        if($player.getClan() != null)
                            $player.getClan().enableSkills($player);
                        $player.activateHeroSkills(true);
                        $player.sendSkillList();
                    }
                }

                actionUpdate(start, $player);
            }
        }
    }

    public void resetSkills(Player player)
    {
        if(!resetSkills)
            return;

        player.getSkillReuses().stream()
                .filter(Objects::nonNull)
                .forEach(sts ->
                {
                    Skill skill = SkillHolder.getInstance().getSkill(sts.getId(), sts.getLevel());
                    if(skill == null)
                        return;
                    if(sts.getReuseBasic() > 900015L)
                        return;
                    player.enableSkill(skill);
                });
    }

    public void disableHeroAndClanSkills(Player player)
    {
        if(!disableHeroAndClanSkills)
            return;

        if(player.getClan() != null)
            player.getClan().disableSkills(player);

        player.activateHeroSkills(false);
        player.sendSkillList();
    }

    public void buff(Player player)
    {
        if(player.getClassId().isMage())
            magBuffs.forEach(s -> s.getEffects(player, player));
        else
            fighterBuffs.forEach(s -> s.getEffects(player, player));
    }

    protected void actionUpdate(boolean start, Player player)
    {
    }
    //endregion

    //region Broadcast
    @Override
    public void sendPacket(IBroadcastPacket packet)
    {
        sendPackets(packet);
    }

    @Override
    public void sendPackets(IBroadcastPacket... packet)
    {
        for(EventPlayerObject d : this)
            if(d.getPlayer() != null)
                d.getPlayer().sendPacket(packet);
    }

    public void sendPacket(IBroadcastPacket packet, TeamType... ar)
    {
        for(TeamType a : ar)
        {
            List<EventPlayerObject> objs = getObjects(a);

            for(EventPlayerObject d : objs)
                if(d.getPlayer() != null)
                    d.getPlayer().sendPacket(packet);
        }
    }
    //endregion

    //region Registration
    private boolean checkPlayer(Player player, boolean second)
    {
        if(player.isInOfflineMode())
        {
            return false;
        }

        if(player.getLevel() > _maxLevel || player.getLevel() < _minLevel)
        {
            return false;
        }

        if(player.isMounted() || player.isDead() || player.isInObserverMode())
        {
            return false;
        }

        SingleMatchEvent evt = player.getEvent(SingleMatchEvent.class);
        if(evt != null && !Objects.equals(evt, this))
        {
            return false;
        }

        if(player.getTeam() != TeamType.NONE)
        {
            return false;
        }

        if(player.isTeleporting())
        {
            return false;
        }

        if(!second)
        {
            if(!player.getReflection().isMain())
                return false;

            if(player.isInZone(ZoneType.epic))
                return false;
        }

        return true;
    }

    public boolean isPlayerRegistered(Player player)
    {
        for(EventPlayerObject d : this)
        {
            if(Objects.equals(d.getPlayer(), player))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public synchronized boolean registerPlayer(Player player)
    {
        if(_registrationOver)
            return false;

        for(EventPlayerObject d : this)
            if(Objects.equals(d.getPlayer(), player))
                return false;

        if(!checkPlayer(player, false))
            return false;

        List<EventPlayerObject> blue = getObjects(TeamType.BLUE);
        List<EventPlayerObject> red = getObjects(TeamType.RED);
        TeamType team;
        if(blue.size() == red.size())
            team = Rnd.get(TeamType.VALUES);
        else if(blue.size() > red.size())
            team = TeamType.RED;
        else
            team = TeamType.BLUE;

        addObject(team, new EventPlayerObject(player, team, false, false));

        return true;
    }

    @Override
    public boolean unregisterPlayer(Player player)
    {
        if(_registrationOver)
        {
            return false;
        }
        if(player.getTeam() != TeamType.NONE)
        {
            return false;
        }

        List<EventPlayerObject> list = getObjects(TeamType.RED);
        for(EventPlayerObject object : list)
        {
            if(Objects.equals(player, object.getPlayer()))
            {
                list.remove(object);
                return true;
            }
        }
        list = getObjects(TeamType.BLUE);
        for(EventPlayerObject object : list)
        {
            if(Objects.equals(player, object.getPlayer()))
            {
                list.remove(object);
                return true;
            }
        }

        return false;
    }

    protected void announceToPlayersWithValidLevel(String str)
    {
        for(Player player : GameObjectsStorage.getPlayers())
            if(player.isGM() || player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel)
                player.sendPacket(new SayPacket2(0, ChatType.ANNOUNCEMENT, "", String.format(StringsHolder.getInstance().getString(str, player), _minLevel, _maxLevel)));
    }

    @Override
    public boolean isInProgress()
    {
        return _state == State.STARTED;
    }

    @Override
    public void action(String name, boolean start)
    {
        if(name.equalsIgnoreCase(REGISTRATION))
            setRegistrationOver(!start);
        else
            super.action(name, start);
    }

    @Override
    public boolean isRegistrationOver()
    {
        return _registrationOver;
    }

    public void setRegistrationOver(boolean registrationOver)
    {
        _registrationOver = registrationOver;

        if (_registrationOver) {
            announceToPlayersWithValidLevel(getClass().getSimpleName() + ".RegistrationIsClose");
            removeCommunityBoardEntry("registration");
        } else {
            announceToPlayersWithValidLevel(getClass().getSimpleName() + ".RegistrationIsOpen");
            addCommunityBoardEntry("registration", new EventRegistrationCommunityBoardEntry(this));
        }
    }
    //endregion

    //region Implementation & Override
    @Override
    public void initEvent()
    {
        super.initEvent();

        InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(getInstantId());

        _reflection.init(instantZone);
        _reflection.getZones().forEach(zone -> zone.addListener(new OnZoneEnterLeaveListener()
        {
            @Override
            public void onZoneEnter(Zone zone, Creature creature)
            {

            }

            @Override
            public void onZoneLeave(Zone zone, Creature creature)
            {
                if(!creature.isPlayable())
                    return;

                if(_state != State.NONE && !creature.isMoveBlocked() && !creature.isTeleporting())
                {
                    creature.startMoveBlock();
                    creature.stopMove();
                    ThreadPoolManager.getInstance().schedule(() ->
                    {
                        TeamType team = creature.getTeam();
                        if(team == TeamType.NONE)
                        {
                            var restartPoint = TeleportUtils.getRestartPoint(creature.getPlayer(), RestartType.TO_VILLAGE);
                            var loc = restartPoint.getLoc();
                            creature.teleToLocation(loc);
                            if(creature.isMoveBlocked())
                                creature.stopMoveBlock();
                            return;
                        }
                        creature.teleToLocation(getTeleportLoc(team));
                        if(creature.isMoveBlocked())
                            creature.stopMoveBlock();
                        creature.sendMessage("You was teleported back to event.");
                    }, 5, TimeUnit.SECONDS);
                }
            }
        }));
    }

    @Override
    public void reCalcNextTime(boolean onInit)
    {
        clearActions();

        removeObjects(TeamType.RED);
        removeObjects(TeamType.BLUE);

        _reflection.clearVisitors();

        _state = State.NONE;

        _startTime = _pattern.next(Instant.now());

        registerActions();
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
        if(_state != State.STARTED || target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
            return false;

        return true;
    }

    @Override
    public SystemMsg canUseItem(Player player, ItemInstance item)
    {
        if(item.getItemType() == EtcItemTemplate.EtcItemType.POTION)
            return SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM;
        return null;
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
        if(!isInProgress() || Objects.equals(player, observer) || !hideNick)
            return null;

        return new CustomMessage("CustomInstantTeamEvent.PlayerName").toString(observer);
    }

    @Override
    public String getVisibleTitle(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer) || !hideNick)
            return null;

        return "";
    }

    @Override
    public Integer getVisibleNameColor(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer) || !hideNick)
            return null;

        return 16777215;
    }

    @Override
    public Integer getVisibleTitleColor(Player player, Player observer)
    {
        if(!isInProgress() || Objects.equals(player, observer) || !hideNick)
            return null;

        return 16777079;
    }

    @Override
    public boolean isPledgeVisible(Player player, Player observer)
    {
        return !isInProgress() || Objects.equals(player, observer) || !hideNick;
    }

    @Override
    public Iterator<EventPlayerObject> iterator()
    {
        List<EventPlayerObject> blue = getObjects(TeamType.BLUE);
        List<EventPlayerObject> red = getObjects(TeamType.RED);
        return Iterators.concat(blue.iterator(), red.iterator());
    }

    @Override
    protected long startTimeMillis()
    {
        return _startTime.toEpochMilli();
    }

    @Override
    public EventType getType()
    {
        return EventType.CUSTOM_PVP_EVENT;
    }

    @Override
    public void announce(int val, SystemMsg msgId)
    {
        switch(val)
        {
            case -10:
            case -5:
            case -4:
            case -3:
            case -2:
            case -1:
                sendPacket(new SystemMessagePacket(SystemMsg.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(-val));
                break;
        }
    }

    @Override
    public void onAddEvent(GameObject o)
    {
        if(o.isPlayer())
            o.getPlayer().addListener(_playerListeners);
    }

    @Override
    public void onRemoveEvent(GameObject o)
    {
        if(o.isPlayer())
            o.getPlayer().removeListener(_playerListeners);
    }

    @Override
    public Reflection getReflection()
    {
        return _reflection;
    }
    //endregion
}
