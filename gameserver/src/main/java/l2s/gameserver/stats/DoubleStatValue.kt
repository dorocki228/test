package l2s.gameserver.stats

/**
 * A state object representing the value of a double stat.
 *
 * @author NosKun
 */
class DoubleStatValue(doubleStat: DoubleStat) {

    init {
        reset(doubleStat)
    }

    var add = 0.0
    var mul = 0.0
    private var markedAdd = 0.0
    private var markedMul = 0.0

    var passiveAdd = 0.0
    var passiveMul = 0.0
    private var markedPassiveAdd = 0.0
    private var markedPassiveMul = 0.0

    /**
     * Resets this double stat value.
     * @param doubleStat the double stat
     */
    fun reset(doubleStat: DoubleStat) {
        add = doubleStat.resetAddValue
        mul = doubleStat.resetMulValue

        passiveAdd = doubleStat.resetAddValue
        passiveMul = doubleStat.resetMulValue
    }

    /**
     * Marks this double stat value so changes can be detected.
     */
    fun mark() {
        markedAdd = add
        markedMul = mul

        markedPassiveAdd = passiveAdd
        markedPassiveMul = passiveMul
    }

    /**
     * Checks if this double stat value has changed since it was marked.
     * @return `true` if this double stat value has changed since it was marked, `false` otherwise
     */
    fun hasChanged(): Boolean {
        return add != markedAdd || mul != markedMul || passiveAdd != markedPassiveAdd || passiveMul != markedPassiveMul
    }

}