package l2s.gameserver.handler.effects;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.handler.effects.impl.consume.*;
import l2s.gameserver.handler.effects.impl.instant.*;
import l2s.gameserver.handler.effects.impl.instant.retail.*;
import l2s.gameserver.handler.effects.impl.pump.*;
import l2s.gameserver.handler.effects.impl.pump.retail.*;
import l2s.gameserver.handler.effects.impl.tick.*;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonux
 **/
public class EffectHandlerHolder extends AbstractHolder {
    private static final EffectHandlerHolder _instance = new EffectHandlerHolder();

    public static EffectHandlerHolder getInstance() {
        return _instance;
    }

    private Map<String, Constructor<? extends EffectHandler>> _handlerConstructors = new HashMap<String, Constructor<? extends EffectHandler>>();

    private EffectHandlerHolder() {
        // Old Effect
        // TODO: Remake to offlike effects
		/*registerHandler(EffectAddSkills.class);
		registerHandler(EffectAgathionResurrect.class);
		registerHandler(EffectBetray.class);
		registerHandler(EffectBuff.class);
		registerHandler(EffectDamageBlock.class);
		registerHandler(EffectDistortedSpace.class);
		registerHandler(EffectCharge.class);
		registerHandler(EffectCharmOfCourage.class);
		registerHandler(EffectCPDamPercent.class);
		registerHandler(EffectDamageHealToEffector.class);
		registerHandler(EffectDestroySummon.class);
		registerHandler(EffectDeathImmunity.class);
		registerHandler(EffectDisarm.class);
		registerHandler(EffectDiscord.class);
		registerHandler(EffectDispelOnHit.class);
		registerHandler(EffectEffectImmunity.class);
		registerHandler(EffectEnervation.class);
		registerHandler(EffectFakeDeath.class);
		registerHandler(EffectFear.class);
		registerHandler(EffectMoveToEffector.class);
		registerHandler(EffectGrow.class);
		registerHandler(EffectHate.class);
		registerHandler(EffectHealBlock.class);
		registerHandler(EffectHPDamPercent.class);
		registerHandler(EffectHpToOne.class);
		registerHandler(EffectIgnoreSkill.class);
		registerHandler(EffectInterrupt.class);
		registerHandler(EffectInvulnerable.class);
		registerHandler(EffectInvisible.class);
		registerHandler(EffectLockInventory.class);
		registerHandler(EffectCurseOfLifeFlow.class);
		registerHandler(EffectLaksis.class);
		registerHandler(EffectLDManaDamOverTime.class);
		registerHandler(EffectManaDamOverTime.class);
		registerHandler(EffectMeditation.class);
		registerHandler(EffectMPDamPercent.class);
		registerHandler(EffectMute.class);
		registerHandler(EffectMuteAll.class);
		registerHandler(EffectMutation.class);
		registerHandler(EffectMuteAttack.class);
		registerHandler(EffectMutePhisycal.class);
		registerHandler(EffectParalyze.class);
		registerHandler(EffectPetrification.class);
		registerHandler(EffectRelax.class);
		registerHandler(EffectSalvation.class);
		registerHandler(EffectSilentMove.class);
		registerHandler(EffectSleep.class);
		registerHandler(EffectStun.class);
		registerHandler(EffectKnockDown.class);
		registerHandler(EffectKnockBack.class);
		registerHandler(EffectFlyUp.class);
		registerHandler(EffectThrowHorizontal.class);
		registerHandler(EffectThrowUp.class);
		registerHandler(EffectTransformation.class);
		registerHandler(EffectVisualTransformation.class);
		registerHandler(EffectVitality.class);
		registerHandler(EffectShadowStep.class);

		registerHandler(EffectRestoreCP.class);
		//registerHandler(EffectRestoreHP.class);
		registerHandler(EffectRestoreMP.class);

		registerHandler(EffectCPDrain.class);
		registerHandler(EffectHPDrain.class);
		registerHandler(EffectMPDrain.class);

		registerHandler(EffectAbsorbDamageToEffector.class); // абсорбирует часть дамага к еффектора еффекта
		registerHandler(EffectAbsorbDamageToMp.class); // абсорбирует часть дамага в мп
		registerHandler(EffectAbsorbDamageToSummon.class); // абсорбирует часть дамага к сумону

		registerHandler(EffectArmorBreaker.class);*/

        // Offlike Effects

        // Consume Effects
        registerHandler(c_chameleon_rest.class);
        registerHandler(c_fake_death.class);
        registerHandler(c_hp.class);
        registerHandler(c_mp.class);
        registerHandler(c_mp_by_level.class);
        registerHandler(c_rest.class);

        // Instant Effects
        registerHandler(cub_heal.class);
        registerHandler(cub_hp_drain.class);
        registerHandler(cub_m_attack.class);
        registerHandler(i_add_hate.class);
        registerHandler(i_align_direction.class);
        registerHandler(i_backstab.class);
        registerHandler(i_betray.class);
        registerHandler(i_call_party.class);
        registerHandler(i_call_pc.class);
        registerHandler(i_call_skill.class);
        // TODO registerHandler(i_change_face.class);
        // TODO registerHandler(i_change_hair_color.class);
        // TODO registerHandler(i_change_hair_style.class);
        // TODO registerHandler(i_change_skill_level.class);
        registerHandler(i_confuse.class);
        registerHandler(i_consume_body.class);
        registerHandler(i_cp.class);
        registerHandler(i_cp_per_max.class);
        registerHandler(i_death.class);
        registerHandler(i_death_link.class);
        registerHandler(i_delete_hate.class);
        registerHandler(i_delete_hate_of_me.class);
        // TODO registerHandler(i_detect_object.class);
        registerHandler(i_dispel_all.class);
        registerHandler(i_dispel_by_category.class);
        registerHandler(i_dispel_by_slot.class);
        registerHandler(i_dispel_by_slot_myself.class);
        registerHandler(i_dispel_by_slot_probability.class);
        registerHandler(i_distrust.class);
        // TODO registerHandler(i_enchant_armor.class);
        // TODO registerHandler(i_enchant_armor_rate.class);
        // TODO registerHandler(i_enchant_attribute.class);
        // TODO registerHandler(i_enchant_item_multi.class);
        // TODO registerHandler(i_enchant_weapon.class);
        // TODO registerHandler(i_enchant_weapon_rate.class);
        registerHandler(i_energy_attack.class);
        registerHandler(i_escape.class);
        registerHandler(i_fatal_blow.class);
        registerHandler(i_focus_energy.class);
        registerHandler(i_focus_max_energy.class);
        // TODO registerHandler(i_food_for_pet.class);
        registerHandler(i_get_agro.class);
        registerHandler(i_get_costume.class);
        registerHandler(i_get_exp.class);
        registerHandler(i_heal.class);
        registerHandler(i_heal_link.class);
        registerHandler(i_heal_special.class);
        // TODO registerHandler(i_holything_possess.class);
        registerHandler(i_hp.class);
        registerHandler(i_hp_by_level_self.class);
        registerHandler(i_hp_drain.class);
        registerHandler(i_hp_per_max.class);
        registerHandler(i_hp_self.class);
        // TODO registerHandler(i_install_camp.class);
        // TODO registerHandler(i_install_camp_ex.class);
        registerHandler(i_knockback.class);
        registerHandler(i_m_attack.class);
        registerHandler(i_m_attack_by_abnormal.class);
        registerHandler(i_m_attack_by_abnormal_slot.class);
        registerHandler(i_m_attack_by_dist.class);
        registerHandler(i_m_attack_mp.class);
        registerHandler(i_m_attack_over_hit.class);
        registerHandler(i_m_attack_range.class);
        registerHandler(i_mp.class);
        registerHandler(i_mp_by_level.class);
        registerHandler(i_mp_by_level_self.class);
        registerHandler(i_mp_per_max.class);
        // TODO registerHandler(i_npc_kill.class);
        // TODO registerHandler(i_open_common_recipebook.class);
        registerHandler(i_open_dwarf_recipebook.class);
        registerHandler(i_p_attack.class);
        registerHandler(i_p_attack_over_hit.class);
        registerHandler(i_physical_attack_hp_link.class);
        // TODO registerHandler(i_pk_count.class);
        registerHandler(i_pledge_reputation.class);
        // TODO registerHandler(i_pledge_send_system_message.class);
        registerHandler(i_pull.class);
        registerHandler(i_randomize_hate.class);
        registerHandler(i_real_damage.class);
        registerHandler(i_rebalance_hp.class);
        registerHandler(i_restoration.class);
        registerHandler(i_restoration_random.class);
        registerHandler(i_resurrection.class);
        registerHandler(i_set_skill.class);
        registerHandler(i_skill_turning.class);
        registerHandler(i_sp.class);
        registerHandler(i_spoil.class);
        registerHandler(i_summon.class);
        registerHandler(i_summon_agathion.class);
        registerHandler(i_summon_cubic.class);
        // TODO other summon effects
        registerHandler(i_sweeper.class);
        registerHandler(i_target_cancel.class);
        registerHandler(i_target_me.class);
        registerHandler(i_teleport_to_target.class);
        // TODO teleport effects
        // TODO registerHandler(i_transfer_hate.class);
        registerHandler(i_unlock.class);
        registerHandler(i_unsummon_agathion.class);
        // old
        registerHandler(i_call_random_skill.class);
        registerHandler(i_fishing_shot.class);
        registerHandler(i_get_aggro_of_monster.class);
        registerHandler(i_my_summon_kill.class);
        registerHandler(i_refresh_instance.class);
        registerHandler(i_reset_skill_reuse.class);
        registerHandler(i_soul_shot.class);
        registerHandler(i_spirit_shot.class);
        registerHandler(i_stop_invis.class);
        registerHandler(i_summon_soul_shot.class);
        registerHandler(i_summon_spirit_shot.class);

        // pump effects
        registerHandler(cub_attack_speed.class);
        registerHandler(cub_block_act.class);
        registerHandler(cub_block_move.class);
        registerHandler(cub_physical_attack.class);
        registerHandler(cub_physical_defence.class);
        registerHandler(p_2h_blunt_bonus.class);
        registerHandler(p_2h_sword_bonus.class);
        registerHandler(p_ability_change.class);
        registerHandler(p_abnormal_shield.class);
        registerHandler(p_add_skill.class);
        registerHandler(p_area_damage.class);
        registerHandler(p_attack_attribute.class);
        registerHandler(p_attack_attribute_add.class);
        registerHandler(p_attack_behind.class);
        registerHandler(p_attack_damage_position.class);
        registerHandler(p_attack_range.class);
        registerHandler(p_attack_speed.class);
        registerHandler(p_attack_speed_by_hp1.class);
        registerHandler(p_attack_speed_by_hp2.class);
        registerHandler(p_attack_speed_by_weapon.class);
        registerHandler(p_attack_trait.class);
        registerHandler(p_avoid.class);
        registerHandler(p_avoid_agro.class);
        registerHandler(p_avoid_by_move_mode.class);
        registerHandler(p_avoid_rate_by_hp1.class);
        registerHandler(p_avoid_rate_by_hp2.class);
        registerHandler(p_avoid_skill.class);
        registerHandler(p_betray.class);
        registerHandler(p_block_act.class);
        registerHandler(p_block_attack.class);
        registerHandler(p_block_buff.class);
        registerHandler(p_block_buff_slot.class);
        registerHandler(p_block_chat.class);
        registerHandler(p_block_controll.class);
        registerHandler(p_block_debuff.class);
        registerHandler(p_block_escape.class);
        registerHandler(p_block_getdamage.class);
        registerHandler(p_block_move.class);
        registerHandler(p_block_resurrection.class);
        // TODO registerHandler(p_block_skill.class);
        registerHandler(p_block_skill_physical.class);
        registerHandler(p_block_skill_special.class);
        registerHandler(p_block_spell.class);
        registerHandler(p_breath.class);
        registerHandler(p_call_skill.class);
        registerHandler(p_channel_clan.class);
        registerHandler(p_cheapshot.class);
        registerHandler(p_condition_block_act_item.class);
        registerHandler(p_condition_block_act_skill.class);
        registerHandler(p_counter_skill.class);
        registerHandler(p_cp_regen.class);
        // TODO registerHandler(p_crafting_critical.class);
        // TODO registerHandler(p_create_common_item.class);
        registerHandler(p_create_item.class);
        registerHandler(p_critical_damage.class);
        registerHandler(p_critical_damage_position.class);
        registerHandler(p_critical_rate.class);
        registerHandler(p_critical_rate_by_hp1.class);
        registerHandler(p_critical_rate_by_hp2.class);
        registerHandler(p_critical_rate_position_bonus.class);
        registerHandler(p_crystal_grade_modify.class);
        registerHandler(p_crystallize.class);
        registerHandler(p_cubic_mastery.class);
        registerHandler(p_damage_by_attack.class);
        registerHandler(p_damage_shield.class);
        registerHandler(p_damage_shield_resist.class);
        registerHandler(p_defence_attribute.class);
        registerHandler(p_defence_critical_damage.class);
        registerHandler(p_defence_critical_rate.class);
        registerHandler(p_defence_trait.class);
        registerHandler(p_disappear_target.class);
        registerHandler(p_disarm.class);
        // TODO registerHandler(p_disarmor.class);
        registerHandler(p_droprate_modify.class);
        registerHandler(p_enlarge_abnormal_slot.class);
        registerHandler(p_enlarge_storage.class);
        registerHandler(p_exp_modify.class);
        registerHandler(p_expand_deco_slot.class);
        registerHandler(p_expand_jewel_slot.class);
        registerHandler(p_enable_primary_agathion_slot.class);
        registerHandler(p_expand_secondary_agathion_slot.class);
        registerHandler(p_fatal_blow_rate.class);
        registerHandler(p_fear.class);
        registerHandler(p_focus_energy.class);
        registerHandler(p_get_damage_limit.class);
        registerHandler(p_hate_attack.class);
        registerHandler(p_heal_effect.class);
        registerHandler(p_hide.class);
        registerHandler(p_hit.class);
        registerHandler(p_hit_at_night.class);
        registerHandler(p_hit_number.class);
        registerHandler(p_hp_regen.class);
        registerHandler(p_hp_regen_by_move_mode.class);
        registerHandler(p_ignore_death.class);
        // TODO registerHandler(p_ignore_skill.class);
        registerHandler(p_instant_kill_resist.class);
        registerHandler(p_limit_cp.class);
        registerHandler(p_limit_hp.class);
        registerHandler(p_limit_mp.class);
        // TODO registerHandler(p_luck.class);
        registerHandler(p_magic_abnormal_resist.class);
        registerHandler(p_magic_avoid.class);
        registerHandler(p_magic_critical_dmg.class);
        registerHandler(p_magic_critical_rate.class);
        registerHandler(p_magic_defence_critical_dmg.class);
        registerHandler(p_magic_defence_critical_rate.class);
        registerHandler(p_magic_hit.class);
        registerHandler(p_magic_mp_cost.class);
        registerHandler(p_magic_speed.class);
        registerHandler(p_magic_speed_by_weapon.class);
        registerHandler(p_magical_attack.class);
        registerHandler(p_magical_attack_add.class);
        registerHandler(p_magical_defence.class);
        registerHandler(p_mana_charge.class);
        registerHandler(p_max_cp.class);
        registerHandler(p_max_hp.class);
        registerHandler(p_max_mp.class);
        registerHandler(p_max_mp_add.class);
        registerHandler(p_mp_regen.class);
        registerHandler(p_mp_regen_add.class);
        registerHandler(p_mp_regen_by_move_mode.class);
        registerHandler(p_mp_shield.class);
        registerHandler(p_mp_vampiric_attack.class);
        registerHandler(p_passive.class);
        registerHandler(p_physical_abnormal_resist.class);
        registerHandler(p_physical_attack.class);
        registerHandler(p_physical_attack_by_hp1.class);
        registerHandler(p_physical_attack_by_hp2.class);
        registerHandler(p_physical_defence.class);
        registerHandler(p_physical_defence_by_hp1.class);
        registerHandler(p_physical_defence_by_hp2.class);
        registerHandler(p_physical_polarm_target_single.class);
        registerHandler(p_physical_shield_defence.class);
        registerHandler(p_physical_shield_defence_angle_all.class);
        registerHandler(p_pk_protect.class);
        registerHandler(p_preserve_abnormal.class);
        // TODO registerHandler(p_protect_death_penalty.class);
        registerHandler(p_pve_magical_skill_defence_bonus.class);
        registerHandler(p_pve_magical_skill_dmg_bonus.class);
        registerHandler(p_pve_physical_attack_defence_bonus.class);
        registerHandler(p_pve_physical_attack_dmg_bonus.class);
        registerHandler(p_pve_physical_skill_defence_bonus.class);
        registerHandler(p_pve_physical_skill_dmg_bonus.class);
        registerHandler(p_pvp_magical_skill_defence_bonus.class);
        registerHandler(p_pvp_magical_skill_dmg_bonus.class);
        registerHandler(p_pvp_physical_attack_defence_bonus.class);
        registerHandler(p_pvp_physical_attack_dmg_bonus.class);
        registerHandler(p_pvp_physical_skill_defence_bonus.class);
        registerHandler(p_pvp_physical_skill_dmg_bonus.class);
        registerHandler(p_reduce_cancel.class);
        registerHandler(p_reduce_drop_penalty.class);
        registerHandler(p_reflect_dd.class);
        registerHandler(p_reflect_skill.class);
        registerHandler(p_remove_equip_penalty.class);
        registerHandler(p_resist_abnormal_by_category.class);
        registerHandler(p_resist_dd_magic.class);
        registerHandler(p_resist_dispel_by_category.class);
        registerHandler(p_resurrection_special.class);
        registerHandler(p_reuse_delay.class);
        registerHandler(p_safe_fall_height.class);
        registerHandler(p_shield_defence_rate.class);
        registerHandler(p_skill_critical_damage.class);
        registerHandler(p_skill_critical_rate.class);
        registerHandler(p_skill_power.class);
        registerHandler(p_soulshot_power.class);
        registerHandler(p_sp_modify.class);
        registerHandler(p_speed.class);
        registerHandler(p_speed_out_of_fight.class);
        registerHandler(p_spell_power.class);
        registerHandler(p_spheric_barrier.class);
        registerHandler(p_spiritshot_power.class);
        // TODO registerHandler(p_statbonus_speed.class);
        // TODO registerHandler(p_statbonus_skillcritical.class);
        registerHandler(p_stat_up.class);
        registerHandler(p_stat_up_at_night.class);
        registerHandler(p_target_me.class);
        registerHandler(p_transfer_damage_pc.class);
        registerHandler(p_transfer_damage_summon.class);
        registerHandler(p_transform.class);
        registerHandler(p_trigger_damage_by_attack.class);
        registerHandler(p_trigger_skill_by_attack.class);
        registerHandler(p_trigger_skill_by_avoid.class);
        registerHandler(p_trigger_skill_by_dmg.class);
        registerHandler(p_trigger_skill_by_magic_type.class);
        registerHandler(p_trigger_skill_by_skill.class);
        registerHandler(p_vampiric_attack.class);
        registerHandler(p_vampiric_defence.class);
        registerHandler(p_weight_limit.class);
        registerHandler(p_weight_penalty.class);
        registerHandler(p_world_chat_point.class);
        registerHandler(p_wrong_casting.class);
        // old
        registerHandler(p_block_buff_slot.class);
        registerHandler(p_block_escape.class);
        registerHandler(p_block_party.class);
        registerHandler(p_block_target.class);
        registerHandler(p_block_target_me.class);
        registerHandler(p_disable_invis.class);
        registerHandler(p_get_item_by_exp.class);
        registerHandler(p_raid_berserk.class);
        registerHandler(p_run_speed.class);
        registerHandler(p_violet_boy.class);
        // permanent elemental effects
        registerHandler(p_fire_elemental_attack.class);
        registerHandler(p_water_elemental_attack.class);
        registerHandler(p_wind_elemental_attack.class);
        registerHandler(p_earth_elemental_attack.class);
        registerHandler(p_fire_elemental_defence.class);
        registerHandler(p_water_elemental_defence.class);
        registerHandler(p_wind_elemental_defence.class);
        registerHandler(p_earth_elemental_defence.class);
        // custom effects
        registerHandler(p_autoloot_custom.class);

        // Tick Effects
        registerHandler(cub_hp.class);
        registerHandler(t_get_energy.class);
        registerHandler(t_hp.class);
        registerHandler(t_hp_fatal.class);
        registerHandler(t_hp_magic.class);
        registerHandler(t_mp.class);
    }

    public void registerHandler(Class<? extends EffectHandler> handlerClass) {
        String name = EffectHandler.getName(handlerClass);
        if (_handlerConstructors.containsKey(name)) {
            warn("EffectHandlerHolder: Dublicate handler registered! Handler: CLASS[" + handlerClass.getSimpleName() + "], NAME[" + name + "]");
            return;
        }

        try {
            _handlerConstructors.put(name, handlerClass.getConstructor(new Class<?>[]{EffectTemplate.class}));
        } catch (Exception e) {
            error("EffectHandlerHolder: Error while loading handler: " + e, e);
        }
    }

    public EffectHandler makeHandler(String handlerName, EffectTemplate template) {
        if (StringUtils.isEmpty(handlerName))
            return new EffectHandler(template);

        Constructor<? extends EffectHandler> constructor = _handlerConstructors.get(handlerName.toLowerCase());
        if (constructor == null) {
            warn("EffectHandlerHolder: Not found handler: " + handlerName + " for skill " + template.getSkill());
            return new EffectHandler(template);
        }

        try {
            return constructor.newInstance(template);
        } catch (Exception e) {
            error("EffectHandlerHolder: Error while making handler: " + handlerName + " for skill " + template.getSkill(), e);
            return new EffectHandler(template);
        }
    }

    @Override
    public int size() {
        return _handlerConstructors.size();
    }

    @Override
    public void clear() {
        _handlerConstructors.clear();
    }
}
