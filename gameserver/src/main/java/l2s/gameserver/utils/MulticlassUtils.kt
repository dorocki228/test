package l2s.gameserver.utils

import l2s.gameserver.Config
import l2s.gameserver.configuration.MultiClassConfig
import l2s.gameserver.data.htm.HtmCache
import l2s.gameserver.data.xml.holder.ItemHolder
import l2s.gameserver.data.xml.holder.SkillAcquireHolder
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.*
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.AcquireSkillDonePacket
import l2s.gameserver.network.l2.s2c.ExAcquirableSkillListByClass
import java.util.*

/**
 * @author Bonux
 * @author Java-man
 */
object MulticlassUtils {

    @JvmStatic
	fun onBypass(player: Player, bypass: String?) {
        val st = StringTokenizer(bypass, "_")
        when (st.nextToken()) {
            "list" -> {
                if (st.hasMoreTokens()) {
                    val raceId = st.nextToken().toInt()
                    val race = Race.VALUES[raceId]
                    if (st.hasMoreTokens()) {
                        val classTypeId = st.nextToken().toInt()
                        val classType = ClassType.VALUES[classTypeId]
                        showMulticlassList(player, race, classType)
                    } else {
                        showMulticlassList(player, race)
                    }
                } else {
                    showMulticlassList(player)
                }
            }
            "learn" -> {
                if (!st.hasMoreTokens()) return
                val id = st.nextToken().toInt()
                val classId = ClassId.valueOf(id)
                if (!checkMulticlass(player.classId, classId)) return
                showMulticlassAcquireList(player, classId)
            }
            "rebirth" -> {
                val rebirthCount = player.rebirthCount()
                if (!checkRebirth(player, rebirthCount)) {
                    return
                }

                rebirth(player, rebirthCount)
            }
        }
    }

    private fun showMulticlassList(player: Player, race: Race?, classType: ClassType? = null) {
        if (!Config.MULTICLASS_SYSTEM_ENABLED) return
        val tpls = HtmCache.getInstance().getTemplates("custom/multiclass.htm", player)
        var html = tpls[0]
        val playerClassId = player.classId
        val showByClassType = race == Race.HUMAN && playerClassId.classLevel.ordinal >= ClassLevel.SECOND.ordinal
        var backButton = ""
        val content = StringBuilder()
        if (race == null) {
            for (r in Race.VALUES) {
                var tempClassButton = tpls[1]
                tempClassButton = tempClassButton.replace("<?bypass?>", "list_" + r.ordinal)
                tempClassButton = tempClassButton.replace("<?image?>", tpls[2].replace("<?image_mark?>", r.toString()))
                tempClassButton = tempClassButton.replace("<?button_name?>", r.getName(player))
                content.append(tempClassButton)
            }
        } else if (classType == null && showByClassType) {
            for (c in ClassType.VALUES) {
                for (classId in ClassId.values()) {
                    if (classId.isDummy) continue
                    if (!classId.isOfRace(race)) continue
                    if (!classId.isOfType(c)) continue
                    if (!checkMulticlass(playerClassId, classId)) continue
                    var tempClassButton = tpls[1]
                    tempClassButton = tempClassButton.replace("<?bypass?>", "list_" + race.ordinal + "_" + c.ordinal)
                    tempClassButton = if (classId.classLevel == ClassLevel.NONE) // В клиенте нет иконок для 1й профессии.
                        tempClassButton.replace("<?image?>", tpls[2].replace("<?image_mark?>", classId.race.toString())) else tempClassButton.replace("<?image?>", tpls[2].replace("<?image_mark?>", classId.id.toString()))
                    tempClassButton = tempClassButton.replace("<?button_name?>", c.getName(player))
                    content.append(tempClassButton)
                    break
                }
            }
            backButton = tpls[3]
            backButton = backButton.replace("<?bypass?>", "list")
        } else {
            for (classId in ClassId.values()) {
                if (classId.isDummy) continue
                if (!classId.isOfRace(race)) continue
                if (classType != null && showByClassType && !classId.isOfType(classType)) continue
                if (!checkMulticlass(playerClassId, classId)) continue
                var tempClassButton = tpls[1]
                tempClassButton = tempClassButton.replace("<?bypass?>", "learn_" + classId.id)
                tempClassButton = if (classId.classLevel == ClassLevel.NONE) // В клиенте нет иконок для 1й профессии.
                    tempClassButton.replace("<?image?>", tpls[2].replace("<?image_mark?>", classId.race.toString())) else tempClassButton.replace("<?image?>", tpls[2].replace("<?image_mark?>", classId.id.toString()))
                tempClassButton = tempClassButton.replace("<?button_name?>", classId.getName(player))
                content.append(tempClassButton)
            }
            backButton = tpls[3]
            backButton = if (classType != null && showByClassType) backButton.replace("<?bypass?>", "list_" + race.ordinal) else backButton.replace("<?bypass?>", "list")
        }
        content.append(backButton)
        html = html.replace("<?content?>", content.toString())
        HtmlUtils.sendHtm(player, html)
    }

    @JvmStatic
	fun showMulticlassList(player: Player) {
        showMulticlassList(player, null, null)
    }

    @JvmStatic
	fun showMulticlassAcquireList(player: Player, classId: ClassId?) {
        val skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, classId, AcquireType.MULTICLASS, player.clan, null)
        val asl = ExAcquirableSkillListByClass(AcquireType.MULTICLASS, skills.size)
        for (s in skills) asl.addSkill(s.id, s.level, s.level, s.cost, s.minLevel)
        if (skills.isEmpty()) {
            player.sendPacket(AcquireSkillDonePacket.STATIC)
            player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN)
        } else {
            player.selectedMultiClassId = classId
            player.sendPacket(asl)
        }
        player.sendActionFailed()
    }

    @JvmStatic
	fun checkMulticlass(playerClassId: ClassId, multiClassId: ClassId): Boolean {
        if (playerClassId == multiClassId) return false
        if (multiClassId.isDummy) return false
        return playerClassId.isOfLevel(multiClassId.classLevel)
    }

    private fun checkRebirth(player: Player, rebirthCount: Int): Boolean {
        if (player.level < MultiClassConfig.rebirthRequireLevel) {
            player.sendMessage("You should have ${MultiClassConfig.rebirthRequireLevel} level for rebirth.")
            return false
        }

        if (player.sp < MultiClassConfig.rebirthRequireSp) {
            player.sendMessage("You should have ${MultiClassConfig.rebirthRequireSp} sp for rebirth.")
            return false
        }

        val items = MultiClassConfig.rebirthRequireItems.getValue(rebirthCount)
        val missedItems = items.filter { item ->
            !ItemFunctions.haveItem(player, item.id, item.count)
        }
        if (missedItems.isNotEmpty()) {
            missedItems.forEach { item ->
                val template = ItemHolder.getInstance().getTemplate(item.id)
                val itemName = template.getName(player)
                player.sendMessage("You should have ${item.count} $itemName for rebirth.")
            }
            return false
        }

        return true
    }

    private fun rebirth(player: Player, rebirthCount: Int) {
        player.setVar("rebirth_count", player.rebirthCount() + 1)

        val expAdd = Experience.getExpForLevel(1) - player.exp
        player.addExpAndSp(expAdd, 0, true)

        MultiClassConfig.rebirthRewardItems.forEach { item ->
            ItemFunctions.addItem(player, item.id, item.count)
        }
    }

    private fun Player.rebirthCount(): Int = this.getVarInt("rebirth_count", 0)
}