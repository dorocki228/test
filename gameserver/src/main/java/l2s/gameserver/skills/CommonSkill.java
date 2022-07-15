package l2s.gameserver.skills;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;

/**
 * An Enum to hold some important references to commonly used skills
 * @author DrHouse
 */
public enum CommonSkill
{
	RAID_CURSE(4215, 1),
	RAID_CURSE2(4515, 1),
	SEAL_OF_RULER(246, 1),
	BUILD_HEADQUARTERS(247, 1),
	BUILD_ADVANCED_HEADQUARTERS(326, 1),
	OUTPOST_CONSTRUCTION(844, 1),
	OUTPOST_DEMOLITION(845, 1),
	WYVERN_BREATH(4289, 1),
	STRIDER_SIEGE_ASSAULT(325, 1),
	FIREWORK(5965, 1),
	LARGE_FIREWORK(2025, 1),
	BATTLEFIELD_DEATH_SYNDROME(5660, 1),
	VOID_BURST(3630, 1),
	VOID_FLOW(3631, 1),
	THE_VICTOR_OF_WAR(5074, 1),
	THE_VANQUISHED_OF_WAR(5075, 1),
	SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
	WEAPON_GRADE_PENALTY(6209, 1),
	ARMOR_GRADE_PENALTY(6213, 1),
	EXPERTISE(239, 1),
	ONYX_BEAST_TRANSFORMATION(617, 1),
	DIVINE_INSPIRATION(1405, 1),
	CARAVANS_SECRET_MEDICINE(2341, 1),
	SHILENS_BREATH(14571, 1),
	IMPRIT_OF_LIGHT(19034, 1),
	IMPRIT_OF_DARKNESS(19035, 1),
	ABILITY_OF_LIGHT(19032, 1),
	ABILITY_OF_DARKNESS(19033, 1),
	ALCHEMY_CUBE(17943, 1),
	ALCHEMY_CUBE_RANDOM_SUCCESS(17966, 1),
	PET_SWITCH_STANCE(6054, 1),
	WEIGHT_PENALTY(4270, 1),
	FROG_TRANSFORM(6201, 1),
	CHILD_TRANSFORM(6202, 1),
	NATIVE_TRANSFORM(6203, 1),
	LUCKY_CLOVER(18103, 1),
	DISPLAY_CLAN_GATE(5109, 1);
	
	private final int skillId;
	private final int skillLevel;

	CommonSkill(int id, int level)
	{
		this.skillId = id;
		this.skillLevel = level;
	}
	
	public int getId()
	{
		return skillId;
	}
	
	public int getLevel()
	{
		return skillLevel;
	}
	
	public Skill getSkill()
	{
		return SkillHolder.getInstance().getSkill(skillId, skillLevel);
	}

	public SkillEntry getSkillEntry(SkillEntryType entryType)
	{
		return SkillEntry.makeSkillEntry(entryType, skillId, skillLevel);
	}
}
