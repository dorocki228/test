package l2s.gameserver.network.l2.s2c

import l2s.gameserver.model.Player
import l2s.gameserver.model.items.DynamicManufactureItem
import l2s.gameserver.network.l2.OutgoingPackets
import l2s.gameserver.stats.DoubleStat

class RecipeShopSellListPacket(buyer: Player, manufacturer: Player) : IClientOutgoingPacket {

    private val objId: Int = manufacturer.objectId
    private val curMp: Int = manufacturer.currentMp.toInt()
    private val maxMp: Int = manufacturer.maxMp
    private val adena: Long = buyer.adena
    private val createList: Collection<DynamicManufactureItem>
    private val chanceBonus: Double
    private val criticalCraftChance: Double

    init {
        createList = manufacturer.createList.values
            .map { DynamicManufactureItem(it.recipeId, it.cost, buyer, manufacturer) }
        chanceBonus = manufacturer.stat.getValue(DoubleStat.CRAFT_CHANCE_BONUS)
        criticalCraftChance = manufacturer.stat.getValue(DoubleStat.CRAFT_CRITICAL_CREATION_CHANCE)
    }

    override fun write(packetWriter: l2s.commons.network.PacketWriter): Boolean {
        OutgoingPackets.RECIPE_SHOP_SELL_LIST.writeId(packetWriter)

        packetWriter.writeD(objId)
        packetWriter.writeD(curMp)//Creator's MP
        packetWriter.writeD(maxMp)//Creator's MP
        packetWriter.writeQ(adena)
        packetWriter.writeD(createList.size)
        for (mi in createList) {
            packetWriter.writeD(mi.recipeId)
            packetWriter.writeD(mi.canCraft)
            packetWriter.writeQ(mi.cost)
            packetWriter.writeF(chanceBonus)
            if (mi.canBeCriticalCrafted && criticalCraftChance > 0.0) {
                packetWriter.writeC(0x01)
                packetWriter.writeF(criticalCraftChance)
            } else {
                packetWriter.writeC(0x00)
                packetWriter.writeF(0.0)
            }
        }

        return true
    }

}