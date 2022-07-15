package l2s.gameserver.model.base

/**
 * @author VISTALL
 * @date 13:00/27.04.2011
 */
enum class RestartType {
    VILLAGE,
    AGIT,
    CASTLE,
    BATTLE_CAMP,
    FORTRESS,
    ORIGINAL,
    ADVENTURERS_SONG,
    TOWN; // TODO

    companion object {

        @JvmField
        val VALUES: Array<RestartType> = values()

        fun find(name: String): RestartType {
            return valueOf(name.toUpperCase())
        }

    }
}