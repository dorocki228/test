package l2s.gameserver.model.base

/**
 * Creature races.
 */
enum class NpcRace {
    NULL,
    UNDEAD,
    CONSTRUCT,
    BEAST,
    ANIMAL,
    PLANT,
    HUMANOID,
    ELEMENTAL,
    DIVINE,
    DEMONIC,
    DRAGON,
    GIANT,
    BUG,
    FAIRY,
    HUMAN,
    ELF,
    DARKELF,
    ORC,
    DWARF,
    ETC,
    NONE,
    SIEGE_WEAPON,
    CASTLE_GUARD,
    MERCENARY,
    ETC2,
    KAMAEL,
    ERTHEIA;

    companion object {
        fun find(name: String): NpcRace {
            return valueOf(name.replace("race_", "").toUpperCase())
        }
    }
}