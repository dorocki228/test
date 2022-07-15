package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.templates.skill.restoration.RestorationGroup
import l2s.gameserver.templates.skill.restoration.RestorationInfo
import l2s.gameserver.templates.skill.restoration.RestorationItem
import l2s.gameserver.utils.ItemFunctions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Restoration Random effect implementation.
 * @author Zoey76
 * @author Java-man
 */
class i_restoration_random(template: EffectTemplate?) : i_abstract_effect(template) {

    private val restorationInfo = RestorationInfo(0, 0, 0)

    init {
        val param = params.getString("i_restoration_random_param1").replace("\\s".toRegex(), "")
        regex.findAll(param)
                .map {
                    val id = it.groupValues[1].toInt()
                    val minCount = it.groupValues[2].toInt()
                    val maxCount = it.groupValues[3].toIntOrNull() ?: minCount
                    val item = RestorationItem(id, minCount, maxCount, 0)
                    RestorationGroup(it.groupValues[4].toDouble(), listOf(item))
                }
                .forEach {
                    restorationInfo.addRestorationGroup(it)
                }
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return

        val item = Rnd.get(restorationInfo.randomGroupItems) ?: return
        ItemFunctions.addItem(casterPlayer, item.id, item.randomCount.toLong())
    }

    companion object {
        private val regex = """\{\{\{(\d+);(\d+);?(\d+)?\}\};(\d+[,.]?\d+)\}""".toRegex()
    }

}