package l2s.commons.util

/**
 * @author Java-man
 * @since 20.06.2019
 */
sealed class Chance {

    abstract fun roll(): Boolean

}

class NormalChance(private val chance: Double): Chance() {

    init {
        require(chance in 0.0..100.0) { "Chance should be in 0..100 range." }
    }

    override fun roll(): Boolean = Rnd.chance(chance)

}

object AlwaysSuccessfulChance: Chance() {

    override fun roll(): Boolean = true

}

object AlwaysUnsuccessfulChance: Chance() {

    override fun roll(): Boolean = false

}