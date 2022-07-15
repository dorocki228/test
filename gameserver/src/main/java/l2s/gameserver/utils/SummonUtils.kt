package l2s.gameserver.utils

import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.model.Player
import l2s.gameserver.model.instances.SummonInstance
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType

object SummonUtils {

    fun restoreSummon(
            player: Player,
            restored: SummonInstance.RestoredSummon
    ) {
        val skill = SkillHolder.getInstance().getSkill(restored.skillId, restored.skillLvl)
                ?: return
        val skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill)
        player.callSkill(player, skillEntry, listOf(player), false, false)
    }

}