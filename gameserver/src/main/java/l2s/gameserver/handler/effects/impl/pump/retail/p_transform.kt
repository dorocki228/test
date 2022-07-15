package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.util.Rnd
import l2s.gameserver.data.xml.holder.TransformTemplateHolder
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.base.Sex
import l2s.gameserver.templates.player.transform.TransformTemplate
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Transformation effect implementation.
 * @author nBd
 * @author Java-man
 */
class p_transform(template: EffectTemplate) : EffectHandler(template) {

    private val transformations: Map<Sex, List<TransformTemplate>>

    init {
        val param = params.getString("p_transform_param1")

        val temp = mutableMapOf<Sex, List<TransformTemplate>>()
        Sex.VALUES.forEach { sex: Sex ->
            val trans = param.split(";")
                    .map { it.toInt() }
                    .map { TransformTemplateHolder.getInstance().getTemplate(sex, it) }
                    .onEach {
                        requireNotNull(it) {
                            "Can't find transform ${it.id} for $sex"
                        }
                    }
            temp[sex] = trans
        }
        transformations = temp.toMap()

        require(transformations.isNotEmpty()) { "Must have transformations!" }
    }

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return !target.isDoor
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val transform = Rnd.get(transformations[target.sex])
        requireNotNull(transform)

        target.transform = transform
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.transform = null
    }

}