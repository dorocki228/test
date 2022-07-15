package l2s.gameserver.handler.effects;

import l2s.gameserver.model.AbnormalTypeList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.skill.SkillTarget;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncOwner;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 **/
public class EffectHandler implements FuncOwner {
    public static String getName(Class<? extends EffectHandler> cls) {
        return cls.getSimpleName().replaceAll("^(Effect)?(.*?)(EffectHandlerHolder)?$", "$2").toLowerCase();
    }

    private final String _name;
    private final EffectTemplate _template;

    private int ticks;

    public EffectHandler(EffectTemplate template) {
        _name = getName(getClass());
        _template = template;
    }

    @Override
    public final boolean isFuncEnabled() {
        return true;
    }

    @Override
    public final boolean overrideLimits() {
        return false;
    }

    public final String getName() {
        return _name;
    }

    public final EffectTemplate getTemplate() {
        return _template;
    }

    public final Skill getSkill() {
        return _template.getSkill();
    }

    public final StatsSet getParams() {
        return _template.getParams();
    }

    @Deprecated
    public final double getValue() {
        return _template.getValue();
    }

    /**
     * Gets the effect ticks
     *
     * @return the ticks
     */
    public int getTicks() {
        return ticks;
    }

    /**
     * Sets the effect ticks
     *
     * @param ticks the ticks
     */
    protected void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public double getTicksMultiplier() {
        return (getTicks() * 666.0) / 1000f;
    }

    @Deprecated
    public Func[] getStatFuncs() {
        return _template.getStatFuncs(this);
    }

    @Deprecated
    public Condition getCondition() {
        return _template.getCondition();
    }

    public Collection<SkillEntry> getCalledSkills() {
        return Collections.emptyList();
    }

    // TODO Избавиться
    public boolean checkBlockedAbnormalType(Abnormal abnormal, Creature effector, Creature effected, AbnormalTypeList abnormalTypeList) {
        return false;
    }

    // TODO Избавиться
    public boolean isHidden() {
        return false;
    }

    // TODO Избавиться
    public boolean isSaveable() {
        return true;
    }

    private final boolean testCondition(Creature effector, Creature effected) {
        Condition cond = getCondition();
        return cond == null || cond.test(effector, effected, getSkill(), null, 0);
    }

    public final boolean checkPumpConditionImpl(@Nullable Abnormal abnormal, Creature effector, Creature effected) {
        if (!checkPumpCondition(abnormal, effector, effected))
            return false;
        return testCondition(effector, effected);
    }

    public final boolean checkActingConditionImpl(@Nullable Abnormal abnormal, Creature effector, Creature effected) {
        if (!checkActingCondition(abnormal, effector, effected))
            return false;
        return testCondition(effector, effected);
    }

    protected boolean checkActingCondition(@Nullable Abnormal abnormal, Creature effector, Creature effected) {
        return true;
    }

    /**
     * Calculates whether this effects land or not.<br>
     * If it lands will be scheduled and added to the character effect list.<br>
     * Override in effect implementation to change behavior. <br>
     * <b>Warning:</b> Must be used only for instant effects continuous effects will not call this they have their success handled by activate_rate.
     *
     * @param caster
     * @param target
     * @param skill
     * @return {@code true} if this effect land, {@code false} otherwise
     */
    public boolean calcSuccess(Creature caster, Creature target, Skill skill) {
        return true;
    }

    /**
     * Called upon cast.<br>
     *
     * @param caster
     * @param targets
     * @param skill
     * @param item
     */
    public void instantUse(Creature caster,
                           List<SkillTarget> targets,
                           AtomicBoolean soulShotUsed,
                           Cubic cubic) {
        for (SkillTarget skillTarget : targets) {
            Creature target = skillTarget.getTarget();

            // TODO move upper
            if (!getTemplate().getTargetType().checkTarget(target)) {
                continue;
            }

            if (!calcSuccess(caster, target, getSkill())) {
                continue;
            }

            instantUse(caster, target, soulShotUsed, skillTarget.getReflected(), cubic);
        }
    }

    /**
     * Called upon cast.<br>
     *
     * @param skill
     * @param item
     * @param caster
     * @param target
     * @param soulShotUsed
     * @param cubic
     */
    public void instantUse(Creature caster,
                           Creature target,
                           AtomicBoolean soulShotUsed,
                           boolean reflected,
                           Cubic cubic) {
        //
    }

    /**
     * Called when effect start.<br>
     *
     * @param caster
     * @param target
     * @param skill
     */
    public void pumpStart(@Nullable Abnormal abnormal, Creature caster, Creature target) {
        //
    }

    /**
     * Called when effect exit.<br>
     *
     * @param caster
     * @param target
     * @param skill
     */
    public void pumpEnd(@Nullable Abnormal abnormal, Creature caster, Creature target) {
        //
    }

    /**
     * Called on each tick.<br>
     * If the abnormal time is lesser than zero it will last forever.
     *
     * @param caster
     * @param target
     * @param skill
     * @return {@code false} if skill must be cancelled, {@code true} otherwise
     */
    public void tick(@Nullable Abnormal abnormal, Creature caster, Creature target) {
    }

    /**
     * @param caster
     * @param target
     * @param skill
     * @return {@code true} if pump can be invoked, {@code false} otherwise
     */
    protected boolean checkPumpCondition(@Nullable Abnormal abnormal, Creature caster, Creature target) {
        return true;
    }

    public void pump(Creature target, @Nullable SkillEntry skillEntry) {
        //
    }

    /**
     * @param target
     * @param skill
     * @return {@code false} if skill must be cancelled, {@code true} otherwise
     */
    public boolean consume(@Nullable Abnormal abnormal, Creature target) {
        return true;
    }

    public EffectHandler getImpl() {
        return this;
    }

    @Override
    public String toString() {
        return "EffectHandler{" +
                "_name='" + _name + '\'' +
                ", _template=" + _template +
                '}';
    }
}
