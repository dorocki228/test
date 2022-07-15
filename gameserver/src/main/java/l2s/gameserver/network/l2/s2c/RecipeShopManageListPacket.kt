package l2s.gameserver.network.l2.s2c

import l2s.gameserver.model.Player
import l2s.gameserver.model.items.DynamicManufactureItem
import l2s.gameserver.network.l2.OutgoingPackets
import l2s.gameserver.templates.item.RecipeTemplate
import kotlin.math.min

class RecipeShopManageListPacket(seller: Player, private val isDwarven: Boolean) : IClientOutgoingPacket {

    private val createList: Collection<DynamicManufactureItem>
    private val recipes: Collection<RecipeTemplate>
    private val sellerId: Int = seller.objectId
    private val adena: Long = seller.adena

    init {
        recipes = if (isDwarven)
            seller.dwarvenRecipeBook
        else
            seller.commonRecipeBook

        createList = seller.createList.values
            .map { DynamicManufactureItem(it.recipeId, it.cost, seller, seller) }
    }

    override fun write(packetWriter: l2s.commons.network.PacketWriter): Boolean {
        OutgoingPackets.RECIPE_SHOP_MANAGE_LIST.writeId(packetWriter)
        packetWriter.writeD(sellerId)
        packetWriter.writeD(min(adena, Integer.MAX_VALUE.toLong()).toInt()) //FIXME не менять на packet.writeQ, в текущем клиенте там все еще D (видимо баг NCSoft)
        packetWriter.writeD(if (isDwarven) 0x00 else 0x01)
        packetWriter.writeD(recipes.size)
        var i = 1
        for (recipe in recipes) {
            packetWriter.writeD(recipe.id)
            packetWriter.writeD(i++)
        }
        packetWriter.writeD(createList.size)
        for (mi in createList) {
            packetWriter.writeD(mi.recipeId)
            packetWriter.writeD(mi.canCraft)
            packetWriter.writeQ(mi.cost)
        }

        return true
    }
}