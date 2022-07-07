package l2s.gameserver.component.player;

import com.google.common.math.DoubleMath;
import l2s.gameserver.Config;
import l2s.gameserver.component.AbstractComponent;
import l2s.gameserver.dao.ConfrontationDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;

import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ConfrontationComponent extends AbstractComponent<Player> {
    private final Map<Integer, SkillEntry> skills = new ConcurrentHashMap<>();
    private int currentPeriodPoints;
    private int totalPoints;
    private int availablePoints;

    public ConfrontationComponent(Player player) {
        super(player);
    }

    @Override
    public void restore() {
        ConfrontationDAO.getInstance().selectPointFromPlayer(this);
        ConfrontationDAO.getInstance().selectPlayerSkills(this);
    }

    @Override
    public void store() {
        ConfrontationDAO.getInstance().updateConfrontation(this);
    }

    public int getCurrentPeriodPoints() {
        return currentPeriodPoints;
    }

    public synchronized void resetPoints(int mask) {
        if((mask & 0x1) == 0x1)
            currentPeriodPoints = Config.FACTION_WAR_START_POINTS;
        if((mask & 0x2) == 0x2)
            totalPoints = 0;
    }

    public void setCurrentPeriodPoints(int currentPeriodPoints) {
        this.currentPeriodPoints = currentPeriodPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(int availablePoints) {
        this.availablePoints = availablePoints;
    }

    public synchronized void decrementCurrentPeriodPoints(int points) {
        currentPeriodPoints -= Math.abs(points);
        currentPeriodPoints = Math.max(0, currentPeriodPoints);
    }

    public synchronized void incrementPoints(int points) {
        var ratedPoints = DoubleMath.roundToInt(
                points * getObject().getConfrontationPointsRate(), RoundingMode.CEILING);
        availablePoints += ratedPoints;
        totalPoints += points;
        currentPeriodPoints += points;
    }

    public int getTrustLevel() {
        for (int i = 0; i < Config.FACTION_WAR_TRUST_LEVEL_POINTS.length; i++) {
            int current = Config.FACTION_WAR_TRUST_LEVEL_POINTS[i];
            if(i + 1 == Config.FACTION_WAR_TRUST_LEVEL_POINTS.length) {
                if(totalPoints>= current)
                    return i + 1;
                else
                    return 0;
            }
            int next = Config.FACTION_WAR_TRUST_LEVEL_POINTS[i + 1];
            if(totalPoints >= current && totalPoints <= next)
                return i + 1;
        }
        return 0;
    }

    public SkillEntry addSkill(SkillEntry entry, boolean restore) {
        boolean[] update = new boolean[]{true};
        final SkillEntry newSkill = skills.compute(entry.getId(), (k, v) -> {
            if(v == null || v.getLevel() < entry.getLevel())
                return entry;
            update[0] = false;
            return v;
        });
        if(update[0] || restore) {
            if(!restore)
                ConfrontationDAO.getInstance().updateSkill(getObject().getObjectId(), newSkill);
            if(Config.FACTION_WAR_ENABLED)
                getObject().addSkill(entry);
        }
        return newSkill;
    }

    public SkillEntry addSkill(SkillEntry entry) {
        return addSkill(entry, false);
    }

    public SkillEntry getSkill(int id) {
        return skills.get(id);
    }

    public int getSkillLevel(int id) {
        return Optional.ofNullable(skills.get(id)).map(SkillEntry::getLevel).orElse(0);
    }

    public Map<Integer, SkillEntry> getSkills() {
        return skills;
    }

    public synchronized boolean reduceFixedCount(int type, int count, boolean update) {
        if(type == 1) {
            if(availablePoints < count)
                return false;
            availablePoints -= count;
        } else
            return true;
        if(update)
            ConfrontationDAO.getInstance().updateConfrontation(this);
        return true;
    }
}
