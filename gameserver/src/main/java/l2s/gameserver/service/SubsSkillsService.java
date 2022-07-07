package l2s.gameserver.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.listener.actor.player.OnLevelChangeListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author KRonst
 */
public class SubsSkillsService {

    private static final String SUBS_SKILLS_VAR = "sss_skills";
    private static final String SUBS_CLASSES_VAR = "sss_classes";
    private static final SubsSkillsService INSTANCE = new SubsSkillsService();

    private SubsSkillsService() {
        PlayerListenerList.addGlobal(new LevelChangeListener());
    }

    public static SubsSkillsService getInstance() {
        return INSTANCE;
    }

    public void restoreSkills(Player player) {
        String skillsVar = player.getVar(SUBS_SKILLS_VAR);
        if (skillsVar == null) {
            return;
        }
        String[] skills = skillsVar.split(";");
        for (String s : skills) {
            if (!s.isEmpty()) {
                int skillId = Integer.parseInt(s);
                SkillEntry skill = SkillHolder.getInstance().getSkillEntry(skillId, 1);
                if (skill != null) {
                    player.addSkill(skill);
                }
            }
        }
    }

    public void addSkill(Player player, SkillEntry skill) {
        player.addSkill(skill);
        String skillsVar = player.getVar(SUBS_SKILLS_VAR, "");
        player.setVar(SUBS_SKILLS_VAR, skillsVar + skill.getId() + ";");
    }

    public void removeSkill(Player player, SkillEntry skill) {
        String skillsVar = player.getVar(SUBS_SKILLS_VAR);
        if (skillsVar == null) {
            return;
        }
        String[] skills = skillsVar.split(";");
        ArrayUtils.remove(skills, String.valueOf(skill.getId()));

        StringBuilder sb = new StringBuilder();
        for (String s : skills) {
            if (!s.isEmpty()) {
                sb.append(s).append(";");
            }
        }

        player.setVar(SUBS_SKILLS_VAR, sb.toString());
        player.removeSkill(skill);
    }

    public void removeAllSkills(Player player) {
        if (getLearnedSkills(player).isEmpty()) {
            return;
        }
        int removedSkills = 0;
        final String skillsVar = player.getVar(SUBS_SKILLS_VAR);
        if (skillsVar == null) {
            return;
        }
        if (player.reduceAdena(Config.SUBS_SKILLS_RESET_PRICE, true)) {
            final String[] skills = skillsVar.split(";");
            for (String skill : skills) {
                if (!skill.isEmpty()) {
                    SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(Integer.parseInt(skill), 1);
                    if (skillEntry != null) {
                        player.removeSkill(Integer.parseInt(skill), false);
                        removedSkills++;
                    }
                }
            }
            player.setVar(SUBS_SKILLS_VAR, "");
            int count = removedSkills * Config.SUBS_SKILLS_PRICE;
            ItemFunctions.addItem(player, Config.SUBS_SKILLS_CURRENCY, count, true);
        } else {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        }
    }

    public List<Integer> getLearnedSkills(Player player) {
        String skillsVar = player.getVar(SUBS_SKILLS_VAR);
        if (skillsVar == null || skillsVar.isEmpty()) {
            return new ArrayList<>();
        }
        String[] skills = skillsVar.split(";");
        return Arrays.stream(skills).filter(s -> !s.isEmpty()).map(Integer::parseInt).collect(Collectors.toList());
    }

    public long getLearnedSkillsCount(Player player) {
        String skillsVar = player.getVar(SUBS_SKILLS_VAR);
        if (skillsVar == null || skillsVar.isEmpty()) {
            return 0;
        }
        final String[] skills = skillsVar.split(";");
        return Arrays.stream(skills).filter(s -> !s.isEmpty()).count();
    }

    private static class LevelChangeListener implements OnLevelChangeListener {

        @Override
        public void onLevelChange(Player player, int oldLvl, int newLvl) {
            if (newLvl != Config.SUBS_SKILLS_REWARD_RECEIVE_LEVEL || player.getActiveSubClass().isBase()) {
                return;
            }

            String classesVar = player.getVar(SUBS_CLASSES_VAR);
            String[] classes = classesVar == null ? new String[0] : classesVar.split(";");
            if (classes.length == Config.SUBS_SKILLS_REWARD_MAX_COUNT) {
                return;
            }

            String currentClassId = String.valueOf(player.getClassId().getId());
            String parentClassId = String.valueOf(player.getClassId().getParent().getId());
            if (ArrayUtils.contains(classes, currentClassId) || ArrayUtils.contains(classes, parentClassId)) {
                return;
            }

            player.getInventory().addItem(Config.SUBS_SKILLS_REWARD_ITEM, 1);
            player.sendMessage(new CustomMessage("services.subsskills.reward"));

            StringBuilder sb = new StringBuilder();
            for (String c : classes) {
                if (!c.isEmpty()) {
                    sb.append(c).append(";");
                }
            }

            sb.append(currentClassId).append(";");
            player.setVar(SUBS_CLASSES_VAR, sb.toString());
        }
    }
}
