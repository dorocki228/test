package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.Config
import l2s.gameserver.data.xml.holder.CostumesHolder
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.s2c.ExCostumeUseItem
import l2s.gameserver.network.l2.s2c.ExSendCostumeList
import l2s.gameserver.templates.CostumeTemplate
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Bonux
 * @author Java-man
 */
class i_get_costume(template: EffectTemplate) : i_abstract_effect(template) {

    private val costumeTemplates: List<CostumeTemplate>

    init {
        val temp = params.getString("id", "")
                .split(";")
                .filter { it.isNotBlank() }
                .map { CostumesHolder.getInstance().getCostume(it.toInt()) }
                .onEach { requireNotNull(it) }

        val grades = params.getString("grade", "")
                .split(";")
                .filter { it.isNotBlank() }
                .map { CostumesHolder.getInstance().getCostumesByGrade(it.toInt()) }
                .flatten()
                .onEach { requireNotNull(it) }

        costumeTemplates = temp + grades
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (Config.EX_COSTUME_DISABLE) {
            return
        }

        val player = target.player ?: return

        val locationId = player.locationId
        val availableCostumes = costumeTemplates
                .filter { it.locationId == -1 || it.locationId == locationId }

        if (availableCostumes.isEmpty()) {
            player.sendPacket(ExCostumeUseItem(false, 0)) // TODO: Нужен ли он здесь?
            return
        }

        val costumeTemplate = Rnd.get(availableCostumes)

        player.costumeList.add(costumeTemplate)
        player.sendPacket(ExCostumeUseItem(true, costumeTemplate.id)) // TODO: Нужен ли он здесь?
        player.sendPacket(ExSendCostumeList(player))
    }

}