package l2s.gameserver.instancemanager;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class GveRewardManager
{
    private static final GveRewardManager INSTANCE = new GveRewardManager();
    private static final String KILL_REWARD_VAR = "GVE_KILL_REWARD_DAILY_COUNT";

    private final Table<Integer, Integer, Pair<AtomicInteger, Instant>> killPenalties;
    private final Table<Integer, Integer, Instant> killPenaltiesTime;

    private final Map<Integer, Pair<AtomicInteger, Instant>> deathPenalties = new HashMap<>();
    private final Map<Integer, Instant> deathPenaltiesTime = new HashMap<>();

    private final Table<Integer, Integer, Pair<AtomicInteger, Instant>> seriesPenalties;
    private final Table<Integer, Integer, Instant> seriesPenaltiesTime;

    private final CopyOnWriteArrayList<Integer> revivePenalties = new CopyOnWriteArrayList<>();

    public GveRewardManager()
    {
        killPenalties = Tables.synchronizedTable(HashBasedTable.create());
        killPenaltiesTime = Tables.synchronizedTable(HashBasedTable.create());
        seriesPenalties = Tables.synchronizedTable(HashBasedTable.create());
        seriesPenaltiesTime = Tables.synchronizedTable(HashBasedTable.create());
    }

    public static GveRewardManager getInstance()
    {
        return INSTANCE;
    }

    public void tryGiveReward(Player killer, Player assistant, List<Player> debuffers, Player victim)
    {
        tryGiveReward(killer, assistant, debuffers, victim, 1.0);
    }

    public void tryGiveReward(Player killer, Player assistant, List<Player> debuffers, Player victim, double adenaMod)
    {
        Party party = killer.getParty();
        if(party != null && party == victim.getParty()) {
            return;
        }
        double adenaReward = victim.getAdenaReward() * victim.getGreed();
        double expReward = victim.getExpReward();
        double spReward = victim.getSpReward();

        boolean validKiller = checkCondition(killer, victim);
        boolean validAssistant = !killer.equals(assistant) && !killer.isInSameParty(assistant);
        validAssistant = validAssistant && checkCondition(assistant, victim);

        debuffers = debuffers.stream()
                .filter(player -> player.getRef().get() != null)
                .filter(player -> checkCondition(player, victim))
                .collect(Collectors.toUnmodifiableList());
        boolean validDebuffers = !debuffers.isEmpty();

        if(!validKiller && !validAssistant && !validDebuffers)
        {
            return;
        }

        double killerShare;
        double assistantShare;
        double debufferShare;

        if(validKiller && validAssistant && validDebuffers)
        {
            killerShare = 0.5D;
            assistantShare = 0.3D;
            debufferShare = 0.2D / debuffers.size();
        }
        else if(validKiller && !validAssistant && validDebuffers)
        {
            killerShare = 0.8D;
            assistantShare = 0.0D;
            debufferShare = 0.2D / debuffers.size();
        }
        else if(!validKiller && validAssistant && validDebuffers)
        {
            killerShare = 0.0D;
            assistantShare = 0.8D;
            debufferShare = 0.2D / debuffers.size();
        }
        else if(!validKiller && !validAssistant && validDebuffers)
        {
            killerShare = 0.0D;
            assistantShare = 0.0D;
            debufferShare = 1.0D / debuffers.size();
        }
        else if(validKiller && validAssistant)
        {
            killerShare = 0.6D;
            assistantShare = 0.4D;
            debufferShare = 0.0D;
        }
        else if(validKiller && !validAssistant)
        {
            killerShare = 1.0D;
            assistantShare = 0.0D;
            debufferShare = 0.0D;
        }
        else if(!validKiller && validAssistant)
        {
            killerShare = 0.0D;
            assistantShare = 1.0D;
            debufferShare = 0.0D;
        }
        else //if(!validKiller && !validAssistant && !validDebuffers)
        {
            return;
        }

        if(killer.getFraction() == victim.getFraction() && Config.FACTION_REWARD_ADENA_PENALTY > 0) {
            assistantShare -= (assistantShare * Config.FACTION_REWARD_ADENA_PENALTY) / 100d;
            killerShare -= (killerShare * Config.FACTION_REWARD_ADENA_PENALTY) / 100d;
            debufferShare -= (debufferShare * Config.FACTION_REWARD_ADENA_PENALTY) / 100d;
        }

        if(killerShare > 0.0D)
        {
            giveReward(killer, victim, adenaReward * killerShare * adenaMod, expReward * killerShare,
                    spReward * killerShare, true, true);
            updatePenalties(killer, victim);
            killer.manageComboKill();
        }

        if(assistantShare > 0.0D)
        {
            giveReward(assistant, victim, adenaReward * assistantShare * adenaMod, expReward * assistantShare,
                    spReward * assistantShare, false, false);
            updatePenalties(assistant, victim);
        }

        if(debufferShare > 0.0D)
        {
            for(Player debuffer : debuffers) {
                giveReward(debuffer, victim, adenaReward * debufferShare * adenaMod,
                        expReward * debufferShare, spReward * debufferShare, false, false);
                updatePenalties(debuffer, victim);
            }
        }

        victim.broadcastUserInfo(true);
    }

    private void updatePenalties(Player player, Player victim)
    {
        incrementKillerPenalty(player, victim);
        incrementDeathPenalty(victim);
        incrementSeriesPenalty(player, victim);
    }

    private void incrementKillerPenalty(Player actor, Player victim)
    {
        int actorObjectId = actor.getObjectId();
        int victimObjectId = victim.getObjectId();

        if(killPenaltiesTime.contains(actorObjectId, victimObjectId))
        {
            return;
        }

        Pair<AtomicInteger, Instant> pair = killPenalties.get(actorObjectId, victimObjectId);
        if(pair == null)
        {
            Instant time = Instant.now().plus(Config.GVE_KILL_PENALTY_REMOVE_TIME);
            pair = new ImmutablePair<>(new AtomicInteger(0), time);
            killPenalties.put(actorObjectId, victimObjectId, pair);
        }
        else
        {
            if(Instant.now().isAfter(pair.getValue()))
            {
                killPenalties.remove(actorObjectId, victimObjectId);
                incrementKillerPenalty(actor, victim);
            }
        }

        int count = pair.getKey().incrementAndGet();
        if(count > Config.GVE_KILL_PENALTY_COUNT)
        {
            Instant time = Instant.now().plus(Config.GVE_KILL_PENALTY_TIME);
            killPenaltiesTime.put(actorObjectId, victimObjectId, time);
        }
    }

    private void incrementDeathPenalty(Player victim)
    {
        int victimObjectId = victim.getObjectId();

        if(deathPenaltiesTime.containsKey(victimObjectId))
        {
            return;
        }

        Pair<AtomicInteger, Instant> pair = deathPenalties.get(victimObjectId);
        if(pair == null)
        {
            Instant time = Instant.now().plus(Config.GVE_DEATH_PENALTY_REMOVE_TIME);
            pair = new ImmutablePair<>(new AtomicInteger(0), time);
            deathPenalties.put(victimObjectId, pair);
        }
        else
        {
            if(Instant.now().isAfter(pair.getValue()))
            {
                deathPenalties.remove(victimObjectId);
                incrementDeathPenalty(victim);
            }
        }

        int count = pair.getKey().incrementAndGet();
        if(count > Config.GVE_DEATH_PENALTY_COUNT)
        {
            Instant time = Instant.now().plus(Config.GVE_DEATH_PENALTY_TIME);
            deathPenaltiesTime.put(victimObjectId, time);
        }
    }

    private void incrementSeriesPenalty(Player actor, Player victim)
    {
        int actorObjectId = actor.getObjectId();
        int victimObjectId = victim.getObjectId();

        if(seriesPenaltiesTime.contains(actorObjectId, victimObjectId))
        {
            return;
        }

        Pair<AtomicInteger, Instant> pair = seriesPenalties.get(actorObjectId, victimObjectId);
        if(pair == null)
        {
            seriesPenalties.column(actorObjectId).clear();

            Instant time = Instant.now().plus(Config.GVE_SERIES_KILL_PENALTY_REMOVE_TIME);
            pair = new ImmutablePair<>(new AtomicInteger(0), time);
            seriesPenalties.put(actorObjectId, victimObjectId, pair);
        }
        else
        {
            if(Instant.now().isAfter(pair.getValue()))
            {
                seriesPenalties.remove(actorObjectId, victimObjectId);
                incrementSeriesPenalty(actor, victim);
            }
        }

        int count = pair.getKey().incrementAndGet();
        if(count > Config.GVE_SERIES_KILL_COUNT)
        {
            Instant time = Instant.now().plus(Config.GVE_SERIES_KILL_PENALTY_TIME);
            seriesPenaltiesTime.put(actorObjectId, victimObjectId, time);
        }
    }

    private void giveReward(Player activeChar, Player victim, double adena, double exp, double sp,
                            boolean killer, boolean addClanReputation)
    {
        if(exp < 0 || sp < 0)
        {
            System.out.println("negative reward exp=" + exp + " sp=" + sp + " actor=" + activeChar + " target=" + victim);
            exp = Math.max(0, exp);
            sp = Math.max(0, sp);
        }

        adena *= activeChar.getRateAdena();
        exp *= activeChar.getRateExp();
        sp *= activeChar.getRateSp();

        Party party = activeChar.getParty();
        if(party != null)
        {
            var item = ItemFunctions.createItem(ItemTemplate.ITEM_ID_ADENA);
            item.setCount((long) adena);
            party.distributeItem(activeChar, item, null);
            party.distributeXpAndSp(activeChar, victim, exp, sp);

            party.forEach(member -> {
                if (member.isInZone(Zone.ZoneType.defend)) {
                    var prev = member.getVarInt("defend_value", 0);
                    member.setVar("defend_value", prev + 1);
                }
                if(member.isInZone(Zone.ZoneType.defendArtifact)) {
                    var prev = member.getVarInt("artifact_defend_value", 0);
                    member.setVar("artifact_defend_value", prev + 1);
                }
            });
        }
        else
        {
            activeChar.addAdena((long) adena, true);
            activeChar.addExpAndSp((long) exp, (long) sp);

            if (activeChar.isInZone(Zone.ZoneType.defend)) {
                var prev = activeChar.getVarInt("defend_value", 0);
                activeChar.setVar("defend_value", prev + 1);
            }
            if(activeChar.isInZone(Zone.ZoneType.defendArtifact)) {
                var prev = activeChar.getVarInt("artifact_defend_value", 0);
                activeChar.setVar("artifact_defend_value", prev + 1);
            }
        }

        if(addClanReputation)
        {
            Clan killerClan = activeChar.getClan();
            Clan victimClan = victim.getClan();
            if(killerClan != null && victimClan != null)
            {
                if(victimClan.getReputationScore() > 0)
                {
                    killerClan.incReputation(Config.CLAN_WAR_REPUTATION_SCORE_PER_KILL, true, "ClanWar");
                    if(victim.getPledgeType() != -1)
                    {
                        victimClan.incReputation(-Config.CLAN_WAR_REPUTATION_SCORE_PER_KILL, true, "ClanWar");
                    }
                }
            }
        }

        if(killer)
        {
            List<RewardItemData> rewardItemDataList;
            if(activeChar.isInSiegeZone() && victim.isInSiegeZone()) {
                rewardItemDataList = Config.GVE.killRewardsSiege();
            }
            else {
                rewardItemDataList = Config.GVE.killRewards();
            }
            final boolean mercenary = activeChar.isMercenary();
            if (activeChar.getVarLong(KILL_REWARD_VAR, 0L) < Config.GVE.killDailyRewardsLimit()) {
                rewardItemDataList.forEach(rewardItemData -> {
                    double chance = rewardItemData.getChance();
                    if(mercenary) {
                        chance += chance * Config.GVE.rewardPercentMercenaries() / 100;
                    }
                    chance = Math.min(100, chance);
                    if(!Rnd.chance(chance)) {
                        return;
                    }
                    rewardPlayer(activeChar, rewardItemData);

                });
            }
        }
    }

    private void rewardPlayer(Player activeChar, RewardItemData rewardItemData) {
        var party = activeChar.getParty();
        if (party != null) {
            var member = party.getRandomPartyMember();
            long count = rewardItemData.getRandomCount();
            ItemFunctions.addItem(member, rewardItemData.getId(), count);
            storeRewardVar(member, count);
        } else {
            long count = rewardItemData.getRandomCount();
            ItemFunctions.addItem(activeChar, rewardItemData.getId(), count);
            storeRewardVar(activeChar, count);
        }
    }

    public boolean checkCondition(Player activeChar, Player victim)
    {
        if(activeChar == null)
        {
            return true;
        }

        if(revivePenalties.contains(victim.getObjectId()))
        {
            return false;
        }

        if(Objects.equals(activeChar, victim))
        {
            return true;
        }

        if(HwidUtils.INSTANCE.isSameHWID(activeChar, victim))
        {
            return false;
        }

        if(activeChar.isInOfflineMode() || victim.isInOfflineMode())
        {
            return false;
        }

        // kill penalty
        Instant killPenaltyTime = killPenaltiesTime.get(activeChar.getObjectId(), victim.getObjectId());
        if(killPenaltyTime != null)
        {
            if(Instant.now().isBefore(killPenaltyTime))
            {
                return false;
            }
            else
            {
                killPenaltiesTime.remove(activeChar.getObjectId(), victim.getObjectId());
                activeChar.broadcastCharInfo();
            }
        }

        // death penalty
        Instant deathPenaltyTime = deathPenaltiesTime.get(victim.getObjectId());
        if(deathPenaltyTime != null)
        {
            if(Instant.now().isBefore(deathPenaltyTime))
            {
                return false;
            }
            else
            {
                deathPenaltiesTime.remove(victim.getObjectId());
                activeChar.broadcastCharInfo();
            }
        }

        // series penalty
        Instant seriesPenaltyTime = seriesPenaltiesTime.get(activeChar.getObjectId(), victim.getObjectId());
        if(seriesPenaltyTime != null)
        {
            if(Instant.now().isBefore(seriesPenaltyTime))
            {
                return false;
            }
            else
            {
                seriesPenaltiesTime.remove(activeChar.getObjectId(), victim.getObjectId());
                activeChar.broadcastCharInfo();
            }
        }

        return true;
    }

    public void manageRevivePenalty(Player player, boolean add)
    {
        if(add)
        {
            revivePenalties.addIfAbsent(player.getObjectId());
        }
        else
        {
            revivePenalties.remove((Integer) player.getObjectId());
        }
    }

    private void storeRewardVar(Player player, long count) {
        long total = player.getVarLong(KILL_REWARD_VAR, 0L) + count;
        player.setVar(KILL_REWARD_VAR, total, getRewardLimitResetTime());
    }

    private long getRewardLimitResetTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 6);

        if (calendar.getTime().after(new Date()))
            return calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }
}
