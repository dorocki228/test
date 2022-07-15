package l2s.gameserver.model.actor.instances.player.tasks

import l2s.commons.lang.reference.HardReference
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.model.Player
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Zone
import l2s.gameserver.model.actor.instances.creature.AbnormalList
import l2s.gameserver.model.actor.instances.player.ShortCut
import l2s.gameserver.skills.EffectUseType
import l2s.gameserver.skills.SkillCastingType
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.item.ExItemType
import java.util.concurrent.ScheduledFuture

/**
 * @author Java-man
 */
class AutoShortcutTask(private val reference: HardReference<Player>) : Runnable {

    private var autoShortcutsTask: ScheduledFuture<*>? = null

    override fun run() {
        val player = reference.get()
        if (player == null) {
            autoShortcutsTask?.cancel(true)
            return
        }

        if (player.isInZone(Zone.ZoneType.peace_zone)) {
            return
        }

        /*if (player.isInCombat) {
            return
        }*/

        if (!player.autoShortcutsCast.compareAndSet(false, true)) {
            return
        }

        launchAutoShortcuts(player, player.allEnabledAutoShortcuts.iterator())
    }

    fun setTask(future: ScheduledFuture<*>?) {
        autoShortcutsTask = future
    }

    private fun launchAutoShortcuts(actor: Player, iterator: Iterator<ShortCut>) {
        ThreadPoolManager.getInstance().schedule({
            if (!actor.autoShortcutsCast.get()) {
                return@schedule
            }

            if (!iterator.hasNext()) {
                actor.autoShortcutsCast.set(false)
                return@schedule
            }

            val autoShortcut = iterator.next()
            if (!autoShortcut.isAutoUseEnabled) {
                launchAutoShortcuts(actor, iterator)
                return@schedule
            }

            tryUse(actor, autoShortcut)
            launchAutoShortcuts(actor, iterator)
        }, 500)
    }

    private fun tryUse(
            player: Player,
            autoShortcut: ShortCut
    ): Boolean {
        when (autoShortcut.type) {
            ShortCut.ShortCutType.ITEM -> {
                if (player.isUseItemDisabled) {
                    return false
                }

                val item = player.inventory.getItemByObjectId(autoShortcut.id) ?: return false

                val calledSkills = mutableListOf<SkillEntry>()

                val isPotion = item.exType == ExItemType.POTION
                // TODO add cache
                val attachedSkills = item.template.attachedSkills
                for (attachedSkill in attachedSkills) {
                    for (effectTemplate in attachedSkill.template.getEffectTemplates(EffectUseType.NORMAL)) {
                        val handler = effectTemplate.handler
                        val calledSkillsTemp = handler.calledSkills
                        if (calledSkillsTemp.isNotEmpty())
                            calledSkills.addAll(calledSkillsTemp)
                    }

                    for (effectTemplate in attachedSkill.template.getEffectTemplates(EffectUseType.NORMAL_INSTANT)) {
                        val handler = effectTemplate.handler
                        val name = handler.name
                        val check = if (isPotion) {
                            when (name) {
                                "i_hp", "i_heal" -> {
                                    player.currentHpPercents <= 80
                                }
                                "i_mp" -> {
                                    player.currentMpPercents <= 80
                                }
                                "i_cp" -> {
                                    player.currentCpPercents <= 80
                                }
                                else -> true
                            }
                        } else {
                            true
                        }

                        if (!check) {
                            return false
                        }

                        val calledSkillsTemp = handler.calledSkills
                        if (calledSkillsTemp.isNotEmpty())
                            calledSkills.addAll(calledSkillsTemp)
                    }
                }

                for (abnormal in player.abnormalList) {
                    if (attachedSkills.any { !canBeCasted(abnormal.skill, it.template) }) {
                        return false
                    }

                    if (calledSkills.any { !canBeCasted(abnormal.skill, it.template) }) {
                        return false
                    }
                }

                return player.useItem(item, true, true)
            }
            ShortCut.ShortCutType.SKILL -> {
                if (player.isActionsDisabled(true)) {
                    return false
                }

                val skillEntry = player.getKnownSkill(autoShortcut.id) ?: return false
                val target = skillEntry.template.getAimingTarget(player, player, skillEntry.template, false, true, false)
                        ?: return false
                if (!skillEntry.template.checkCondition(skillEntry, player, target, false, true, true)) {
                    return false
                }
                if (target.abnormalList.any { !canBeCasted(it.skill, skillEntry.template) }) {
                    return false
                }

                return player.doCast(skillEntry, target, false)
            }
            else -> return true
        }
    }

    private fun canBeCasted(oldSkill: Skill, newSkill: Skill): Boolean {
        if (oldSkill.id == newSkill.id && oldSkill.level >= newSkill.level)
            return false
        if (AbnormalList.checkAbnormalType(newSkill, oldSkill))
            return false
        return true
    }

}