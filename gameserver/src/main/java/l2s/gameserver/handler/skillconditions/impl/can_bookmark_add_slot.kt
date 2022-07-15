package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

class can_bookmark_add_slot(params: StatsSet) : SkillCondition(params) {

    private val _teleportBookmarkSlots = params.getInteger("can_bookmark_add_slot_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        if (player.bookMarkList.capacity + _teleportBookmarkSlots > 9) {
            player.sendPacket(SystemMsg.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT)
            return false
        }

        return true
    }

}