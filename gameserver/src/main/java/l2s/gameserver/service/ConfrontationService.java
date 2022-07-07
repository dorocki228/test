package l2s.gameserver.service;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.component.player.ConfrontationComponent;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.dao.ConfrontationDAO;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.listener.actor.player.OnActiveClassListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.ArtifactInstance;
import l2s.gameserver.model.instances.OutpostInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.taskmanager.AutomaticTasks;
import l2s.gameserver.taskmanager.tasks.ConfrontationTask;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Language;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

// FIXME: 01.08.2018 перенести логику частично на слушатели
public class ConfrontationService {
    private static final ConfrontationService ourInstance = new ConfrontationService();
    private final AtomicInteger period = new AtomicInteger();
    private final Map<Fraction, AtomicInteger> totalPoints;
    public static final String LAST_UPDATE_VARIABLE = "confrontation_last_update";
    public static final String NEXT_UPDATE_VARIABLE = "confrontation_next_update";
    public static final String PERIOD_VARIABLE = "confrontation_period";
    public static final String WIN_VARIABLE = "confrontation_win_%s";

    public ConfrontationService() {
        totalPoints = Arrays.stream(Fraction.VALUES_WITH_NONE).collect(Collectors.toUnmodifiableMap(Function.identity(), v -> new AtomicInteger(0)));
    }

    public static ConfrontationService getInstance() {
        return ourInstance;
    }

    public void restore() {
        if (!Config.FACTION_WAR_ENABLED)
            return;
        period.set(ServerVariables.getInt(PERIOD_VARIABLE, 1));
        final long lastUpdate = ServerVariables.getLong(LAST_UPDATE_VARIABLE, 0);
        final long nextUpdate = ServerVariables.getLong(NEXT_UPDATE_VARIABLE, 0);
        final long currentTimeMillis = System.currentTimeMillis();
        if (nextUpdate > 0 && (nextUpdate < currentTimeMillis && lastUpdate < nextUpdate))
            newPeriod();
        PlayerListenerList.addGlobal(new PlayerListener());
        ConfrontationDAO.getInstance().selectTotalPoints(totalPoints);
        AutomaticTasks.getInstance().add(new ConfrontationTask(), true);
    }

    public AtomicInteger getTotalPoints(Fraction fraction) {
        return totalPoints.get(fraction);
    }

    public void newPeriod() {
        if (!Config.FACTION_WAR_ENABLED)
            return;
        int oldPeriod = period.get();
        final int nextPeriod = period.incrementAndGet();
        ServerVariables.set(PERIOD_VARIABLE, nextPeriod);
        ServerVariables.set(LAST_UPDATE_VARIABLE, System.currentTimeMillis());
        totalPoints.entrySet().stream().
                max(Comparator.comparingInt(e -> e.getValue().get())).
                filter(e -> e.getValue().get() > 0).
                ifPresent(p -> {
                    final Fraction fraction = p.getKey();
                    int oldWin = getFactionWin(fraction);
                    final List<ConfrontationComponent> updates = GameObjectsStorage.getPlayers().
                            stream().
                            filter(pl -> !pl.isLogoutStarted()).
                            map(Player::getConfrontationComponent).
                            collect(Collectors.toList());
                    ConfrontationDAO.getInstance().batchUpdateConfrontation(updates);
                    final List<Triple<Integer, String, Integer>> list = ConfrontationDAO.getInstance().selectConfrontationPlayer(fraction);
                    final Map<Integer, Language> playerLanguageMap = CharacterVariablesDAO.getInstance().getAllPlayerLanguage();
                    for (int i = 0; i < list.size(); i++) {
                        int place = i + 1;
                        long[][] rewards;
                        if (place == 1)
                            rewards = Config.FACTION_WAR_TOP_1_REWARD;
                        else if (place <= 10)
                            rewards = Config.FACTION_WAR_TOP_10_REWARD;
                        else if (place <= 50)
                            rewards = Config.FACTION_WAR_TOP_50_REWARD;
                        else if (place <= 100)
                            rewards = Config.FACTION_WAR_TOP_100_REWARD;
                        else if (place <= 300)
                            rewards = Config.FACTION_WAR_TOP_300_REWARD;
                        else
                            rewards = Config.FACTION_WAR_NOT_REWARD;
                        final Triple<Integer, String, Integer> pair = list.get(i);
                        final int objId = pair.getLeft();
                        final Language language = playerLanguageMap.getOrDefault(objId, Language.ENGLISH);
                        final String body = new CustomMessage("faction.war.s5").addNumber(oldPeriod).addNumber(place).toString(language);
                        final String title = language == Language.RUSSIAN ? "Противостояние" : "Confrontation";
                        Functions.sendSystemMail(objId, pair.getMiddle(), title, body, rewards);
                    }
                    GameObjectsStorage.getPlayers().forEach(pl -> {
                        Triple<Integer, String, Integer> pair = list.isEmpty() ? Triple.of(0, "Server Error", -1) : list.get(0);
                        Announcements.announceToPlayerFromStringHolder(pl, "faction.war.s1",
                                oldPeriod, fraction.toString(), getTotalPoints(fraction).get(), pair.getMiddle(), pair.getRight());
                        pl.getConfrontationComponent().resetPoints(0x01);
                    });
                    ServerVariables.set(String.format(WIN_VARIABLE, fraction.name().toLowerCase()), oldWin + 1);
                });
        ConfrontationDAO.getInstance().resetPeriod();
        totalPoints.values().forEach(a -> a.set(0));
    }

    public int getFactionWin(Fraction fraction) {
        return ServerVariables.getInt(String.format(WIN_VARIABLE, fraction.name().toLowerCase()), 0);
    }

    public int getPeriod() {
        return period.get();
    }

    class PlayerListener implements OnKillListener, OnActiveClassListener {
        @Override
        public void onKill(Creature p0, Creature p1) {
            if (!Config.FACTION_WAR_ENABLED)
                return;
            if (p0 == null || p1 == null)
                return;
            if (!p0.isPlayable() || !p1.isPlayer())
                return;
            if (p0.getLevel() < Config.FACTION_WAR_MIN_LEVEL || p1.getLevel() < Config.FACTION_WAR_MIN_LEVEL)
                return;
            final Player killer = p0.getPlayer();
            final Player victim = p1.getPlayer();
            if (killer.getFraction() == victim.getFraction())
                return;
            if (Config.FACTION_WAR_PVP_HWID_ENABLE && Objects.equals(killer.getHwidHolder(), victim.getHwidHolder()))
                return;
            final ConfrontationComponent confrontationComponent = victim.getConfrontationComponent();
            if (confrontationComponent.getCurrentPeriodPoints() < Math.abs(Config.FACTION_WAR_PVP_DEATH_POINTS))
                return;
            confrontationComponent.decrementCurrentPeriodPoints(Config.FACTION_WAR_PVP_DEATH_POINTS);
            killer.getConfrontationComponent().incrementPoints(Config.FACTION_WAR_PVP_KILL_POINTS);
            getTotalPoints(killer.getFraction()).addAndGet(Config.FACTION_WAR_PVP_KILL_POINTS);
            getTotalPoints(victim.getFraction()).addAndGet(-Math.abs(Config.FACTION_WAR_PVP_DEATH_POINTS));
        }

        @Override
        public boolean ignorePetOrSummon() {
            return true;
        }

        @Override
        public void onActiveClass(Player player, ClassId classId, SubClass newActiveSub, boolean onRestore) {
            if (onRestore)
                return;
            ConfrontationComponent confrontationComponent = player.getConfrontationComponent();
            Map<Integer, SkillEntry> skills = confrontationComponent.getSkills();
            skills.values().forEach(s -> player.addSkill(s, false));
        }
    }

    public void siege(Collection<Player> players, int point) {
        incrementPoints(players, point, "faction.war.s2");
    }

    public void changeFraction(Player player, Fraction oldFraction, Fraction newFraction) {
/*        getTotalPoints(oldFraction).addAndGet(-player.getConfrontationComponent().getCurrentPeriodPoints());
        player.getConfrontationComponent().resetPoints(0x01 | 0x02);
        player.getConfrontationComponent().store();*/
    }

    public void outpostDestroy(OutpostInstance outpostInstance, Playable playable) {
        if (!Config.FACTION_WAR_ENABLED || outpostInstance == null || playable == null)
            return;
        incrementPoints(getAroundPlayers(outpostInstance, playable.getFraction()), Config.FACTION_WAR_OUTPOST_KILL_POINTS, "faction.war.s2");
    }

    private void incrementPoints(Collection<Player> playerList, int points, String address) {
        playerList.stream().map(Player::getConfrontationComponent).forEach(c -> {
            c.incrementPoints(points);
            c.getObject().sendMessage(new CustomMessage(address).addNumber(points));
            getTotalPoints(c.getObject().getFraction()).addAndGet(points);
        });
    }

    public void artifactCapture(ArtifactInstance artifact, Player caster) {
        if (!Config.FACTION_WAR_ENABLED || artifact == null || caster == null)
            return;
        incrementPoints(getAroundPlayers(artifact, caster.getFraction()), Config.FACTION_WAR_ARTIFACT_CAPTURE_POINTS, "faction.war.s2");
    }

    public void bossKill(Collection<Player> collection) {
        if (!Config.FACTION_WAR_ENABLED)
            return;
        incrementPoints(collection, Config.FACTION_WAR_EPIC_BOSS_KILL_POINTS, "faction.war.s2");
    }

    public void addPoints(Player player, int points) {
        if (player == null || points == 0) {
            return;
        }
        ConfrontationComponent confrontationComponent = player.getConfrontationComponent();
        if (confrontationComponent == null) {
            return;
        }
        confrontationComponent.incrementPoints(points);
        player.sendMessage(new CustomMessage("faction.war.s2").addNumber(points));
        getTotalPoints(confrontationComponent.getObject().getFraction()).addAndGet(points);
    }

    private Collection<Player> getAroundPlayers(Creature actor, Fraction fraction) {
        return HwidUtils.INSTANCE.filterSameHwids(World.getAroundPlayers(actor, 2000, 2000)).stream()
                .filter(p -> p.getFraction() == fraction)
                .filter(p -> p.getLevel() >= Config.FACTION_WAR_MIN_LEVEL)
                .collect(Collectors.toUnmodifiableList());
    }
}
