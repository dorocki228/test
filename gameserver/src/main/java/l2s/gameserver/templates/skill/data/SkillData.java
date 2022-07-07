package l2s.gameserver.templates.skill.data;

/**
 * @author Java-man
 * @since 25.01.2019
 */
public class SkillData {
    private final int id;
    private final int level;

    public SkillData(int id, int level) {
        this.id = id;
        this.level = level;
    }

    public SkillData(String string) {
        var split = string.split(",");
        id = Integer.parseInt(split[0]);
        level = Integer.parseInt(split[1]);
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }
}
