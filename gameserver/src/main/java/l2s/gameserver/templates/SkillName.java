package l2s.gameserver.templates;

public class SkillName {
    private final int skillId;
    private final int skillLevel;
    private final String name;
    private final String desc;

    public SkillName(int skillId, int skillLevel, String name, String desc) {
        this.skillId = skillId;
        this.skillLevel = skillLevel;
        this.name = name;
        this.desc = desc;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
