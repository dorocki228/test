package l2s.gameserver.features.foursepulchers.instances

import l2s.commons.collections.MultiValueSet
import l2s.gameserver.Config
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Party
import l2s.gameserver.model.Player
import l2s.gameserver.model.instances.RaidBossInstance
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.utils.ItemFunctions
import l2s.gameserver.features.foursepulchers.FourSepulchersHall

class SepulcherRaidInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
    RaidBossInstance(objectId, template, set) {

    override fun onDeath(killer: Creature) {
        val player = killer.player
        if (player != null)
            giveCup(player)

        super.onDeath(killer)
    }

    override fun onDelete() {
        val param = (getParameter("four_sepulchers_hall")
            ?: throw IllegalArgumentException("Can't find hall for npc $npcId"))
        val hall = param as FourSepulchersHall
        val parentNpcId = getParameter("parent_npc_id", -1)
        require(parentNpcId != -1) { "Can't find parent npc for npc $npcId" }
        hall.spawnEmperorsGraveNpc(this, parentNpcId)
        super.onDelete()
    }

    private fun giveCup(player: Player) {
        var cupId = 0
        val oldBrooch = 7262

        when (npcId) {
            25339 -> cupId = 7256
            25342 -> cupId = 7257
            25346 -> cupId = 7258
            25349 -> cupId = 7259
        }

        val party = player.party
        if (party != null) {
            for (mem in party.partyMembers) {
                if (mem.inventory.getItemByItemId(oldBrooch) == null && player.isInRange(
                        mem,
                        Config.ALT_PARTY_DISTRIBUTION_RANGE
                    )
                )
                    ItemFunctions.addItem(mem, cupId, 1/*, "Give sup for party by SepulcherRaidInstance"*/)
            }
        } else {
            if (player.inventory.getItemByItemId(oldBrooch) == null)
                ItemFunctions.addItem(player, cupId, 1/*, "Give sup for player by SepulcherRaidInstance"*/)
        }
    }

    override fun canChampion(): Boolean {
        return false
    }

    override fun canRaidBerserk(): Boolean {
        return false
    }
}