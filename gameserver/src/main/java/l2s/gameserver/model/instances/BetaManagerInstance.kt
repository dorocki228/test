package l2s.gameserver.model.instances

import l2s.commons.collections.MultiValueSet
import l2s.gameserver.Config
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.dao.CommunityBufferDAO
import l2s.gameserver.data.xml.holder.CommunityBufferHolder
import l2s.gameserver.model.GameObject
import l2s.gameserver.model.Playable
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.Experience
import l2s.gameserver.network.l2.s2c.MagicSkillUse
import l2s.gameserver.templates.communitybuffer.BuffSet
import l2s.gameserver.templates.communitybuffer.BuffSkill
import l2s.gameserver.templates.item.ItemTemplate
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.utils.ItemFunctions
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * @author Java-man
 */
class BetaManagerInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
        NpcInstance(objectId, template, set) {

    override fun getHtmlDir(filename: String, player: Player): String = "custom/"

    override fun onBypassFeedback(player: Player, command: String) {
        if (!Config.BETA_SERVER) {
            player.sendMessage(if (player.isLangRus) "Данная функция не доступна." else "This feature is not available.")
            player.sendActionFailed()
            return
        }

        val st = StringTokenizer(command)
        when (st.nextToken()) {
            "level_up" -> setLevel(player, player.level + 1)
            "level_down" -> setLevel(player, player.level - 1)
            "add_skill_points" -> player.addExpAndSp(0, 100_000_000_000L)
            "add_col" -> ItemFunctions.addItem(player, 4037, 10000)
            "add_adena" -> ItemFunctions.addItem(player, ItemTemplate.ITEM_ID_ADENA, 100_000_000L)
            "get_buffs" -> buffPlayer(player)
            else -> super.onBypassFeedback(player, command)
        }
    }

    private fun buffPlayer(player: Player) {
        val availableSkills = CommunityBufferHolder.getInstance().getAvailableSkills(player)
        val buffSet = getBuffSets(-1)
        val setId = if (player.isMageClass) 2 else 1
        doBuff(player, buffSet[setId]?.getBuffSkills(availableSkills) ?: emptyList())
    }

    private fun getBuffSets(ownerId: Int): Map<Int, BuffSet> {
        return if (ownerId <= 0) CommunityBufferHolder.getInstance().buffSets else CommunityBufferDAO.getInstance().restore(
            ownerId
        )

    }

    private fun doBuff(target: Playable, buffs: Collection<BuffSkill>) {
        val player = target.player ?: return

        ThreadPoolManager.getInstance().execute {
            var success = false
            for (buff in buffs) {
                val nextbuff = checkSkill(buff, player)
                if (nextbuff == null)
                    continue

                nextbuff.skill.getEffects(target, target, nextbuff.timeAssign * 60 * 1000, nextbuff.timeModifier)

                success = true
            }

            if (success)
                player.broadcastPacket(MagicSkillUse(player, player, 23128, 1, 1, 0))
        }
    }

    private fun checkSkill(buffSkill: BuffSkill?, player: Player): BuffSkill? {
        return if (buffSkill == null) null else CommunityBufferHolder.getInstance().getAvailableSkills(player)[buffSkill.id]
    }

    private fun setLevel(target: GameObject?, level: Int) {
        if (target == null || !target.isPlayer) {
            return
        }

        if (target.isPlayer) {
            val player = target.player
            var level = min(level, player.maxLevel + 1)
            level = max(level, 1)

            val expAdd = Experience.getExpForLevel(level) - player.exp
            player.addExpAndSp(expAdd, 0, true)
        }
    }
}