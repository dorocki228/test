package l2s.gameserver.handler.effects.impl.instant;

import l2s.commons.string.StringArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 **/
public class i_call_random_skill extends i_abstract_effect {
    private final List<SkillEntry> _skills = new ArrayList<SkillEntry>();

    public i_call_random_skill(EffectTemplate template) {
        super(template);

        int[][] skills = StringArrayUtils.stringToIntArray2X(getParams().getString("skills"), ";", "-");
        for (int[] skillArr : skills) {
            SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillArr[0], skillArr.length >= 2 ? skillArr[1] : 1);
            if (skillEntry != null)
                _skills.add(skillEntry);
        }
    }

    @Override
    public void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic) {
        if (_skills.isEmpty())
            return;

        SkillEntry skillEntry = Rnd.get(_skills);
        if (skillEntry == null)
            return;

        Skill skill = skillEntry.getTemplate();
        if (skill.getReuseDelay() > 0 && caster.isSkillDisabled(skill))
            return;

        if (skillEntry.checkCondition(caster, target, true, true, true, false, true)) {
            List<Creature> targets = skill.getTargets(caster, target, skillEntry, false, true, false);

            if (!skill.isNotBroadcastable() && !caster.isCastingNow()) {
                for (Creature cha : targets)
                    caster.broadcastPacket(new MagicSkillUse(caster, cha, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
            }

            caster.callSkill(target, skillEntry, targets, false, true, null);
            caster.disableSkill(skill, skill.getReuseDelay());
        }
    }
}
