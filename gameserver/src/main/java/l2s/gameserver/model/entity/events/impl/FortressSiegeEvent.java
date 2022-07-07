package l2s.gameserver.model.entity.events.impl;

import gve.util.GveMessageUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.FortressUpgradeHolder;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.OfflinePlayer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.EventTimeTask;
import l2s.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SpawnExFortObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.fortress.UpgradeData;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.service.BroadcastService;
import l2s.gameserver.service.MoraleBoostService;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.time.counter.TimeCounter;
import l2s.gameserver.utils.Location;

public class FortressSiegeEvent extends SiegeEvent<Fortress, SiegeClanObject> {
    private final ZoneEnterLeaveListener _zoneListener = new ZoneEnterLeaveListener();

    private long _lastAlarmTime;
    private int _reward = 0;
    private String eventServerVariable;

    private ScheduledFuture<?> broadcastCrystalStatusTask;

    private final Map<Integer, Location[]> fortressDefendersTeleports = new HashMap<>();
    private final Map<Integer, Location[]> fortressAttackersTeleports = new HashMap<>();

    public FortressSiegeEvent(MultiValueSet<String> set) {
        super(set);

        fortressDefendersTeleports.put(400, new Location[]{
                new Location(78296, 90552, -2880),
                new Location(79960, 89224, -2880),
                new Location(79688, 89896, -2440),
                new Location(80408, 91288, -2880),
                new Location(79688, 92952, -2440),
                new Location(77512, 92872, -2880)});
        fortressAttackersTeleports.put(400, new Location[]{
                new Location(74888, 91688, -3264),
                new Location(74888, 91688, -3264)});

        fortressDefendersTeleports.put(401, new Location[]{
                new Location(63186, 69655, -3024),
                new Location(63128, 67704, -3024),
                new Location(60136, 69496, -3024),
                new Location(62088, 68360, -2576),
                new Location(63384, 69800, -3024),
                new Location(61176, 69624, -3024)});
        fortressAttackersTeleports.put(401, new Location[]{
                new Location(64808, 69528, -3712),
                new Location(57960, 66856, -3536)});

        fortressDefendersTeleports.put(402, new Location[]{
                new Location(47832, 91736, -2976),
                new Location(48552, 91704, -2680),
                new Location(47096, 90039, -2976),
                new Location(46888, 90776, -2976),
                new Location(45224, 92648, -2976),
                new Location(47032, 92760, -2976)});
        fortressAttackersTeleports.put(402, new Location[]{
                new Location(46552, 88152, -3520),
                new Location(46552, 88152, -3520)});
    }

    @Override
    public void initEvent() {
        super.initEvent();

        SpawnExFortObject spawns = getFirstObject("spawns");

        Fraction f = getResidence().getFraction();
        for (NpcInstance npc : spawns.getAllSpawned()) {
            npc.setFraction(f);
            npc.setCurrentHp(npc.getMaxHp(), false, false);
        }

        // Возможно стоит обновлять апгрейд хп при смене владельца для сброса уровня старого владельца
        List<DoorObject> doorObjects = getObjects("doors");
        int level = getResidence().getUpgrade(UpgradeType.GATE);
        UpgradeData data = FortressUpgradeHolder.getInstance().get(getResidence().getId()).getData(UpgradeType.GATE, level);
        for (DoorObject doorObject : doorObjects) {
            final long max_hp = doorObject.getDoor().getMaxHp();
            doorObject.setUpgradeValue(this, (int) ((max_hp * Double.parseDouble(data.getParam())) - max_hp));
        }

        if (f != Fraction.NONE) {
            spawnGuardian();
        }

        ZoneObject zone = getFirstObject("siege_zone");
        if (zone != null)
            zone.getZone().addListener(_zoneListener);

        eventServerVariable = "FortressSiegeEvent_" + getResidence().getId();
        loadReward();

        ThreadPoolManager.getInstance().scheduleAtFixedDelay(this::increaseReward, Config.GVE_FORTRESS_REWARD_INTERVAL, Config.GVE_FORTRESS_REWARD_INTERVAL);
    }

    public void spawnGuardian() {
        Fraction fraction = getResidence().getFraction();
        int level = getResidence().getUpgrade(UpgradeType.GUARDIAN);
        if (level > 1) {
            final UpgradeData data = FortressUpgradeHolder.getInstance().get(getResidence().getId()).getData(UpgradeType.GUARDIAN, level);
            final StringTokenizer tokenizer = new StringTokenizer(data.getParam().split(":")[fraction.ordinal() - 1], "[,]");
            final int id = Integer.parseInt(tokenizer.nextToken());
            final Location location = new Location(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
            final SpawnSimpleObject spawn = new SpawnSimpleObject(id, location);
            addObject("guardian", spawn);
            spawn.spawnObject(this);
            final NpcInstance guardian = spawn.getNpc();
            guardian.setFraction(fraction);
            guardian.broadcastCharInfo();
            spawn.respawnObject(this);
        }
    }

    @Override
    public void giveOwnerCrp(int count) {
        if (getResidence().getOwner() != null)
            getResidence().getOwner().incReputation(count, false, "FortressSiegeDefence");
    }

    @Override
    public List<Player> getPlayersInZone() {
        return getResidence().getZone().getInsidePlayers();
    }

    @Override
    public void startEvent() {
        if (isInProgress())
            return;

        _oldOwner = getResidence().getOwner();

        if(!canStart()) {
            return;
        }

        // TODO move to xml
        TimeCounter.INSTANCE.start(this, "reward");

        super.startEvent();

        registerActions();

        getPlayersInZone().forEach(player -> TimeCounter.INSTANCE.addPlayer(this, "reward", player));

        broadcastCrystalStatusTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(
                this::broadcastCrystalStatus, 0, 10, TimeUnit.SECONDS);

        manageAlarm(true);
    }

    public boolean canStart() {
        Duration timeToStartSiege = getTimeToStartSiege();
        return timeToStartSiege.isNegative() || timeToStartSiege.isZero();
    }

    @Override
    public void reCalcNextTime(boolean onInit) {

    }

    @Override
    public synchronized void registerActions() {
        if (_tasks == null)
            _tasks = new ArrayList<>(_onTimeActions.size());
        long c = System.currentTimeMillis();
        for (int key : _onTimeActions.keySet().toArray()) {
            long time = c + key * 1000L;
            EventTimeTask wrapper = new EventTimeTask(this, key);

            Future<?> task = ThreadPoolManager.getInstance().schedule(wrapper, time - c);
            if (task != null)
                _tasks.add(task);
        }
    }

    @Override
    public Optional<String> getOnScreenMessage(Player player) {
        if (!isInProgress()) {
            return Optional.empty();
        }

        if (player.getFraction() == getOwnerFraction()) {
            return Optional.empty();
        }

        return Optional.ofNullable(getName());
    }

    @Override
    public void stopEvent(boolean force) {
        stopEvent(null);
    }

    public void stopEvent(Player newOwner) {
        //TODO: Fixme
        List<Player> playersInZone = getPlayersInZone();
        playersInZone.addAll(getResidence().getZone().getInsidePlayers());
        playersInZone = playersInZone.stream().distinct().collect(Collectors.toList());

        clearActions();

        super.stopEvent(true);

        var elapsedTimeMap = TimeCounter.INSTANCE.stop(this, "reward");
        elapsedTimeMap.forEach(playerWithTime ->
                playerWithTime.ifPlayerSpendEnoughTimeOrElse(Config.GVE_FORTRESS_REWARD_TIME_IN_ZONE,
                        player -> {
                            rewardPlayer(newOwner, player);
                            return null;
                        },
                        player -> {
                            player.sendMessage("You did not spend enough time in event to receive a reward.");
                            return null;
                        }));

        _reward = 0;
        updateReward();

        _residence.getLastSiegeDate().setTimeInMillis(System.currentTimeMillis());

        GveMessageUtil.updateProtectMessage(Fraction.NONE);

        if (broadcastCrystalStatusTask != null)
            broadcastCrystalStatusTask.cancel(true);
        broadcastCrystalStatus(null, false);

        if (Config.GVE_FARM_ENABLED) {
            SteadDataHolder.getInstance().getStead(getId()).changeOwner();
        }

        if (newOwner == null) {
            teleportPlayers(FROM_RESIDENCE_TO_TOWN, playersInZone);
            MoraleBoostService.getInstance().fortressSuccessDefense(getResidence(), getOwnerFraction());

            getResidence().setJdbcState(JdbcEntityState.UPDATED);
            getResidence().update();
            return;
        }

        Clan newOwnerClan = newOwner.getClan();
        Fraction fraction = newOwner.getFraction();

        if (fraction != null) {
            getResidence().setFraction(fraction);

            spawnAction("spawns", false);
            spawnAction("spawns", true);

            SpawnExObject spawns = getFirstObject("spawns");

            for (NpcInstance npc : spawns.getAllSpawned()) {
                npc.setFraction(fraction);
                npc.broadcastCharInfo();
            }
        }

        getResidence().changeOwner(newOwnerClan);
        teleportPlayers(FROM_RESIDENCE_TO_TOWN, playersInZone);

        if (newOwnerClan != null && !newOwnerClan.equals(_oldOwner)) {
            int reputation = newOwnerClan.incReputation(1000, false, toString());
            SystemMsg msg = SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE;
            newOwnerClan.broadcastToOnlineMembers(new SystemMessagePacket(msg).addNumber(reputation));
/*            if (_oldOwner != null) {
                reputation = -_oldOwner.incReputation(-1000, false, toString());
                msg = SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOU_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS;
                _oldOwner.broadcastToOnlineMembers(new SystemMessagePacket(msg).addNumber(reputation));
            }*/
        }

        for (Player pc : getPlayersInZone()) {
            if (pc.isDead()) {
                continue;
            }

            getResidence().manageZoneBonuses(pc, true);
            getResidence().manageZoneStatus(pc, true);
        }

        String ownerName = newOwnerClan == null ? fraction == null ? "NONE" : fraction.toString() : newOwnerClan.getName();
        Announcements.announceToAll(ownerName + " successfully captured " + getResidence().getName() + '.');

        if (fraction != Fraction.NONE && getResidence().getOwner() == null)
            ThreadPoolManager.getInstance().schedule(this::makeAuctionWinner, AUCTION_TIME);
    }

    private void rewardPlayer(Player newOwner, OfflinePlayer offlinePlayer) {
        int count;
        Map<Integer, Integer> rewardItems = null;

        if (newOwner != null) {
            count = offlinePlayer.getPlayerObjectId() == newOwner.getObjectId() ? _reward + 150 : _reward;
            rewardItems = offlinePlayer.getFraction() == newOwner.getFraction() ? Config.GVE_FORTRESS_REWARD_WINNER
                : Config.GVE_FORTRESS_REWARD_LOSER;
        } else {
            count = _reward;
        }

        if (rewardItems != null) {
            rewardItems.forEach((itemId, amount) -> offlinePlayer.addItem(itemId, amount, "FortressSiegeEventWin"));
        }

        offlinePlayer.addItem(ItemTemplate.ITEM_ID_ADENA, count, "FortressSiegeEventWin");
        offlinePlayer.sendMessage("You received a reward for participating in the siege.");

        offlinePlayer.ifPlayerOnline(player -> {
            player.getListeners().onFortressCapture();
            return null;
        });
    }

    private void makeAuctionWinner() {
        List<AuctionSiegeClanObject> siegeClanObjects = removeObjects(ATTACKERS);
        // сортуруем с Макс к мин
        siegeClanObjects.sort(SiegeClanObject.SiegeClanComparatorImpl.getInstance());

        AuctionSiegeClanObject winnerSiegeClan = siegeClanObjects.isEmpty() ? null : siegeClanObjects.get(0);

        // если есть победитель(тоисть больше 1 клана)
        if (winnerSiegeClan != null) {
            // розсылаем мессагу, возращаем всем деньги
            SystemMessage msg = new SystemMessage(SystemMsg.THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN).addString(winnerSiegeClan.getClan().getName());
            for (AuctionSiegeClanObject siegeClan : siegeClanObjects) {
                Player player = siegeClan.getClan().getLeader().getPlayer();
                if (player != null)
                    player.sendPacket(msg);
                else
                    PlayerMessageStack.getInstance().mailto(siegeClan.getClan().getLeaderId(), msg);

                if (siegeClan != winnerSiegeClan) {
                    long returnBid = siegeClan.getParam() - (long) (siegeClan.getParam() * 0.1);

                    siegeClan.getClan().getWarehouse().addItem(ItemTemplate.ITEM_ID_ADENA, returnBid);
                }
            }

            SiegeClanDAO.getInstance().delete(getResidence());

            getResidence().setJdbcState(JdbcEntityState.UPDATED);
            getResidence().changeOwner(winnerSiegeClan.getClan());
        }
    }

    @Override
    public void announce(int val, SystemMsg msgId) {
        int min = val / 60;
        int hour = min / 60;

        SystemMessagePacket msg = null;

        if (min < 0)
            Announcements.announceToAll("Fortress Siege of " + getResidence().getName() + " start at " + (min * -1) + " minutes.");
        else if (val == 0) {
            Announcements.announceToAll("Siege of " + getResidence().getName() + " Fortress started!");
            L2GameServerPacket announcePacket = AAScreenStringPacketPresets.ANNOUNCE
                    .addOrUpdate("Siege of " + getResidence().getName() + " Fortress started!");
            BroadcastService.getInstance().sendToAll(announcePacket);
        } else if (hour > 0)
            msg = new SystemMessagePacket(SystemMsg.S1).addString(hour + " hour(s) until the fortress battle starts.");
        else if (min > 0)
            msg = new SystemMessagePacket(SystemMsg.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS).addNumber(min);
        else
            msg = new SystemMessagePacket(SystemMsg.S1).addString("This fortress siege will end in " + val + " second(s)!");

        if (msg != null)
            broadcastInZone(msg);
    }

    @Override
    public void teleportPlayerToEvent(Player player) {
        Location[] locs = player.getFraction().canAttack(_residence.getFraction())
                ? fortressAttackersTeleports.get(_residence.getId())
                : fortressDefendersTeleports.get(_residence.getId());

        if (locs == null)
            return;

        var loc = player.getFraction().canAttack(_residence.getFraction())
                ? locs[player.getFraction().ordinal() - 1]
                : Rnd.get(locs);

        player.teleToLocation(loc);
    }


    private class ZoneEnterLeaveListener implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(Zone zone, Creature creature) {
            if (!creature.containsEvent(FortressSiegeEvent.this))
                creature.addEvent(FortressSiegeEvent.this);

            if (!creature.isPlayer()) {
                return;
            }

            Player player = creature.getPlayer();

            if (isInProgress()) {
                TimeCounter.INSTANCE.addPlayer(FortressSiegeEvent.this, "reward", player);
            }

            Fraction fraction = getOwnerFraction();
            if (fraction != Fraction.NONE) {
                int enemies = (int) zone.getInsidePlayers().stream()
                        .filter(p -> fraction.canAttack(p.getFraction()))
                        .count();

                if (enemies > Config.GVE_FORTRESS_ZONE_ENEMY_COUNT
                        && System.currentTimeMillis() > _lastAlarmTime + Config.GVE_FORTRESS_ZONE_ENEMY_INTERVAL)
                    manageAlarm(false);
            }

            if (isInProgress())
                broadcastCrystalStatus(player, true);
        }

        @Override
        public void onZoneLeave(Zone zone, Creature creature) {
            creature.removeEvent(FortressSiegeEvent.this);

            if (!creature.isPlayer()) {
                return;
            }

            Player player = creature.getPlayer();

            despawnSiegeSummons(player);

            if (isInProgress())
                TimeCounter.INSTANCE.removePlayer(FortressSiegeEvent.this, "reward", player);

            if (isInProgress())
                broadcastCrystalStatus(player, false);
        }
    }

    public void manageAlarm(boolean siege) {
        if (!siege) {
            _lastAlarmTime = System.currentTimeMillis();

            List<DoorObject> doors = getObjects("doors");
            for (DoorObject door : doors)
                door.close(this);
        }

        Fraction f = getResidence().getFraction();
        if (f != Fraction.NONE) {
            if (!siege) {
                Announcements.announceToFraction(f, "Near the " + getResidence().getName() + " was found enemies! Need protection!");
            } else {
                Announcements.announceToAll(getResidence().getName() + " was attacked! Siege started!");
                GveMessageUtil.updateProtectMessage(f);
            }
        } else if (siege) {
            Announcements.announceToAll(getResidence().getName() + " was attacked! Siege started!");
        }
    }

    private void increaseReward() {
        _reward = Math.min(Config.GVE_FORTRESS_REWARD_MAX, _reward += Config.GVE_FORTRESS_REWARD_INCREASE);
        updateReward();
    }

    public int getReward() {
        return _reward;
    }

    private void loadReward() {
        _reward = ServerVariables.getInt(eventServerVariable, 0);
    }

    private void updateReward() {
        ServerVariables.set(eventServerVariable, _reward);
    }

    public void broadcastCrystalStatus() {
        broadcastCrystalStatus(null, true);
    }

    public void broadcastCrystalStatus(Player player, boolean addPacket) {
        L2GameServerPacket packet;
        if (addPacket) {
            List<SpawnExFortObject> crystals = getObjects("crystals");

            int current = 0;
            if (crystals != null) {
                current = crystals.stream()
                        .map(SpawnExObject::getSpawns)
                        .flatMap(Collection::stream)
                        .mapToInt(Spawner::getCurrentCount).sum();
            }

            int max = 2;
            packet = AAScreenStringPacketPresets.SIEGE_CRYSTALS
                    .addOrUpdate("CRYSTALS:\n" + current + "/" + max);
        } else
            packet = AAScreenStringPacketPresets.SIEGE_CRYSTALS.remove();

        if (player != null)
            player.sendPacket(packet);
        else {
            for (Player p : getPlayersInZone())
                p.sendPacket(packet);
        }
    }

    public Duration getTimeToStartSiege() {
        Instant lastSiegeDateMillis = Instant.ofEpochMilli(getResidence().getLastSiegeDate().getTimeInMillis());
        return Duration.between(Instant.now(), lastSiegeDateMillis.plus(Config.GVE_FORTRESS_SIEGE_INTERVAL));
    }
}
