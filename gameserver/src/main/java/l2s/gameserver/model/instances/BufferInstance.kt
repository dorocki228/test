package l2s.gameserver.model.instances

import gve.buffer.BuffProfileHolder
import l2s.commons.collections.MultiValueSet
import l2s.gameserver.Config
import l2s.gameserver.data.htm.HtmCache
import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.components.HtmlMessage
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.utils.ItemFunctions

import java.util.concurrent.TimeUnit

class BufferInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
    NpcInstance(objectId, template, set) {

    private val lastPage = mutableMapOf<Int, Int>().withDefault { 0 }

    override fun onBypassFeedback(player: Player, command: String) {
        if (!checkConditions(player))
            return

        var bph: BuffProfileHolder? = player.defaultProfile

        val args = command.split(" ")

        when {
            command == "regenHpMpCp" -> {
                player.currentCp = player.maxCp.toDouble()
                player.setCurrentHpMp(player.maxHp.toDouble(), player.maxMp.toDouble())
                showChatWindow(player, 0, false)
            }
            command == "cancel" -> {
                player.dispelBuffs()
                player.abnormalList.stopEffects(4215)
                player.abnormalList.stopEffects(4515)
                showChatWindow(player, 0, false)
            }
            command == "warriorSet" -> {
                for (s in Config.BUFFER_FIGHTER_SET)
                    s.getEffects(player, player, TimeUnit.HOURS.toMillis(2).toInt(), 1.0)
                showChatWindow(player, 0, false)
            }
            command == "mageSet" -> {
                for (s in Config.BUFFER_MAGE_SET)
                    s.getEffects(player, player, TimeUnit.HOURS.toMillis(2).toInt(), 1.0)
                showChatWindow(player, 0, false)
            }
            command.startsWith("buffPremium") -> {
                if (!havePremiumAccess(player)) {
                    showChatWindow(player, 666, false)
                    return
                }

                if (bph == null) {
                    return
                }

                val id = Integer.parseInt(args[1])

                val s = Config.BUFFER_BUFFS.get(id)
                if (s != null) {
                    s.getEffects(player, player, TimeUnit.HOURS.toMillis(2).toInt(), 1.0)

                    if (player.buffLimit + Config.ALT_MUSIC_LIMIT > bph.buffsCount() && !bph.hasBuff(id))
                        bph.addBuff(id)
                }

                showChatWindow(player, lastPage.getValue(player.objectId), false)
            }
            command.startsWith("buff") -> {
                if (bph == null) {
                    return
                }

                val id = Integer.parseInt(args[1])

                val s = Config.BUFFER_BUFFS.get(id)
                if (s != null) {
                    s.getEffects(player, player, TimeUnit.HOURS.toMillis(2).toInt(), 1.0)

                    if (player.buffLimit + Config.ALT_MUSIC_LIMIT > bph.buffsCount() && !bph.hasBuff(id))
                        bph.addBuff(id)
                }

                showChatWindow(player, lastPage.getValue(player.objectId), false)
            }
            command.startsWith("remove") -> {
                if (bph == null) {
                    return
                }

                val id = Integer.parseInt(args[1])
                bph.removeBuff(id)

                showChatWindow(player, lastPage.getValue(player.objectId), false)
            }
            command.startsWith("use") -> {
                player.setDefaultProfile(args[1])
                bph = player.defaultProfile
                if (bph == null) {
                    return
                }

                val premium = havePremiumAccess(player)
                val premiumBuffs = Config.BUFFER_PAGE_CONTENT.get(Config.BUFFER_PREMIUM_PAGE)
                val toRemove = bph.buffs
                    .filter { id -> !premium && premiumBuffs.containsKey(id) }
                    .toList()

                bph.removeBuffs(toRemove)

                bph.buffs
                    .mapNotNull { Config.BUFFER_BUFFS.get(it) }
                    .forEach {
                        it.getEffects(player, player, TimeUnit.HOURS.toMillis(2).toInt(), 1.0)
                    }

                showChatWindow(player, 0, false)
            }
            command.startsWith("delete") -> {
                player.deleteProfile(args[1])
                showChatWindow(player, 0, false)
            }
            command.startsWith("create") -> {
                if (args.size == 2) {
                    for (bp in player.buffProfiles)
                        if (bp.name.equals(args[1], ignoreCase = true)) {
                            showChatWindow(player, 0, false)
                            return
                        }

                    if (player.buffProfiles.size < Config.BUFFER_MAX_PROFILES) {
                        player.createNewProfile(args[1])
                        player.setDefaultProfile(args[1])
                    }

                    showChatWindow(player, 0, false)
                }

                showChatWindow(player, 0, false)
            }
            command.startsWith("setname") -> if (args.size == 3) {
                for (bp in player.buffProfiles)
                    if (bp.name.equals(args[2], ignoreCase = true)) {
                        showChatWindow(player, 0, false)
                        return
                    }

                for (bp in player.buffProfiles)
                    if (bp.name == args[1]) {
                        bp.name = args[2]
                        player.setDefaultProfile(args[2])
                        break
                    }

                showChatWindow(player, 0, false)
            }
            command.startsWith("clear") -> {
                if (bph == null) {
                    return
                }

                bph.clear()
                showChatWindow(player, 0, false)
            }
            else -> super.onBypassFeedback(player, command)
        }
    }

    private fun checkConditions(player: Player): Boolean {
        return !player.isInCombat
    }

    private fun havePremiumAccess(player: Player): Boolean {
        return Config.BUFFER_PREMIUM_ITEMS.any { ItemFunctions.haveItem(player, it, 1) }
    }

    override fun showChatWindow(player: Player, `val`: Int, firstTalk: Boolean, vararg replace: Any) {
        val filename = getHtmlPath(getHtmlFilename(`val`, player), player)

        val html = HtmCache.getInstance().getHtml(filename, player)
        val packet = HtmlMessage(this).setPlayVoice(firstTalk)
        packet.setHtml(html)

        var gbh = player.defaultProfile
        if (gbh == null) {
            player.createNewProfile("default")
            player.setDefaultProfile("default")

            gbh = player.defaultProfile
        }

        if (gbh == null) {
            return
        }

        if (`val` == 0) {
            packet.replace("%default_profile%", gbh.name)
            packet.replace("%default_profile_size%", getFreeSize(gbh, player))
            packet.replace("%profiles%", getCombolistProfiles(player))
        } else {
            val content = Config.BUFFER_PAGE_CONTENT.get(`val`)

            val sb = StringBuilder()

            val byppass = if (Config.BUFFER_PREMIUM_PAGE == `val`) "buffPremium" else "buff"

            if (content != null && content.isNotEmpty()) {
                sb.append("<table>")
                for (v in content.values) {
                    sb.append("<tr>")
                    sb.append("<td><img src=" + v.icon + " width=32 height=32 align=left></td>")
                    sb.append("<td><br><button value=\"" + v.name + "\" action=\"bypass -h npc_%objectId%_" + byppass + " " + v.id + "\" width=134 height=25 back=\"L2UI_CT1.Button_DF_Calculator_Down\" fore=\"L2UI_CT1.Button_DF_Calculator\"></td>")
                    if (gbh.hasBuff(v.id))
                        sb.append("<td width=32><br><button value=\" \" action=\"bypass -h npc_%objectId%_remove " + v.id + "\" width=24 height=24 back=\"L2UI_CT1.PersonalConnectionsWnd_DF_ListBtn_Block_Over\" fore=\"L2UI_CT1.PersonalConnectionsWnd_DF_ListBtn_Block_Down\"></td>")
                    else
                        sb.append("<td width=32></td>")
                    sb.append("</tr>")
                }
                sb.append("</table>")
            }

            packet.replace("%content%", sb.toString())
        }

        if (replace.size % 2 == 0) {
            var i = 0
            while (i < replace.size) {
                packet.replace(replace[i].toString(), replace[i + 1].toString())
                i += 2
            }
        }
        player.sendPacket(packet)
    }

    private fun getFreeSize(profile: BuffProfileHolder, player: Player): String {
        return profile.buffsCount().toString() + " / " + (player.buffLimit + Config.ALT_MUSIC_LIMIT)
    }

    private fun getCombolistProfiles(player: Player): String {
        return player.buffProfiles
            .filter { it.name != player.defaultProfileName }
            .joinToString(separator = ";") { it.name }
    }

    override fun getHtmlDir(filename: String, player: Player): String? {
        return "buffer/"
    }

    override fun getHtmlFilename(`val`: Int, player: Player): String {
        if (`val` == 0)
            return "index.htm"

        lastPage[player.objectId] = `val`

        return "index-$`val`.htm"
    }

}
