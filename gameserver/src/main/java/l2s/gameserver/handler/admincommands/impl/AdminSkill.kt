package l2s.gameserver.handler.admincommands.impl

import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.admincommands.IAdminCommandHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.GameObject
import l2s.gameserver.model.Player
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.HtmlMessage
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.utils.HtmlUtils
import l2s.gameserver.utils.Log

class AdminSkill : IAdminCommandHandler {
    private enum class Commands {
        admin_show_skills,
        admin_remove_skills,
        admin_remove_all_skills,
        admin_skill_list,
        admin_skill_index,
        admin_add_skill,
        admin_remove_skill,
        admin_get_skills,
        admin_reset_skills,
        admin_give_all_skills,
        admin_show_effects,
        admin_debug_stats,
        admin_remove_cooldown,
        admin_buff,
        admin_callskill,
        admin_use_skill
    }

    override fun useAdminCommand(
        comm: Enum<*>,
        wordList: Array<String>,
        fullString: String,
        activeChar: Player
    ): Boolean {
        val command = comm as Commands

        if (!activeChar.playerAccess.CanEditChar)
            return false

        when (command) {
            Commands.admin_show_skills -> showSkillsPage(activeChar)
            Commands.admin_show_effects -> showEffects(activeChar)
            Commands.admin_remove_skills -> removeSkillsPage(activeChar)
            Commands.admin_remove_all_skills -> removeAllSkills(activeChar)
            Commands.admin_skill_list -> activeChar.sendPacket(HtmlMessage(5).setFile("admin/skills.htm"))
            Commands.admin_skill_index -> if (wordList.size > 1)
                activeChar.sendPacket(HtmlMessage(5).setFile("admin/skills/" + wordList[1] + ".htm"))
            Commands.admin_add_skill -> adminAddSkill(activeChar, wordList)
            Commands.admin_remove_skill -> adminRemoveSkill(activeChar, wordList)
            Commands.admin_get_skills -> adminGetSkills(activeChar)
            Commands.admin_reset_skills -> adminResetSkills(activeChar)
            Commands.admin_give_all_skills -> adminGiveAllSkills(activeChar)
            Commands.admin_debug_stats -> debug_stats(activeChar)
            Commands.admin_remove_cooldown -> {
                val target = activeChar.target
                var player: Player? = null
                if (target != null && target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
                    player = target as Player
                else {
                    activeChar.sendPacket(SystemMsg.INVALID_TARGET)
                    return false
                }
                player.resetReuse()
                player.sendPacket(SkillCoolTimePacket(activeChar))
                player.sendMessage("The reuse delay of all skills has been reseted.")

                showSkillsPage(activeChar)
            }
            Commands.admin_buff -> {
                for (i in 7041..7064)
                    activeChar.addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, i, 1))
                activeChar.sendSkillList()
            }
            Commands.admin_use_skill, Commands.admin_callskill -> adminCallSkill(
                activeChar,
                wordList
            )
        }

        return true
    }

    private fun debug_stats(activeChar: Player) {
        val target_obj = activeChar.target
        if (target_obj == null || !target_obj.isCreature) {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val target = target_obj as Creature

        val calculators = target.stat.calculators

        var log_str = "--- Debug for " + target.name + " ---\r\n"

        for (calculator in calculators) {
            if (calculator == null)
                continue
            //val env = Env(target, activeChar, null)
            var value = calculator.base
            log_str += "Stat: " + calculator.stat.value + ", base value = $value, prevValue: " + calculator.last + "\r\n"

            val funcs = calculator.functions
            funcs.filter { func ->
                val funcOwner = func.owner
                // TODO optimize and remove hardcode
                if (funcOwner is Skill && funcOwner.isPassive
                    && funcOwner.id !in 45001..45016 && funcOwner.id !in 54034..54035
                    && funcOwner.id !in 55253..55268
                ) {
                    return@filter false
                }
                return@filter func.modifierType == null
            }.forEachIndexed { index, func ->
                var order = Integer.toHexString(func.order).toUpperCase()
                if (order.length == 1)
                    order = "0$order"
                log_str += "\tFunc: no modifier #" + index + "@ [0x" + order + "]" + func.javaClass.simpleName + "\t" + value
                val condition = func.condition
                if (condition == null || condition.test(target, activeChar, null, null, value))
                    value = func.calc(target, activeChar, null, value)
                log_str += " -> " + value + (if (func.owner != null) "; owner: " + func.owner.toString() else "; no owner") + "\r\n"
            }
            log_str += "after no modifiers value = $value\r\n"

            funcs.filter { func ->
                val funcOwner = func.owner
                // TODO optimize and remove hardcode
                if (funcOwner !is Skill || !funcOwner.isPassive || funcOwner.id in 45001..45016
                    || funcOwner.id in 54034..54035 || funcOwner.id in 55253..55268
                ) {
                    return@filter false
                }
                return@filter func.modifierType == null
            }.forEachIndexed { index, func ->
                var order = Integer.toHexString(func.order).toUpperCase()
                if (order.length == 1)
                    order = "0$order"
                log_str += "\tFunc: no modifier #" + index + "@ [0x" + order + "]" + func.javaClass.simpleName + "\t" + value
                val condition = func.condition
                if (condition == null || condition.test(target, activeChar, null, null, value))
                    value = func.calc(target, activeChar, null, value)
                log_str += " -> " + value + (if (func.owner != null) "; owner: " + func.owner.toString() else "; no owner") + "\r\n"
            }
            log_str += "after no modifiers passives value = $value\r\n"

            funcs.filter { func ->
                val funcOwner = func.owner
                // TODO optimize and remove hardcode
                if (funcOwner !is Skill || !funcOwner.isPassive || funcOwner.id in 45001..45016
                    || funcOwner.id in 54034..54035 || funcOwner.id in 55253..55268
                ) {
                    return@filter false
                }
                return@filter func.modifierType == StatModifierType.PER
            }.forEachIndexed { index, func ->
                var order = Integer.toHexString(func.order).toUpperCase()
                if (order.length == 1)
                    order = "0$order"
                log_str += "\tFunc: no modifier #" + index + "@ [0x" + order + "]" + func.javaClass.simpleName + "\t" + value
                val condition = func.condition
                if (condition == null || condition.test(target, activeChar, null, null, value))
                    value = func.calc(target, activeChar, null, value)
                log_str += " -> " + value + (if (func.owner != null) "; owner: " + func.owner.toString() else "; no owner") + "\r\n"
            }
            log_str += "after PER modifiers passives value = $value\r\n"

            funcs.filter { func ->
                val funcOwner = func.owner
                // TODO optimize and remove hardcode
                if (funcOwner !is Skill || !funcOwner.isPassive || funcOwner.id in 45001..45016
                    || funcOwner.id in 54034..54035 || funcOwner.id in 55253..55268
                ) {
                    return@filter false
                }
                return@filter func.modifierType == StatModifierType.DIFF
            }.forEachIndexed { index, func ->
                var order = Integer.toHexString(func.order).toUpperCase()
                if (order.length == 1)
                    order = "0$order"
                log_str += "\tFunc: no modifier #" + index + "@ [0x" + order + "]" + func.javaClass.simpleName + "\t" + value
                val condition = func.condition
                if (condition == null || condition.test(target, activeChar, null, null, value))
                    value = func.calc(target, activeChar, null, value)
                log_str += " -> " + value + (if (func.owner != null) "; owner: " + func.owner.toString() else "; no owner") + "\r\n"
            }
            log_str += "after DIFF modifiers passives value = $value\r\n"

            //val env2 = Env(target, activeChar, null)
            funcs.filter { func ->
                val funcOwner = func.owner
                // TODO optimize and remove hardcode
                if (funcOwner is Skill && funcOwner.isPassive
                    && funcOwner.id !in 45001..45016 && funcOwner.id !in 54034..54035
                    && funcOwner.id !in 55253..55268
                ) {
                    return@filter false
                }
                return@filter func.modifierType == StatModifierType.PER
            }.forEachIndexed { index, func ->
                var order = Integer.toHexString(func.order).toUpperCase()
                if (order.length == 1)
                    order = "0$order"
                log_str += "\tFunc: PER modifier #" + index + "@ [0x" + order + "]" + func.javaClass.simpleName + "\t" + value
                val condition = func.condition
                if (condition == null || condition.test(target, activeChar, null, null, value))
                    value = func.calc(target, activeChar, null, value)
                log_str += " -> " + value + (if (func.owner != null) "; owner: " + func.owner.toString() else "; no owner") + "\r\n"
            }
            //value *= target.stat.getMul(calculator.stat, null, null)
            log_str += "after PER modifiers value = $value\r\n"

            //val env3 = Env(target, activeChar, null)
            funcs.filter { func ->   val funcOwner = func.owner
                // TODO optimize and remove hardcode
                if (funcOwner is Skill && funcOwner.isPassive
                    && funcOwner.id !in 45001..45016 && funcOwner.id !in 54034..54035
                    && funcOwner.id !in 55253..55268
                ) {
                    return@filter false
                }
                return@filter func.modifierType == StatModifierType.DIFF
            }.forEachIndexed { index, func ->
                var order = Integer.toHexString(func.order).toUpperCase()
                if (order.length == 1)
                    order = "0$order"
                log_str += "\tFunc: DIFF modifier #" + index + "@ [0x" + order + "]" + func.javaClass.simpleName + "\t" + value
                val condition = func.condition
                if (condition == null || condition.test(target, activeChar, null, null, value))
                    value = func.calc(target, activeChar, null, value)
                log_str += " -> " + value + (if (func.owner != null) "; owner: " + func.owner.toString() else "; no owner") + "\r\n"
            }
            //value += target.stat.getAdd(calculator.stat, null, null)
            log_str += "after DIFF modifiers value = $value\r\n"
        }

        Log.add(log_str, "debug_stats")
    }

    /**
     * This function will give all the skills that the gm target can have at its
     * level to the traget
     *
     * @param activeChar: the gm char
     */
    private fun adminGiveAllSkills(activeChar: Player) {
        val target = activeChar.target
        var player: Player? = null
        if (target != null && target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }
        val skillCounter = player.rewardSkills(true, true, true, false)
        player.sendMessage("Admin gave you $skillCounter skills.")
        activeChar.sendMessage("You gave " + skillCounter + " skills to " + player.name)
    }

    override fun getAdminCommandEnum(): Array<Enum<*>> {
        return Commands.values() as Array<Enum<*>>
    }

    private fun removeSkillsPage(activeChar: Player) {
        val target = activeChar.target
        val player: Player
        if (target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val skills = player.allSkills

        val adminReply = HtmlMessage(5)
        val replyMSG = StringBuilder("<html><body>")
        replyMSG.append("<table width=260><tr>")
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>")
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("</tr></table>")
        replyMSG.append("<br><br>")
        replyMSG.append("<center>Editing character: " + player.name)
        replyMSG.append("<br>Level: " + player.level + " " + HtmlUtils.htmlClassName(player.classId.id) + "</center>")
        replyMSG.append("<br><center>Click on the skill you wish to remove:</center>")
        replyMSG.append("<br><table width=270>")
        replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>")
        for (element in skills)
            replyMSG.append(
                "<tr><td width=80><a action=\"bypass -h admin_remove_skill " + element.id + "\">" + element.getName(
                    activeChar
                ) + "</a></td><td width=60>" + element.level + "</td><td width=40>" + element.id + "</td></tr>"
            )
        replyMSG.append("</table>")
        replyMSG.append("<br><center><table>")
        replyMSG.append("Remove custom skill:")
        replyMSG.append("<tr><td>Id: </td>")
        replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>")
        replyMSG.append("</table></center>")
        replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill \$id_to_remove\" width=110 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>")
        replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>")
        replyMSG.append("</body></html>")

        adminReply.setHtml(replyMSG.toString())
        activeChar.sendPacket(adminReply)
    }

    private fun removeAllSkills(activeChar: Player) {
        val target = activeChar.target
        val player: Player
        if (target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val skills = player.allSkills
        for (skillEntry in skills) {
            if (skillEntry != null)
                player.removeSkill(skillEntry, true)
        }

        player.sendSkillList()

        activeChar.sendMessage("You removed all skills from target: " + player.name + ".")
        showSkillsPage(activeChar)
    }

    private fun showSkillsPage(activeChar: Player) {
        val target = activeChar.target
        val player: Player
        if (target != null && target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val adminReply = HtmlMessage(5)

        val replyMSG = StringBuilder("<html><body>")
        replyMSG.append("<table width=260><tr>")
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>")
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("</tr></table>")
        replyMSG.append("<br><br>")
        replyMSG.append("<center>Editing character: " + player.name)
        replyMSG.append("<br>Level: " + player.level + " " + HtmlUtils.htmlClassName(player.classId.id) + "</center>")
        replyMSG.append("<br><center><table>")
        replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>")
        replyMSG.append("<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("<td><button value=\"Delete all skills\" action=\"bypass -h admin_remove_all_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>")
        replyMSG.append("<tr><td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("<td><button value=\"Reset reuse\" action=\"bypass -h admin_remove_cooldown\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>")
        replyMSG.append("<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=100 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>")
        replyMSG.append("</table></center>")
        replyMSG.append("</body></html>")

        adminReply.setHtml(replyMSG.toString())
        activeChar.sendPacket(adminReply)
    }

    private fun showEffects(activeChar: Player) {
        val target = activeChar.target
        val player: Player
        if (target != null && target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val adminReply = HtmlMessage(5)

        val replyMSG = StringBuilder("<html><body>")
        replyMSG.append("<table width=260><tr>")
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>")
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>")
        replyMSG.append("</tr></table>")
        replyMSG.append("<br><br>")
        replyMSG.append("<center>Editing character: " + player.name + "</center>")

        replyMSG.append("<br><center><button value=\"")
        replyMSG.append(if (player.isLangRus) "Обновить" else "Refresh")
        replyMSG.append("\" action=\"bypass -h admin_show_effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center>")
        replyMSG.append("<br>")

        for (e in player.abnormalList)
            replyMSG.append(e.skill.getName(activeChar)).append(" ").append(e.skill.level).append(" - ").append(if (e.skill.isToggle) "Infinity" else e.timeLeft.toString() + " seconds").append(
                "<br1>"
            )
        replyMSG.append("<br></body></html>")

        adminReply.setHtml(replyMSG.toString())
        activeChar.sendPacket(adminReply)
    }

    private fun adminGetSkills(activeChar: Player) {
        val target = activeChar.target
        val player: Player
        if (target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        if (player.name == activeChar.name)
            player.sendMessage("There is no point in doing it on your character.")
        else {
            val skills = player.allSkills
            adminSkills = activeChar.allSkillsArray
            for (element in adminSkills!!)
                activeChar.removeSkill(element, true)
            for (element in skills)
                activeChar.addSkill(element, true)
            activeChar.sendMessage("You now have all the skills of  " + player.name + ".")
        }

        showSkillsPage(activeChar)
    }

    private fun adminResetSkills(activeChar: Player) {
        val target = activeChar.target
        val player: Player
        if (target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target.player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val counter = 0
        //TODO WtF?
        /*	SkillEntry[] skills = player.getAllSkillsArray();
			for(L2Skill element : skills)
			if(!element.isCommon() && !SkillTreeTable.getInstance().isSkillPossible(player, element.getId(), element.getLevel()))
			{
				player.removeSkill(element, true);
				counter++;
			}*/
        player.checkSkills()
        player.sendSkillList()
        player.sendMessage("[GM]" + activeChar.name + " has updated your skills.")
        activeChar.sendMessage("$counter skills removed.")

        showSkillsPage(activeChar)
    }

    private fun adminAddSkill(activeChar: Player, wordList: Array<String>) {
        val target = activeChar.target
        val player: Player
        if (target != null && target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        if (wordList.size >= 2) {
            val id = Integer.parseInt(wordList[1])
            var level = 1
            if (wordList.size >= 3)
                level = Integer.parseInt(wordList[2])
            val skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, id, level)
            if (skillEntry != null) {
                player.addSkill(skillEntry, true)
                player.sendSkillList()
                player.player!!.sendPacket(
                    SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(
                        skillEntry.id,
                        skillEntry.level
                    )
                )
                activeChar.sendMessage("You gave the skill " + skillEntry.getName(activeChar) + " to " + player.name + ".")
            } else
                activeChar.sendMessage("Error: there is no such skill.")
        }

        showSkillsPage(activeChar)
    }

    private fun adminRemoveSkill(activeChar: Player, wordList: Array<String>) {
        val target = activeChar.target
        var player: Player? = null
        if (target.isPlayer && (activeChar === target || activeChar.playerAccess.CanEditCharAll))
            player = target as Player
        else {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        if (wordList.size == 2) {
            val id = Integer.parseInt(wordList[1])
            val level = player.getSkillLevel(id)
            val skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, id, level)
            if (skillEntry != null) {
                player.sendMessage("Admin removed the skill " + skillEntry.getName(player) + ".")
                player.removeSkill(skillEntry, true)
                player.sendSkillList()
                activeChar.sendMessage("You removed the skill " + skillEntry.getName(activeChar) + " from " + player.name + ".")
            } else
                activeChar.sendMessage("Error: there is no such skill.")
        }

        removeSkillsPage(activeChar)
    }

    private fun adminCallSkill(player: Player, wordList: Array<String>) {
        var target: GameObject? = player.target
        if (target == null)
            target = player

        if (!target.isPlayer)
            return

        val skill =
            SkillHolder.getInstance().getSkill(Integer.parseInt(wordList[1]), Integer.parseInt(wordList[2])) ?: return

        val targets = ArrayList<Creature>()
        targets.add(target.player)
        player.callSkill(target.player, SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill), targets, false, false)
    }

    companion object {

        private var adminSkills: Array<SkillEntry>? = null
    }
}