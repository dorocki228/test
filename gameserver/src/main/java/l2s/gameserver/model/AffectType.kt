package l2s.gameserver.model

enum class AffectType {
    SELF,
    ALL;

    companion object {
        fun find(name: String) : AffectType {
            return when (name) {
                "all" -> ALL
                "self" -> SELF
                else -> error("Can't find affect type value: $name")
            }
        }
    }

}