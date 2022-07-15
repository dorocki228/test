package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.data.xml.holder.NpcHolder
import l2s.gameserver.geometry.Location
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.idfactory.IdFactory
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Servitor
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.Experience
import l2s.gameserver.model.instances.SummonInstance
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.templates.item.data.ItemData
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

/**
 * Summon effect implementation.
 *
 * @author UnAfraid
 * @author Java-man
 */
class i_summon(template: EffectTemplate) : i_abstract_effect(template) {

    private val summonTemplate: NpcTemplate
    private val _expMultiplier: Double = params.getDouble("i_summon_param2")
    private val _consumeItem = ItemData(
            params.getInteger("i_summon_param3"),
            params.getLong("i_summon_param4")
    )
    private val _lifeTime: Int
    private val _consumeItemInterval: Int

    init {
        val npcId = params.getInteger("i_summon_param1")
        summonTemplate = requireNotNull(NpcHolder.getInstance().getTemplate(npcId)) {
            "Summon: Template ID $npcId is NULL FIX IT!"
        }

        val param5 = params.getInteger("i_summon_param5")
        _lifeTime = when (param5) {
            0 -> DEFAULT_LIFE_TIME
            else -> param5
        } * 1000

        _consumeItemInterval = (when {
            summonTemplate.race != 21 -> 240
            else -> 60
        }) * 1000
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return

        if (casterPlayer.isProcessingRequest) {
            casterPlayer.sendPacket(SystemMsg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME)
            return
        }

        val currentSummon = casterPlayer.summon
        currentSummon?.unSummon(false)

        val summon = SummonInstance(
                IdFactory.getInstance().nextId,
                summonTemplate,
                casterPlayer,
                _lifeTime,
                _consumeItem.id,
                _consumeItem.count.toInt(),
                _consumeItemInterval,
                skill,
                true
        )

        casterPlayer.summon = summon

        summon.title = Servitor.TITLE_BY_OWNER_NAME
        summon.expPenalty = _expMultiplier / 100.0
        summon.exp = Experience.getExpForLevel(min(summon.level, Experience.getMaxAvailableLevel()))
        summon.heading = casterPlayer.heading
        summon.reflection = casterPlayer.reflection
        summon.setRunning()
        summon.spawnMe(Location.findAroundPosition(casterPlayer, 50, 70))
        summon.isFollowMode = true

        if (summon.getSkillLevel(4140) > 0)
            summon.altUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4140, summon.getSkillLevel(4140)), casterPlayer)

        summon.setCurrentHpMp(summon.stat.getMaxHp(), summon.stat.getMaxMp(), false)

        casterPlayer.listeners.onSummonServitor(summon)
    }

    companion object {
        private const val DEFAULT_LIFE_TIME = 2000000
    }

}