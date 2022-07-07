package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author KRonst
 */
public class EffectGreedOfMomot extends Abnormal {

    private static final double DEFAULT_MOD = 1.0;
    private final double mod;

    public EffectGreedOfMomot(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template) {
        super(creature, target, skill, reflected, template);
        this.mod = template.getParam().getDouble("mod", DEFAULT_MOD);
    }

    @Override
    protected void onStart() {
        super.onStart();
        _effected.setGreed(mod);
    }

    @Override
    protected void onExit() {
        super.onExit();
        _effected.setGreed(DEFAULT_MOD);
    }
}
