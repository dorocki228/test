package l2s.gameserver.network.l2.s2c

import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.OutgoingPackets
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.RecipeTemplate

class RecipeItemMakeInfoPacket private constructor(
    player: Player,
    recipe: RecipeTemplate,
    private val result: Int,
    private val offeringsAllowed: Boolean,
    private val itemsCount: Long,
    private val createCriticalSuccess: Boolean
) : IClientOutgoingPacket {

    companion object {

        fun success(
            player: Player,
            recipe: RecipeTemplate,
            offeringsAllowed: Boolean,
            itemsCount: Long,
            createCriticalSuccess: Boolean
        ): RecipeItemMakeInfoPacket {
            return RecipeItemMakeInfoPacket(player, recipe, 1, offeringsAllowed, itemsCount, createCriticalSuccess)
        }

        fun failure(
            player: Player,
            recipe: RecipeTemplate,
            offeringsAllowed: Boolean
        ): RecipeItemMakeInfoPacket {
            return RecipeItemMakeInfoPacket(player, recipe, 0, offeringsAllowed, 0, false)
        }

        fun info(
            player: Player,
            recipe: RecipeTemplate,
            offeringsAllowed: Boolean
        ): RecipeItemMakeInfoPacket {
            return RecipeItemMakeInfoPacket(player, recipe, -1, offeringsAllowed, 0, false)
        }

    }

    private val _id: Int = recipe.id
    private val _isCommon: Boolean = recipe.isCommon
    private val _curMP: Int = player.currentMp.toInt()
    private val _maxMP: Int = player.maxMp
    private val chanceBonus: Double = player.stat.getValue(DoubleStat.CRAFT_CHANCE_BONUS)
    private val criticalCraftChance: Double = if (recipe.isCanBeCriticalCrafted)
        player.stat.getValue(DoubleStat.CRAFT_CRITICAL_CREATION_CHANCE)
    else
        0.0

    override fun write(packetWriter: l2s.commons.network.PacketWriter): Boolean {
        OutgoingPackets.RECIPE_ITEM_MAKE_INFO.writeId(packetWriter)
        packetWriter.writeD(_id) //ID рецепта
        packetWriter.writeD(if (_isCommon) 0x01 else 0x00)
        packetWriter.writeD(_curMP)
        packetWriter.writeD(_maxMP)
        packetWriter.writeD(result) //итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
        packetWriter.writeC(offeringsAllowed)
        packetWriter.writeQ(itemsCount)
        packetWriter.writeF(chanceBonus)
        if (criticalCraftChance > 0.0) {
            packetWriter.writeC(0x01)
            packetWriter.writeF(criticalCraftChance)
        } else {
            packetWriter.writeC(0x00)
            packetWriter.writeF(0.0)
        }
        packetWriter.writeC(createCriticalSuccess)

        return true
    }
}