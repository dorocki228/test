package l2s.gameserver.templates;

public class FactionWarSkill {
    private final int skillId;
    private final int skillLevel;
    private final int minTrust;
    private final int cost;

    public FactionWarSkill(int skillId, int skillLevel, int minTrust, int cost) {
        this.skillId = skillId;
        this.skillLevel = skillLevel;
        this.minTrust = minTrust;
        this.cost = cost;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMinTrust() {
        return minTrust;
    }

    public int getCost() {
        return cost;
    }
}
