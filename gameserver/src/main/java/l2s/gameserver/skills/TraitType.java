package l2s.gameserver.skills;

/**
 * @author UnAfraid, NosBit
 */
public enum TraitType
{
	NONE(0),
	SWORD(1),
	BLUNT(1),
	DAGGER(1),
	POLE(1),
	FIST(1),
	BOW(1),
	ETC(1),
	UNK_8(0),
	POISON(3),
	HOLD(3),
	BLEED(3),
	SLEEP(3),
	SHOCK(3),
	DERANGEMENT(3),
	BUG_WEAKNESS(2),
	ANIMAL_WEAKNESS(2),
	PLANT_WEAKNESS(2),
	BEAST_WEAKNESS(2),
	DRAGON_WEAKNESS(2),
	PARALYZE(3),
	DUAL(1),
	DUALFIST(1),
	BOSS(3),
	GIANT_WEAKNESS(2),
	CONSTRUCT_WEAKNESS(2),
	DEATH(3),
	VALAKAS(2),
	ANESTHESIA(2),
	CRITICAL_POISON(3),
	ROOT_PHYSICALLY(3),
	ROOT_MAGICALLY(3),
	RAPIER(1),
	CROSSBOW(1),
	ANCIENTSWORD(1),
	TURN_STONE(3),
	GUST(3),
	PHYSICAL_BLOCKADE(3),
	TARGET(3),
	PHYSICAL_WEAKNESS(3),
	MAGICAL_WEAKNESS(3),
	DUALDAGGER(1),
	DEMONIC_WEAKNESS(2), // CT26_P4
	DIVINE_WEAKNESS(2),
	ELEMENTAL_WEAKNESS(2),
	FAIRY_WEAKNESS(2),
	HUMAN_WEAKNESS(2),
	HUMANOID_WEAKNESS(2),
	UNDEAD_WEAKNESS(2),
	// The values from below are custom.
	DUALBLUNT(1),
	KNOCKBACK(3),
	KNOCKDOWN(3),
	PULL(3),
	HATE(3),
	AGGRESSION(3),
	AIRBIND(3),
	DISARM(3),
	DEPORT(3),
	CHANGEBODY(3),
	TWOHANDCROSSBOW(1),
	ZONE(3),
	PSYCHIC(3),
	EMBRYO_WEAKNESS(2),
	SPIRIT_WEAKNESS(2);

	private final int type; // 1 = weapon, 2 = weakness, 3 = resistance

	TraitType(int type)
	{
		this.type = type;
	}

	public int getType()
	{
		return type;
	}

	public static TraitType find(String name) {
		return TraitType.valueOf(name.replace("trait_", "").toUpperCase());
	}
}
