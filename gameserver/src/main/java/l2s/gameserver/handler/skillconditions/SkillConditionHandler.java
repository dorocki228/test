package l2s.gameserver.handler.skillconditions;

import l2s.gameserver.handler.skillconditions.impl.*;
import l2s.gameserver.templates.StatsSet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author NosBit
 */
public final class SkillConditionHandler
{
	private final Map<String, Function<StatsSet, SkillCondition>> skillConditionHandlerFactories = new HashMap<>();
	
	public void registerHandler(String name, Function<StatsSet, SkillCondition> handlerFactory)
	{
		skillConditionHandlerFactories.put(name, handlerFactory);
	}
	
	public Function<StatsSet, SkillCondition> getHandlerFactory(String name)
	{
		return skillConditionHandlerFactories.get(name);
	}
	
	public int size()
	{
		return skillConditionHandlerFactories.size();
	}
	
	protected SkillConditionHandler()
	{
		registerHandler("build_advance_base", build_advance_base::new);
		registerHandler("build_camp", build_camp::new);
		registerHandler("can_bookmark_add_slot", can_bookmark_add_slot::new);
		registerHandler("can_get_costume", can_get_costume::new);
		registerHandler("can_summon", can_summon::new);
		registerHandler("can_summon_cubic", can_summon_cubic::new);
		registerHandler("can_summon_pet", can_summon_pet::new);
		registerHandler("can_summon_siege_golem", can_summon_siege_golem::new);
		registerHandler("can_transform", can_transform::new);
		registerHandler("can_untransform", can_untransform::new);
		registerHandler("can_use_in_battlefield", can_use_in_battlefield::new);
		registerHandler("can_use_swoop_cannon", can_use_swoop_cannon::new);
		registerHandler("cannot_use_in_transform", cannot_use_in_transform::new);
		registerHandler("check_level", check_level::new);
		registerHandler("check_sex", check_sex::new);
		registerHandler("consume_body", consume_body::new);
		registerHandler("energy_saved", energy_saved::new);
		registerHandler("equip_armor", equip_armor::new);
		registerHandler("equip_shield", equip_shield::new);
		registerHandler("equip_weapon", equip_weapon::new);
		registerHandler("not_in_underwater", not_in_underwater::new);
		registerHandler("op_2h_weapon", op_2h_weapon::new);
		registerHandler("op_agathion_energy", op_agathion_energy::new);
		registerHandler("op_alignment", op_alignment::new);
		registerHandler("op_blink", op_blink::new);
		registerHandler("op_call_pc", op_call_pc::new);
		registerHandler("op_can_escape", op_can_escape::new);
		registerHandler("op_cannot_use_target_with_private_store", op_cannot_use_target_with_private_store::new);
		registerHandler("op_check_abnormal", op_check_abnormal::new);
		registerHandler("op_check_cast_range", op_check_cast_range::new);
		registerHandler("op_check_class", op_check_class::new);
		registerHandler("op_check_class_list", op_check_class_list::new);
		registerHandler("op_check_crt_effect", op_check_crt_effect::new);
		registerHandler("op_check_residence", op_check_residence::new);
		registerHandler("op_check_skill", op_check_skill::new);
		registerHandler("op_companion", op_companion::new);
		registerHandler("op_enchant_range", op_enchant_range::new);
		registerHandler("op_encumbered", op_encumbered::new);
		registerHandler("op_energy_max", op_energy_max::new);
		registerHandler("op_equip_item", op_equip_item::new);
		registerHandler("op_fishing_cast", op_fishing_cast::new);
		registerHandler("op_fishing_pumping", op_fishing_pumping::new);
		registerHandler("op_fishing_reeling", op_fishing_reeling::new);
		registerHandler("op_have_summon", op_have_summon::new);
		registerHandler("op_have_summoned_npc", op_have_summoned_npc::new);
		registerHandler("op_home", op_home::new);
		registerHandler("op_in_siege_time", op_in_siege_time::new);
		registerHandler("op_instantzone", op_instantzone::new);
		registerHandler("op_mainjob", op_mainjob::new);
		registerHandler("op_need_agathion", op_need_agathion::new);
		registerHandler("op_need_summon_or_pet", op_need_summon_or_pet::new);
		registerHandler("op_not_cursed", op_not_cursed::new);
		registerHandler("op_not_instantzone", op_not_instantzone::new);
		registerHandler("op_not_olympiad", op_not_olympiad::new);
		registerHandler("op_not_territory", op_not_territory::new);
		registerHandler("op_peacezone", op_peacezone::new);
		registerHandler("op_pkcount", op_pkcount::new);
		registerHandler("op_pledge", op_pledge::new);
		registerHandler("op_restart_point", op_restart_point::new);
		registerHandler("op_resurrection", op_resurrection::new);
		registerHandler("op_siege_hammer", op_siege_hammer::new);
		registerHandler("op_skill", op_skill::new);
		registerHandler("op_skill_acquire", op_skill_acquire::new);
		registerHandler("op_social_class", op_social_class::new);
		registerHandler("op_subjob", op_subjob::new);
		registerHandler("op_sweeper", op_sweeper::new);
		registerHandler("op_target_all_item_type", op_target_all_item_type::new);
		registerHandler("op_target_armor_type", op_target_armor_type::new);
		registerHandler("op_target_my_pledge_academy", op_target_my_pledge_academy::new);
		registerHandler("op_target_npc", op_target_npc::new);
		registerHandler("op_target_pc", op_target_pc::new);
		registerHandler("op_target_weapon_attack_type", op_target_weapon_attack_type::new);
		registerHandler("op_territory", op_territory::new);
		registerHandler("op_unlock", op_unlock::new);
		registerHandler("op_use_firecracker", op_use_firecracker::new);
		registerHandler("op_use_praseed", op_use_praseed::new);
		registerHandler("op_wyvern", op_wyvern::new);
		registerHandler("possess_holything", possess_holything::new);
		registerHandler("remain_cp_per", remain_cp_per::new);
		registerHandler("remain_hp_per", remain_hp_per::new);
		registerHandler("remain_mp_per", remain_mp_per::new);
		registerHandler("target_item_crystal_type", target_item_crystal_type::new);
		registerHandler("target_my_mentee", target_my_mentee::new);
		registerHandler("target_my_party", target_my_party::new);
		registerHandler("target_my_pledge", target_my_pledge::new);
		registerHandler("target_race", target_race::new);
	}

	public static SkillConditionHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final class SingletonHolder
	{
		protected static final SkillConditionHandler _instance = new SkillConditionHandler();
	}
}
