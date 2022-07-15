package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.data.xml.holder.AgathionHolder
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Agathion
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.cubic.AgathionTemplate
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Summon Agathion effect implementation.
 * @author Zoey76
 * @author Bonux
 * @author Java-man
 */
class i_summon_agathion(template: EffectTemplate) : i_abstract_effect(template) {

    private val _template: AgathionTemplate

    init {
        val id = params.getInteger("i_summon_agathion_param1")
        val unk = params.getInteger("i_summon_agathion_param2")
        val temp = AgathionHolder.getInstance().getTemplate(id)
        _template = requireNotNull(temp)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val player = caster.player ?: return

        if (player.agathion != null) {
            player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
            return;
        }

        val agathion = Agathion(player, _template, skill)
        agathion.init()
    }

}