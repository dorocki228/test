package l2s.gameserver.model.base

/**
 * @author Sdw
 */
enum class DispelSlotType {
    SLOT_ALL,
    SLOT_BUFF,
    SLOT_DEBUFF,
    MULTI_BUFF;

    companion object {
        fun find(name: String): DispelSlotType {
            return valueOf(name.toUpperCase())
        }
    }

}