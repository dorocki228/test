package services;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;

/**
 * @author KRonst
 */
public class BuffService {

    @Bypass("services.Buff:buff")
    public void buff(Player player, NpcInstance npc, String[] param) {
        if (player == null) {
            return;
        }
        if (param.length < 1) {
            return;
        }
        int skillId = Integer.parseInt(param[0]);
        int level = param.length == 1 ? 1 : Integer.parseInt(param[1]);
        SkillEntry skill = SkillHolder.getInstance().getSkillEntry(skillId, level);
        if (skill != null) {
            skill.getEffects(player, player);
        }
    }
}
