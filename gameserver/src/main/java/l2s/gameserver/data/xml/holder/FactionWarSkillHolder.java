package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.FactionWarSkill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FactionWarSkillHolder extends AbstractHolder {
    private static final FactionWarSkillHolder INSTANCE = new FactionWarSkillHolder();
    private final List<FactionWarSkill> skillList = new ArrayList<>();

    private FactionWarSkillHolder() {
    }

    public static FactionWarSkillHolder getInstance() {
        return INSTANCE;
    }

    public void add(FactionWarSkill skill) {
        skillList.add(skill);
    }

    public List<FactionWarSkill> getSkillList() {
        return skillList;
    }

    public List<FactionWarSkill> getSkillListFromTrustLevel(int trustLevel) {
        return skillList.stream().filter(s -> s.getMinTrust() <= trustLevel).collect(Collectors.toList());
    }

    public int getMaxLevel(int skillId, int trustLevel) {
        return skillList.stream().filter(s -> s.getSkillId() == skillId && s.getMinTrust() <= trustLevel).max(Comparator.comparingInt(FactionWarSkill::getSkillLevel)).map(FactionWarSkill::getSkillLevel).orElse(0);
    }

    public FactionWarSkill getSkill(int skillId, int skillLevel) {
        return skillList.stream().filter(s -> s.getSkillId() == skillId && s.getSkillLevel() == skillLevel).findFirst().orElse(null);
    }

    @Override
    public int size() {
        return skillList.size();
    }

    @Override
    public void clear() {
        skillList.clear();
    }
}
