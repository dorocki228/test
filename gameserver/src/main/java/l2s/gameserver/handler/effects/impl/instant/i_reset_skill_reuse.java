package l2s.gameserver.handler.effects.impl.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 **/
public final class i_reset_skill_reuse extends i_abstract_effect {
    private final int _skillId;

    public i_reset_skill_reuse(EffectTemplate template) {
        super(template);
        _skillId = getParams().getInteger("id");
    }

    @Override
    public void instantUse(Creature caster, Creature target, AtomicBoolean soulShotUsed, boolean reflected, Cubic cubic) {
        SkillEntry skill = target.getKnownSkill(_skillId);
        if (skill != null) {
            target.enableSkill(skill.getTemplate());
            if (target.isPlayer()) {
                Player player = target.getPlayer();
                player.sendPacket(new SkillCoolTimePacket(player));
            }
        }
    }
}