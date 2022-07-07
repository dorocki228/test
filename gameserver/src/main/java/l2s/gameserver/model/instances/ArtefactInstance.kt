package l2s.gameserver.model.instances

import l2s.commons.collections.MultiValueSet
import l2s.gameserver.model.Creature
import l2s.gameserver.templates.npc.NpcTemplate

class ArtefactInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
    NpcInstance(objectId, template, set) {

    init {
        isHasChatWindow = false
    }

    override fun isArtefact(): Boolean {
        return true
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun isAttackable(attacker: Creature): Boolean {
        return false
    }

}
