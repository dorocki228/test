package l2s.gameserver.handler.voicecommands.impl

import l2s.gameserver.Config
import l2s.gameserver.data.htm.HtmCache
import l2s.gameserver.data.xml.holder.ItemHolder
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler
import l2s.gameserver.model.Player
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.s2c.CIPacket
import l2s.gameserver.service.ItemService
import l2s.gameserver.utils.HtmlUtils
import l2s.gameserver.utils.velocity.VelocityUtils

/**
 * @author Java-man
 * @since 15.05.2019
 */
object Costumes : IVoicedCommandHandler {

    private val itemHolder = ItemHolder.getInstance()

    private val commands: Array<String> = arrayOf("costume", "showcostumes", "setcostume")

    private val costumes = listOf(37534, 23879, 47231, 29752, 49532, 75451, 75452, 75453, 75454, 75455)
            .map { itemHolder.getTemplate(it) }
            .onEach { require(it.visualChanges.isNotEmpty()) { "Costume should have visual changes." } }

    override fun getVoicedCommandList(): Array<String> {
        return commands
    }

    override fun useVoicedCommand(command: String, player: Player, args: String): Boolean {
        if (!Config.ALLOW_VOICED_COMMANDS) {
            return false
        }

        when (command) {
            "costume" -> {
                val costumes = getCostumes(player)
                val showCostumesVar = player.getVarBoolean("showcostumes", true)

                sendHtml(player, costumes, showCostumesVar)
            }
            "showcostumes" -> {
                val value = args.toBoolean()
                player.setVar("showcostumes", value)
                val costumes = getCostumes(player)
                sendHtml(player, costumes, value)
            }
            "setcostume" -> {
                if(args.isEmpty()){
                    return false
                }
                val split = args.split(" ")
                val id = split[0].toInt()
                if (id == 0) {
                    val visualItemObjId = split[1].toInt()
                    val item = player.inventory.getItemByObjectId(visualItemObjId)
                    if (!ItemService.getInstance().disableVisualChanges(item, player)) {
                        player.sendMessage("Снять визуализацию невозможно, у вас не полный сет либо надеты не те предметы!")
                        return false
                    }
                    val showCostumesVar = player.getVarBoolean("showcostumes", true)

                    val costumes = getCostumes(player)
                    sendHtml(player, costumes, showCostumesVar)
                    return true
                }
                val itemByItemId = player.inventory.getItemByItemId(id)
                if (!ItemService.getInstance().enableVisualChanges(itemByItemId, player)) {
                    player.sendMessage("Возможно в предмет уже вставлен костюм либо сет не полностью надет!")
                    return false
                }

                val costumes = getCostumes(player)
                val showCostumesVar = player.getVarBoolean("showcostumes", true)

                sendHtml(player, costumes, showCostumesVar)
            }
        }

        return true
    }

    /**
     * Костыль жесточайший FIXME:
     */
    private fun getCostumes(player: Player): List<Costume> {
        val equippeds = hashMapOf<Int, ItemInstance>()
        val inventory = player.inventory
        for (paperdoll in CIPacket.PAPERDOLL_ORDER_VISUAL_ID) {
            val item = inventory.getPaperdollItem(paperdoll)
            if (item != null && item.visualItemObjId != 0) {
                val visualItem = inventory.getItemByObjectId(item.visualItemObjId)
                if (visualItem != null) {
                    equippeds[visualItem.itemId] = visualItem
                }
            }
        }

        return costumes
                .map { inventory.getItemByItemId(it.itemId) }
                .filterNotNull()
                .map { item ->
                    val equipped = equippeds[item.itemId]

                    val itemId = item.itemId
                    var objId = item.objectId
                    if (equipped != null) {
                        objId = equipped.objectId
                    }
                    Costume(itemId, objId, item.name, item.template.icon, equipped != null)
                }
    }

    private fun sendHtml(
            player: Player,
            costumes: List<Costume>,
            showCostumesVar: Boolean
    ) {
        var html = HtmCache.getInstance().getHtml("command/costume.vm", player)
        val variables = mapOf(
                "costumes" to costumes,
                "showCostumesVar" to showCostumesVar
        )
        html = VelocityUtils.evaluate(html, variables)
        HtmlUtils.sendHtm(player, html)
    }

    class Costume(val id: Int, val objId: Int, val name: String, val icon: String, val equipped: Boolean)

}