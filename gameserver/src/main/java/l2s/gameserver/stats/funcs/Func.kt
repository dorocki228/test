package l2s.gameserver.stats.funcs

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.stats.conditions.Condition

/**
 * A Func object is a component of a Calculator created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, HP_REGEN...).
 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR></BR><BR></BR>
 *
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR></BR><BR></BR>
 *
 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>.
 * Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order.
 * The result of the calculation is stored in the value property of an Env class instance.<BR></BR><BR></BR>
 *
 */
abstract class Func @JvmOverloads constructor(
    /** Statistics, that is affected by this function (See L2Character.CALCULATOR_XXX constants)  */
    val stat: DoubleStat,
    /**
     * Order of functions calculation.
     * Functions with lower order are executed first.
     * Functions with the same order are executed in unspecified order.
     * Usually add/substruct functions has lowest order,
     * then bonus/penalty functions (multiplay/divide) are
     * applied, then functions that do more complex calculations
     * (non-linear functions).
     */
    val order: Int,
    /**
     * Owner can be an armor, weapon, skill, system event, quest, etc
     * Used to remove all functions added by this owner.
     */
    val owner: Any?,
    val value: Double = 0.0
) : Comparable<Func> {

    /**
     * Для отладки
     */
    var condition: Condition? = null

    open val modifierType: StatModifierType?
         get() = null

    // TODO remove hardcode
    val passive: Boolean =
        (owner is Skill && owner.isPassive
                && owner.id !in 45001..45016 && owner.id !in 54034..54035
                && owner.id !in 55253..55268)

    abstract fun calc(actor: Creature, target: Creature?, skill: Skill?, value: Double): Double

    override fun compareTo(other: Func): Int {
        return order.compareTo(other.order)
    }

    companion object {
        val EMPTY_FUNC_ARRAY = emptyArray<Func>()
    }

}