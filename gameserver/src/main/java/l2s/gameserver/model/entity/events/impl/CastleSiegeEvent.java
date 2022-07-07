package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CastleDamageZoneDAO;
import l2s.gameserver.dao.CastleDoorUpgradeDAO;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets;
import l2s.gameserver.service.BroadcastService;
import l2s.gameserver.service.MoraleBoostService;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.MerchantGuard;
import l2s.gameserver.time.counter.TimeCounter;
import l2s.gameserver.utils.Location;

public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject> {
    public static final int MAX_SIEGE_CLANS = Config.MAX_SIEGE_CLANS;
    public static final int BASE_SIEGE_FAME = 72;
    public static final String DEFENDERS_WAITING = "defenders_waiting";
    public static final String DEFENDERS_REFUSED = "defenders_refused";
    public static final String CONTROL_TOWERS = "control_towers";
    public static final String FLAME_TOWERS = "flame_towers";
    public static final String BOUGHT_ZONES = "bought_zones";
    public static final String GUARDS = "guards";
    public static final String HIRED_GUARDS = "hired_guards";
    private static final String LIGHT_SIDE = "light_side";
    private static final String DARK_SIDE = "dark_side";
    private static final String WATER_FRACTION = "water_fraction";
    private static final String FIRE_FRACTION = "fire_fraction";
    private static final String SIEGE_CRON = "siege_cron";

    private boolean _firstStep;
    private final SchedulingPattern _siegeCron;

    private final ZoneEnterLeaveListener _zoneListener = new ZoneEnterLeaveListener();

    private ScheduledFuture<?> broadcastCrystalStatusTask;
    private Fraction _oldFraction;

    public CastleSiegeEvent(MultiValueSet<String> set) {
        super(set);
        _firstStep = false;
        _killListener = new KillListener();
        _siegeCron = new SchedulingPattern(set.getString(SIEGE_CRON));
    }

    @Override
    public Optional<String> getOnScreenMessage(Player player) {
        if (!isInProgress())
            return Optional.empty();

        return Optional.ofNullable(getName());
    }

    @Override
    public void initEvent() {
        _residence = ResidenceHolder.getInstance().getResidence(getId());

        Castle c = getResidence();
        if (c.getResidenceSide() == ResidenceSide.NEUTRAL) {
            switch (c.getId()) {
                case 4:     // Oren
                case 8:     // Rune
                    c.setResidenceSide(ResidenceSide.DARK, false);
                    break;
                case 9:     // Schuttgart
                case 10:    // Elven
                    c.setResidenceSide(ResidenceSide.LIGHT, false);
                    break;
            }
        }

        super.initEvent();

        List<DoorObject> doorObjects = getObjects("doors");
        addObjects(BOUGHT_ZONES, CastleDamageZoneDAO.getInstance().load(getResidence()));
        for (DoorObject doorObject : doorObjects) {
            doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
            doorObject.getDoor().addListener(_doorDeathListener);
        }

        List<ZoneObject> zones = getObjects(SIEGE_ZONES);
        for (ZoneObject zone : zones)
            zone.getZone().addListener(_zoneListener);
    }

    public void takeCastle(Player player) {
        ResidenceSide side = player.getFraction() == Fraction.FIRE ? ResidenceSide.DARK : ResidenceSide.LIGHT;

        getResidence().setResidenceSide(side, false);

        processStep(player);

        Clan newOwnerClan = player.getClan();
        String ownerName = newOwnerClan == null ? player.getFraction().toString() : newOwnerClan.getName();
        Announcements.announceToAll(ownerName + " successfully captured " + getResidence().getName() + '.');

        getResidence().broadcastResidenceState();
    }

    @Override
    public void processStep(Player newOwner) {
        Clan newOwnerClan = newOwner.getClan();
        Clan oldOwnerClan = getResidence().getOwner();

        getResidence().changeOwner(newOwnerClan);

        if (Config.GVE_FARM_ENABLED) {
            SteadDataHolder.getInstance().getStead(getId()).changeOwner();
        }

        if(getOwnerFraction() != Fraction.NONE)
            MoraleBoostService.getInstance().castleSuccessAttack(getResidence());

        if (oldOwnerClan != null) {
            SiegeClanObject ownerSiegeClan = getSiegeClan("defenders", oldOwnerClan);
            if (ownerSiegeClan != null) {
                removeObject("defenders", ownerSiegeClan);
                ownerSiegeClan.setType("attackers");
                addObject("attackers", ownerSiegeClan);
            }
            removeState(4);
        }

        for (CastleSiegeEvent castleSiege : EventHolder.getInstance().getEvents(CastleSiegeEvent.class)) {
            if (castleSiege == this)
                continue;
            if (!castleSiege.isInProgress())
                continue;
            SiegeClanObject siegeClan2 = castleSiege.getSiegeClan("attackers", newOwnerClan);
            if (siegeClan2 != null) {
                siegeClan2.deleteFlag();
                castleSiege.removeObject("attackers", siegeClan2);
                for (Player player : newOwnerClan.getOnlineMembers(0)) {
                    player.removeEvent(castleSiege);
                    player.broadcastCharInfo();
                }
            }
            siegeClan2 = castleSiege.getSiegeClan("defenders", newOwnerClan);
            if (siegeClan2 == null)
                continue;
            siegeClan2.deleteFlag();
            castleSiege.removeObject("defenders", siegeClan2);
            for (Player player : newOwnerClan.getOnlineMembers(0)) {
                player.removeEvent(castleSiege);
                player.broadcastCharInfo();
            }
        }

        updateParticles(true, "attackers", "defenders");

        teleportPlayers(FROM_RESIDENCE_TO_TOWN);

        if (!_firstStep) {
            _firstStep = true;
            broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, "attackers", "defenders");
            if (_oldOwner != null) {
                if (containsObjects("hired_guards"))
                    spawnAction("hired_guards", false);
                damageZoneAction(false);
                removeObjects("hired_guards");
                removeObjects(BOUGHT_ZONES);
                CastleDamageZoneDAO.getInstance().delete(getResidence());
            }

            List<DoorObject> doorObjects = getObjects("doors");
            for (DoorObject doorObject : doorObjects) {
                doorObject.setWeak(true);
                doorObject.setUpgradeValue(this, 0);
                CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
            }
        }
        spawnAction("doors", true);

        spawnAction(CONTROL_TOWERS, false);
        spawnAction(CONTROL_TOWERS, true);
        spawnAction(FLAME_TOWERS, false);
        spawnAction(FLAME_TOWERS, true);
        spawnAction(GUARDS, false);
        spawnAction(GUARDS, true);
        if(getOwnerFraction() != Fraction.NONE) {
            String defenderGroup = getOwnerFraction().name().toLowerCase() + "_defender";
            spawnAction(defenderGroup, false);
            spawnAction(defenderGroup, true);

            String oldAttackerGroup = getOwnerFraction().name().toLowerCase() + "_attacker";
            spawnAction(oldAttackerGroup, false);
            String newAttackerGroup = getOwnerFraction().revert().name().toLowerCase() + "_attacker";
            spawnAction(newAttackerGroup, true);
        }

        despawnSiegeSummons();
    }

    @Override
    public void startEvent() {
        _oldOwner = getResidence().getOwner();
        _oldFraction = getOwnerFraction();
        if (_oldOwner != null) {
            addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
            if (!getResidence().getSpawnMerchantTickets().isEmpty()) {
                for (ItemInstance item : getResidence().getSpawnMerchantTickets()) {
                    MerchantGuard guard = getResidence().getMerchantGuard(item.getItemId());
                    addObject("hired_guards", new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));
                    item.deleteMe();
                }
                CastleHiredGuardDAO.getInstance().delete(getResidence());
                if (containsObjects("hired_guards"))
                    spawnAction("hired_guards", true);
            }
        }
        SiegeClanDAO.getInstance().delete(getResidence());
        updateParticles(true, "attackers", "defenders");
        broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT, "attackers");
        broadcastTo(new SystemMessagePacket(SystemMsg.YOU_ARE_PARTICIPATING_IN_THE_SIEGE_OF_S1_THIS_SIEGE_IS_SCHEDULED_FOR_2_HOURS).addResidenceName(getResidence()), "attackers", "defenders");

        addState(4);

        // TODO move to xml
        TimeCounter.INSTANCE.start(this, "reward");

        super.startEvent();
        if (_oldOwner == null)
            initControlTowers();
        else
            damageZoneAction(true);

        getPlayersInZone().forEach(player ->
        {
            TimeCounter.INSTANCE.addPlayer(this, "reward", player);
        });

        broadcastCrystalStatusTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(
                this::broadcastCrystalStatus, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stopEvent(boolean force) {
        removeState(4);
        List<DoorObject> doorObjects = getObjects("doors");
        for (DoorObject doorObject : doorObjects)
            doorObject.setWeak(false);
        damageZoneAction(false);
        _blockedFameOnKill.clear();

        if (broadcastCrystalStatusTask != null)
            broadcastCrystalStatusTask.cancel(true);
        broadcastCrystalStatus(null, false);

        updateParticles(false, "attackers", "defenders");
        List<SiegeClanObject> attackers = removeObjects("attackers");
        for (SiegeClanObject siegeClan : attackers)
            siegeClan.deleteFlag();
        broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()));
        removeObjects("defenders");
        removeObjects("defenders_waiting");
        removeObjects("defenders_refused");
        Clan ownerClan = getResidence().getOwner();
        if (ownerClan != null) {
            if (_oldOwner == ownerClan) {
                getResidence().getOwner().setCastleDefendCount(getResidence().getOwner().getCastleDefendCount() + 1);
                getResidence().getOwner().updateClanInDB();
                ownerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addNumber(ownerClan.incReputation(1500, false, toString())));
            } else {
                broadcastToWorld(new SystemMessagePacket(SystemMsg.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(ownerClan.getName()).addResidenceName(getResidence()));
                ownerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addNumber(ownerClan.incReputation(3000, false, toString())));
/*
                if (_oldOwner != null)
                    _oldOwner.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOU_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS).addNumber(-_oldOwner.incReputation(-3000, false, toString())));
*/
                for (UnitMember member : ownerClan) {
                    Player player = member.getPlayer();
                    if (player != null)
                        player.sendPacket(PlaySoundPacket.SIEGE_VICTORY);
                }
            }
            for (Castle castle : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
                if (castle != getResidence())
                    continue;

                SiegeEvent<?, ?> siegeEvent = castle.getSiegeEvent();
                SiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", ownerClan);

                if (siegeClan2 == null)
                    siegeClan2 = siegeEvent.getSiegeClan("defenders", ownerClan);
                if (siegeClan2 == null)
                    siegeClan2 = siegeEvent.getSiegeClan("defenders_waiting", ownerClan);
                if (siegeClan2 == null)
                    continue;
                siegeEvent.getObjects(siegeClan2.getType()).remove(siegeClan2);
                SiegeClanDAO.getInstance().delete(castle, siegeClan2);
            }
        }

        if(getOwnerFraction() != Fraction.NONE)
        {
            if(_oldFraction == getOwnerFraction())
                MoraleBoostService.getInstance().castleSuccessDefense(getResidence());
            else
                MoraleBoostService.getInstance().castleSuccessAttack(getResidence());
        }

        getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
        getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
        //		else
        //		{
        //			this.broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));
        //			getResidence().getOwnDate().setTimeInMillis(0L);
        //			getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
        //			getResidence().setResidenceSide(ResidenceSide.NEUTRAL, false);
        //			getResidence().broadcastResidenceState();
        //		}
        despawnSiegeSummons();
        if (_oldOwner != null) {
            if (containsObjects("hired_guards"))
                spawnAction("hired_guards", false);
            removeObjects("hired_guards");
        }

        for (Player pc : getPlayersInZone()) {
            if (pc.isDead())
                continue;

            getResidence().manageZoneBonuses(pc, true);
            getResidence().manageZoneStatus(pc, true);
        }

        var elapsedTimeMap = TimeCounter.INSTANCE.stop(this, "reward");
        elapsedTimeMap.forEach(playerWithTime ->
                playerWithTime.ifPlayerSpendEnoughTimeOrElse(Config.GVE_CASTLE_REWARD_TIME_IN_ZONE,
                        player -> {
                            player.addItem(ItemTemplate.ITEM_ID_ADENA, Config.GVE_CASTLE_REWARD_COUNT_IN_ZONE, "CastleSiegeEventWin");
                            player.sendMessage("You received a reward for participating in the siege.");
                            return null;
                        },
                        player -> {
                            player.sendMessage("You did not spend enough time in event to receive a reward.");
                            return null;
                        }));

        Announcements.announceToAll("Castle Siege of " + getResidence().getName() + " ended.");
        if (getResidence().getFraction() != Fraction.NONE && getResidence().getOwner() == null)
            ThreadPoolManager.getInstance().schedule(this::makeAuctionWinner, AUCTION_TIME);

        super.stopEvent(force);
    }

    private void makeAuctionWinner() {
        List<AuctionSiegeClanObject> siegeClanObjects = removeObjects(ATTACKERS);
        // сортуруем с Макс к мин
        AuctionSiegeClanObject[] clans = siegeClanObjects.toArray(new AuctionSiegeClanObject[0]);
        Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());

        AuctionSiegeClanObject winnerSiegeClan = clans.length > 0 ? clans[0] : null;

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
    public void reCalcNextTime(boolean onInit) {
        clearActions();
        long currentTimeMillis = System.currentTimeMillis();
        Calendar startSiegeDate = getResidence().getSiegeDate();
        Calendar ownSiegeDate = getResidence().getOwnDate();
        if (onInit) {
            if (startSiegeDate.getTimeInMillis() > currentTimeMillis) {
                addState(REGISTRATION_STATE);
                registerActions();
            } else if (startSiegeDate.getTimeInMillis() == 0L || startSiegeDate.getTimeInMillis() <= currentTimeMillis)
                setNextSiegeTime();
        } else {
            if (getResidence().getOwner() != null) {
                getResidence().getSiegeDate().setTimeInMillis(0L);
                getResidence().setJdbcState(JdbcEntityState.UPDATED);
                getResidence().update();
            }
            setNextSiegeTime();
        }
    }

    @Override
    public void loadSiegeClans() {
        super.loadSiegeClans();
        addObjects("defenders_waiting", SiegeClanDAO.getInstance().load(getResidence(), "defenders_waiting"));
        addObjects("defenders_refused", SiegeClanDAO.getInstance().load(getResidence(), "defenders_refused"));
    }

    @Override
    public void removeState(int val) {
        super.removeState(val);
        if (val == REGISTRATION_STATE) {
            Castle r = getResidence();
            if (r.getId() == 1 && r.getOwnerId() != 0) {
                SiegeClanObject siegeClan = getSiegeClan(DEFENDERS, getResidence().getOwnerId());
                if (siegeClan != null) {
                    removeObject(siegeClan.getType(), siegeClan);
                    int lvl = siegeClan.getClan().getLevel();
                    if (lvl == 3 || lvl == 4) {
                        siegeClan = new SiegeClanObject("attackers", siegeClan.getClan(), 0L);
                        addObject(ATTACKERS, siegeClan);
                        SiegeClanDAO.getInstance().insert(r, siegeClan);
                    }
                }

                r.changeOwner(null);

                r.getSiegeEvent().spawnAction("castle_messenger_light_npc", false);
                r.getSiegeEvent().spawnAction("castle_messenger_dark_npc", false);
                r.getSiegeEvent().spawnAction("castle_peace_light_npcs", false);
                r.getSiegeEvent().spawnAction("castle_peace_dark_npcs", false);
                r.setResidenceSide(ResidenceSide.NEUTRAL, false);
                r.broadcastResidenceState();

                r.setJdbcState(JdbcEntityState.UPDATED);
                r.update();

            }
            broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED).addResidenceName(getResidence()));
        }
    }

    @Override
    public void announce(int val, SystemMsg msgId) {
        int min = val / 60;
        int hour = min / 60;

        SystemMessagePacket msg = null;

        if (min < 0)
            Announcements.announceToAll("Castle Siege of " + getResidence().getName() + " start at " + (min * -1) + " minutes.");
        else if (val == 0) {
            Announcements.announceToAll("Siege of " + getResidence().getName() + " Castle started!");
            L2GameServerPacket announcePacket = AAScreenStringPacketPresets.ANNOUNCE
                    .addOrUpdate("Siege " + getResidence().getName() + " Castle started!");
            BroadcastService.getInstance().sendToAll(announcePacket);
        } else if (hour > 0)
            msg = new SystemMessagePacket(SystemMsg.S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION).addNumber(hour);
        else if (min > 0)
            msg = new SystemMessagePacket(SystemMsg.S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION).addNumber(min);
        else
            msg = new SystemMessagePacket(SystemMsg.THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS).addNumber(val);

        if (msg != null)
            broadcastInZone(msg);
    }

    @Override
    public void teleportPlayerToEvent(Player player) {
        Location loc = _residence.getRestartPoint(player);
        if (loc != null)
            player.teleToLocation(loc);
    }

    private void initControlTowers() {
        List<SpawnExObject> objects = getObjects("guards");
        List<Spawner> spawns = new ArrayList<>();
        for (SpawnExObject o : objects)
            spawns.addAll(o.getSpawns());
        List<SiegeToggleNpcObject> ct = getObjects("control_towers");
        for (Spawner spawn : spawns) {
            Location spawnLoc = spawn.getRandomSpawnRange().getRandomLoc(ReflectionManager.MAIN.getGeoIndex());
            SiegeToggleNpcInstance closestCt = null;
            double distanceClosest = 0.0;
            for (SiegeToggleNpcObject c : ct) {
                SiegeToggleNpcInstance npcTower = c.getToggleNpc();
                double distance = npcTower.getDistance(spawnLoc);
                if (closestCt == null || distance < distanceClosest) {
                    closestCt = npcTower;
                    distanceClosest = distance;
                }
                closestCt.register(spawn);
            }
        }
    }

    private void damageZoneAction(boolean active) {
        if (containsObjects(BOUGHT_ZONES))
            zoneAction(BOUGHT_ZONES, active);
    }

    private void setNextSiegeTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(_siegeCron.next(System.currentTimeMillis()));
        broadcastToWorld(new SystemMessagePacket(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addResidenceName(getResidence()));
        clearActions();
        getResidence().getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
        getResidence().setJdbcState(JdbcEntityState.UPDATED);
        getResidence().update();
        registerActions();
        addState(REGISTRATION_STATE);
    }

    @Override
    public boolean isAttackersInAlly() {
        return !_firstStep;
    }

    @Override
    public boolean ifVar(String name) {
        switch (name) {
            case LIGHT_SIDE:
                return getResidence().getResidenceSide() == ResidenceSide.LIGHT;
            case DARK_SIDE:
                return getResidence().getResidenceSide() == ResidenceSide.DARK;
            case WATER_FRACTION:
                return getResidence().getFraction() == Fraction.WATER;
            case FIRE_FRACTION:
                return getResidence().getFraction() == Fraction.FIRE;
            default:
                return super.ifVar(name);
        }
    }

    public int crystalsAlive()
    {
        List<SiegeToggleNpcObject> crystals = getObjects(CONTROL_TOWERS);
        int alive = (int) crystals.stream().filter(SiegeToggleNpcObject::isAlive).count();

        crystals = getObjects(FLAME_TOWERS);
        alive += crystals.stream().filter(SiegeToggleNpcObject::isAlive).count();

        return alive;
    }

    public class KillListener implements OnKillListener {
        @Override
        public void onKill(Creature killer, Creature victim) {
            Player winner = killer.getPlayer();
            if (winner == null || !victim.isPlayer() || winner == victim || !checkIfInZone(victim) || !((Player) victim).isUserRelationActive() || !victim.containsEvent(CastleSiegeEvent.this))
                return;
            List<Player> players;
            if (winner.getParty() == null)
                players = Collections.singletonList(winner);
            else
                players = winner.getParty().getPartyMembers();
            double bonus = Config.ALT_PARTY_BONUS[Math.min(Config.ALT_PARTY_BONUS.length, players.size()) - 1];
            int value = (int) (Math.round(72.0 * bonus) / players.size());
            for (Player temp : players)
                if (temp.containsEvent(CastleSiegeEvent.this) && temp.getLevel() >= 40) {
                    if (!temp.isInRange(winner, Config.ALT_PARTY_DISTRIBUTION_RANGE))
                        continue;
                    temp.setFame(temp.getFame() + value, CastleSiegeEvent.this.toString(), true);
                }
            ((Player) victim).startEnableUserRelationTask(300000L, CastleSiegeEvent.this);
            _blockedFameOnKill.put(victim.getObjectId(), System.currentTimeMillis() + 300000L);
        }

        @Override
        public boolean ignorePetOrSummon() {
            return true;
        }
    }

    @Override
    public void broadcastTo(IBroadcastPacket packet, String... types) {
        Castle c = getResidence();
        Fraction f = getOwnerFraction();
        for (Player p : getPlayersInZone()) {
            for (String t : types) {
                if (f == p.getFraction() && t == DEFENDERS || f != p.getFraction() && t == ATTACKERS)
                    p.sendPacket(packet);
            }
        }
        super.broadcastTo(packet, types);
    }

    private class ZoneEnterLeaveListener implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(Zone zone, Creature creature) {
            if (!creature.containsEvent(CastleSiegeEvent.this))
                creature.addEvent(CastleSiegeEvent.this);

            if (!creature.isPlayer()) {
                return;
            }

            Player player = creature.getPlayer();

            if (isInProgress()) {
                TimeCounter.INSTANCE.addPlayer(CastleSiegeEvent.this, "reward", player);
            }

            if (isInProgress())
                broadcastCrystalStatus(player, true);
        }

        @Override
        public void onZoneLeave(Zone zone, Creature creature) {
            creature.removeEvent(CastleSiegeEvent.this);

            if (!creature.isPlayer()) {
                return;
            }

            Player player = creature.getPlayer();

            despawnSiegeSummons(player);

            if (isInProgress()) {
                TimeCounter.INSTANCE.removePlayer(CastleSiegeEvent.this, "reward", player);
            }

            if (isInProgress())
                broadcastCrystalStatus(player, false);
        }
    }

    public void broadcastCrystalStatus() {
        broadcastCrystalStatus(null, true);
    }

    public void broadcastCrystalStatus(Player player, boolean addPacket) {
        L2GameServerPacket packet;
        if (addPacket) {
            List<SiegeToggleNpcObject> crystals = getObjects(CONTROL_TOWERS);
            int current = (int) crystals.stream().filter(SiegeToggleNpcObject::isAlive).count();

            crystals = getObjects(FLAME_TOWERS);
            current += crystals.stream().filter(SiegeToggleNpcObject::isAlive).count();

            int max = 5;
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
}
