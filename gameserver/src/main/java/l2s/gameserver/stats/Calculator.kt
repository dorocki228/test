package l2s.gameserver.stats

import l2s.commons.lang.ArrayUtils
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.stats.funcs.Func
import l2s.gameserver.stats.funcs.FuncOwner

/**
 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, HP_REGEN...).
 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR></BR><BR></BR>
 *
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR></BR><BR></BR>
 *
 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>.
 * Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order.
 * The result of the calculation is stored in the value property of an Env class instance.<BR></BR><BR></BR>
 *
 * Method addFunc and removeFunc permit to add and remove a Func object from a Calculator.<BR></BR><BR></BR>
 *
 */
class Calculator(val stat: DoubleStat, val owner: Creature) {

    /**
     * Для отладки
     */
    var functions: Array<Func> = Func.EMPTY_FUNC_ARRAY
        private set
    var passives: Array<Func> = Func.EMPTY_FUNC_ARRAY
        private set
    var limits: Array<Func> = Func.EMPTY_FUNC_ARRAY
        private set

    var base: Double = 0.toDouble()
        private set
    var last: Double = 0.toDouble()
        private set

    /**
     * Add a Func to the Calculator.<BR></BR><BR></BR>
     */
    fun addFunc(f: Func) {
        functions += f
        functions.sort()
    }

    fun addPassive(f: Func) {
        passives += f
        passives.sort()
    }

    /**
     * Remove a Func from the Calculator.<BR></BR><BR></BR>
     */
    fun removeFunc(f: Func) {
        functions = ArrayUtils.remove(functions, f)
        if (functions.isEmpty())
            functions = Func.EMPTY_FUNC_ARRAY
        else
            functions.sort()
    }

    fun removePassive(f: Func) {
        passives = ArrayUtils.remove(passives, f)
        if (passives.isEmpty())
            passives = Func.EMPTY_FUNC_ARRAY
        else
            passives.sort()
    }

    /**
     * Remove each Func with the specified owner of the Calculator.<BR></BR><BR></BR>
     */
    fun removeByOwner(owner: Any) {
        for (element in functions)
            if (element.owner == owner)
                removeFunc(element)

        for (element in passives)
            if (element.owner == owner)
                removePassive(element)
    }

    /**
     * Run each Func of the Calculator.<BR></BR><BR></BR>
     */
    fun calc(
        target: Creature?,
        skill: Skill?,
        init: Double,
        modifierType: StatModifierType?,
        changeBase: Boolean
    ): Double {
        if (changeBase)
            base = init

        var value = init
        for (func in functions) {
            val funcOwner = func.owner
            if (funcOwner is FuncOwner) {
                if (!funcOwner.isFuncEnabled)
                    continue
            }

            val condition = func.condition
            if (condition == null || condition.test(owner, target, skill, null, value)) {
                if (modifierType == func.modifierType)
                    value = func.calc(owner, target, skill, value)
            }
        }

        if (value != last) {
            val last = this.last //TODO [G1ta0] найти приминение в StatsChangeRecorder
            this.last = value
        }

        return value
    }

    fun calcPassives(
        target: Creature?,
        skill: Skill?,
        init: Double,
        modifierType: StatModifierType?,
        changeBase: Boolean
    ): Double {
        if (changeBase)
            base = init

        var value = init
        for (func in passives) {
            val funcOwner = func.owner
            if (funcOwner is FuncOwner) {
                if (!funcOwner.isFuncEnabled)
                    continue
            }

            val condition = func.condition
            if (condition == null || condition.test(owner, target, skill, null, value)) {
                if (modifierType == func.modifierType)
                    value = func.calc(owner, target, skill, value)
            }
        }

        if (value != last) {
            val last = this.last //TODO [G1ta0] найти приминение в StatsChangeRecorder
            this.last = value
        }

        return value
    }

    /**
     * Run each limit of the Calculator.<BR></BR><BR></BR>
     */
    fun calcLimit(
        target: Creature?,
        skill: Skill?,
        init: Double
    ): Double {
        var value = init
        for (func in limits) {
            val funcOwner = func.owner
            if (funcOwner is FuncOwner) {
                if (!funcOwner.isFuncEnabled)
                    continue
            }

            val condition = func.condition
            if (condition == null || condition.test(owner, target, skill, null, value)) {
                value = func.calc(owner, target, skill, value)
            }
        }

        if (value != last) {
            val last = this.last //TODO [G1ta0] найти приминение в StatsChangeRecorder
            this.last = value
        }

        return value
    }

}