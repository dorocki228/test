package l2s.gameserver.model.items.listeners

import l2s.gameserver.data.xml.holder.OptionDataHolder
import l2s.gameserver.listener.inventory.OnEquipListener
import l2s.gameserver.model.Playable
import l2s.gameserver.model.items.ItemInstance

object ItemAugmentationListener : OnEquipListener {

    override fun onEquip(slot: Int, item: ItemInstance, actor: Playable) {
        if (!item.isEquipable)
            return
        if (!item.isAugmented)
            return

        val player = actor.player ?: return

        // При несоотвествии грейда аугмент не применяется
        if (player.getExpertisePenalty(item) > 0)
            return

        val stats = item.augmentations

        var updateStats = false
        var sendSkillList = false
        for (i in stats) {
            val template = OptionDataHolder.getInstance().getTemplate(i) ?: continue

            if (player.addOptionData(template) != template) {
                updateStats = true
                if (!template.skills.isEmpty())
                    sendSkillList = true
            }
        }

        if (updateStats) {
            if (sendSkillList)
                player.sendSkillList()
            player.sendChanges()
        }
    }

    override fun onUnequip(slot: Int, item: ItemInstance, actor: Playable) {
        if (!item.isEquipable)
            return
        if (!item.isAugmented)
            return

        val player = actor.player ?: return

        val stats = item.augmentations

        var updateStats = false
        var sendSkillList = false
        for (i in stats) {
            val template = player.removeOptionData(i)
            if (template != null) {
                updateStats = true
                if (template.skills.isNotEmpty())
                    sendSkillList = true
            }
        }

        if (updateStats) {
            if (sendSkillList)
                player.sendSkillList()
            player.updateStats()
        }
    }

}