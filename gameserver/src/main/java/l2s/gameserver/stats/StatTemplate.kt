package l2s.gameserver.stats

import l2s.commons.lang.ArrayUtils
import l2s.gameserver.stats.funcs.Func
import l2s.gameserver.stats.funcs.FuncTemplate
import l2s.gameserver.stats.triggers.TriggerInfo
import java.util.*

/**
 * @author VISTALL
 * @date 23:05/22.01.2011
 */
open class StatTemplate {

    var attachedFuncs: Array<FuncTemplate> = FuncTemplate.EMPTY_ARRAY
        protected set
    protected var _triggerList: MutableList<TriggerInfo> = mutableListOf()

    val triggerList: List<TriggerInfo>
        get() = _triggerList

    fun addTrigger(f: TriggerInfo) {
        if (_triggerList.isEmpty())
            _triggerList = ArrayList(4)
        _triggerList.add(f)
    }

    open fun attachFunc(f: FuncTemplate) {
        attachedFuncs = ArrayUtils.add(attachedFuncs, f)
    }

    fun attachFuncs(vararg funcs: FuncTemplate) {
        for (f in funcs)
            attachFunc(f)
    }

    fun removeAttachedFuncs(): Array<FuncTemplate> {
        val funcs = attachedFuncs
        attachedFuncs = FuncTemplate.EMPTY_ARRAY
        return funcs
    }

    fun getStatFuncs(owner: Any): Array<Func> {
        if (attachedFuncs.isEmpty())
            return Func.EMPTY_FUNC_ARRAY

        return Array(attachedFuncs.size) {
            attachedFuncs[it].getFunc(owner)
        }
    }

}
