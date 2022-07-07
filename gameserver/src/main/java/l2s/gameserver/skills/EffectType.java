package l2s.gameserver.skills;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.effects.*;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum EffectType
{
	AddSkills(EffectAddSkills.class, false),
	AgathionResurrect(EffectAgathionRes.class, true),
	AwakenForce(EffectAwakenForce.class, true),
	Betray(EffectBetray.class, true),
	BlessNoblesse(EffectBlessNoblesse.class, true),
	Buff(EffectBuff.class, false),
	Bluff(EffectBluff.class, true),
	DamageBlock(EffectDamageBlock.class, true),
	DistortedSpace(EffectDistortedSpace.class, true),
	CallSkills(EffectCallSkills.class, false),
	ConsumeSoulsOverTime(EffectConsumeSoulsOverTime.class, true),
	Charge(EffectCharge.class, false),
	CharmOfCourage(EffectCharmOfCourage.class, true),
	CPDamPercent(EffectCPDamPercent.class, true),
	Cubic(EffectCubic.class, true),
	DamageHealToEffector(EffectDamageHealToEffector.class, false),
	DestroySummon(EffectDestroySummon.class, true),
	DeathImmunity(EffectDeathImmunity.class, false),
	DeathPenalty(EffectDeathPenalty.class, false),
	Disarm(EffectDisarm.class, true),
	Discord(EffectDiscord.class, true),
	DispelOnHit(EffectDispelOnHit.class, true),
	EffectImmunity(EffectEffectImmunity.class, true),
	Enervation(EffectEnervation.class, false),
	FakeDeath(EffectFakeDeath.class, true),
	Fear(EffectFear.class, true),
	MoveToEffector(EffectMoveToEffector.class, true),
	Grow(EffectGrow.class, false),
	Hate(EffectHate.class, false),
	HealBlock(EffectHealBlock.class, true),
	HPDamPercent(EffectHPDamPercent.class, true),
	HpToOne(EffectHpToOne.class, true),
	IgnoreSkill(EffectIgnoreSkill.class, false),
	Immobilize(EffectImmobilize.class, true),
	Interrupt(EffectInterrupt.class, true),
	Invulnerable(EffectInvulnerable.class, false),
	Invisible(EffectInvisible.class, false),
	LockInventory(EffectLockInventory.class, false),
	CurseOfLifeFlow(EffectCurseOfLifeFlow.class, true),
	Laksis(EffectLaksis.class, true),
	LDManaDamOverTime(EffectLDManaDamOverTime.class, true),
	ManaDamOverTime(EffectManaDamOverTime.class, true),
	Meditation(EffectMeditation.class, false),
	MPDamPercent(EffectMPDamPercent.class, true),
	Mute(EffectMute.class, true),
	MuteAll(EffectMuteAll.class, true),
	Mutation(EffectMutation.class, true),
	MuteAttack(EffectMuteAttack.class, true),
	MutePhisycal(EffectMutePhisycal.class, true),
	NegateMark(EffectNegateMark.class, false),
	Paralyze(EffectParalyze.class, true),
	Petrification(EffectPetrification.class, true),
	RandomHate(EffectRandomHate.class, true),
	Relax(EffectRelax.class, true),
	RemoveTarget(EffectRemoveTarget.class, true),
	Salvation(EffectSalvation.class, true),
	ServitorShare(EffectServitorShare.class, true),
	SilentMove(EffectSilentMove.class, true),
	Sleep(EffectSleep.class, true),
	Stun(EffectStun.class, true),
	TeleportBlock(EffectBuff.class, true),
	ResurrectBlock(EffectBuff.class, true),
	KnockDown(EffectKnockDown.class, true),
	KnockBack(EffectKnockBack.class, true),
	FlyUp(EffectFlyUp.class, true),
	GetEffects(EffectGetEffects.class, true),
	ThrowHorizontal(EffectThrowHorizontal.class, true),
	ThrowUp(EffectThrowUp.class, true),
	Transformation(EffectTransformation.class, true),
	VisualTransformation(EffectVisualTransformation.class, true),
	TargetableDisable(EffectTargetableDisable.class, true),
	TargetLock(EffectTargetLock.class, true),
	ShadowStep(EffectShadowStep.class, false),
	RestoreCP(EffectRestoreCP.class, false),
	RestoreHP(EffectRestoreHP.class, false),
	RestoreMP(EffectRestoreMP.class, false),
	CPDrain(EffectCPDrain.class, true),
	HPDrain(EffectHPDrain.class, true),
	MPDrain(EffectMPDrain.class, true),
	AbsorbDamageToEffector(EffectBuff.class, false),
	AbsorbDamageToMp(EffectBuff.class, false),
	AbsorbDamageToSummon(EffectLDManaDamOverTime.class, true),
	ArmorBreaker(EffectArmorBreaker.class, false),
	RemoveTargetFromAll(EffectRemoveTargetFromAll.class, false),
	i_dispel_all(i_dispel_all.class, false),
	i_dispel_by_category(i_dispel_by_category.class, false),
	i_dispel_by_slot(i_dispel_by_slot.class, false),
	i_dispel_by_slot_myself(i_dispel_by_slot.class, false),
	i_dispel_by_slot_probability(i_dispel_by_slot_probability.class, false),
	i_delete_hate(i_delete_hate_of_me.class, true),
	i_delete_hate_of_me(i_delete_hate_of_me.class, true),
	i_hp_drain(i_hp_drain.class, false),
	i_m_attack(i_m_attack.class, false),
	i_my_summon_kill(i_my_summon_kill.class, false),
	i_p_attack(i_p_attack.class, false),
	i_p_hit(i_p_hit.class, false),
	i_refresh_instance(i_refresh_instance.class, false),
	i_set_skill(i_set_skill.class, false),
	i_soul_shot(i_soul_shot.class, false),
	i_spirit_shot(i_spirit_shot.class, false),
	i_spoil(i_spoil.class, false),
	i_summon_soul_shot(i_soul_shot.class, false),
	i_summon_spirit_shot(i_spirit_shot.class, false),
	i_fishing_shot(i_fishing_shot.class, false),
	p_block_buff_slot(p_block_buff_slot.class, false),
	p_block_chat(p_block_chat.class, true),
	p_block_debuff(p_block_debuff.class, true),
	p_block_move(p_block_move.class, true),
	p_block_party(p_block_party.class, true),
	p_max_cp(p_max_cp.class, false),
	p_max_hp(p_max_hp.class, false),
	p_max_mp(p_max_mp.class, false),
	p_passive(p_passive.class, true),
	p_violet_boy(p_violet_boy.class, true),
	t_hp(t_hp.class, false),
	i_get_exp(i_get_exp.class, true),
	i_get_sp(i_get_sp.class, true),
	i_target_me(i_target_me.class, true),
	i_reset_skill_reuse(i_reset_skill_reuse.class, false),
	BuffImmunity(EffectBuffImmunity.class, false),
	GreedOfMomot(EffectGreedOfMomot.class, true);

	private final Class<? extends Abnormal> _effectClass;
	private final Constructor<? extends Abnormal> _constructor;
	private final boolean _isRaidImmune;

	EffectType(Class<? extends Abnormal> clazz, boolean isRaidImmune)
	{
		_effectClass = clazz;
		try
		{
			_constructor = clazz.getConstructor(Creature.class, Creature.class, Skill.class, boolean.class, EffectTemplate.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new Error(e);
		}
		_isRaidImmune = isRaidImmune;
	}

	public Class<? extends Abnormal> getEffectClass()
	{
		return _effectClass;
	}

	public boolean isRaidImmune()
	{
		return _isRaidImmune;
	}

	public Abnormal makeEffect(Creature creature, Creature target, Skill skill, boolean reflected,
							   EffectTemplate template) throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		return _constructor.newInstance(creature, target, skill, reflected, template);
	}
}
