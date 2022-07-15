package l2s.gameserver.utils

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.Skill
import l2s.gameserver.model.base.AttributeType
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.components.HtmlMessage
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.item.Bodypart

/**
 * @author Java-man
 * @since 18.10.2019
 */
object Debug {

    fun sendStatsDebug(creature: Creature, stat: DoubleStat, set: StatsSet) {
        if (!creature.isPlayer) {
            return
        }

        val sb = StringBuilder()
        val weapon = creature.activeWeaponInstance
        for ((key, value) in set) {
            val parsedValue = parseValue(value)
            sb.append("<tr><td>$key</td><td><font color=\"LEVEL\">$parsedValue</font></td></tr>")
        }

        val msg = HtmlMessage()
        msg.setFile("admin/statsdebug.htm")
        msg.replace("%stat%", stat.toString())
        msg.replace("%mulValue%", Util.formatDouble(creature.stat.getMul(stat), "#.##"))
        msg.replace("%addValue%", creature.stat.getAdd(stat).toString())
        val baseValue = creature.template.getBaseValue(stat).orElse(0.0)
        msg.replace("%templateValue%", Util.formatDouble(baseValue, "#.##"))
        if (weapon != null) {
            val weaponValue = weapon.template.getStat(stat).orElse(0.0)
            msg.replace("%weaponBaseValue%", Util.formatDouble(weaponValue, "#.##"))
        }
        msg.replace("%details%", sb.toString())

        creature.sendPacket(msg)
    }

    fun sendSkillDebug(attacker: Creature, target: Creature, skill: Skill, set: StatsSet) {
        if (!attacker.isPlayer) {
            return
        }

        val sb = StringBuilder()
        for ((key, value) in set) {
            val parsedValue = parseValue(value)
            sb.append("<tr><td>$key</td><td><font color=\"LEVEL\">$parsedValue</font></td></tr>")
        }
        val msg = HtmlMessage()
        msg.setFile("admin/skilldebug.htm")
        msg.replace("%patk%", target.stat.getPAtk().toString())
        msg.replace("%matk%", target.stat.getMAtk().toString())
        msg.replace("%pdef%", target.stat.getPDef().toString())
        msg.replace("%mdef%", target.stat.getMDef().toString())
        msg.replace("%acc%", target.stat.getAccuracy().toString())
        msg.replace("%evas%", target.stat.getEvasionRate().toString())
        msg.replace("%crit%", target.stat.getCriticalHit().toString())
        msg.replace("%speed%", target.runSpeed.toString())
        msg.replace("%pAtkSpd%", target.stat.getPAtkSpd().toString())
        msg.replace("%mAtkSpd%", target.stat.getMAtkSpd().toString())
        msg.replace("%str%", target.str.toString())
        msg.replace("%dex%", target.dex.toString())
        msg.replace("%con%", target.con.toString())
        msg.replace("%int%", target.int.toString())
        msg.replace("%wit%", target.wit.toString())
        msg.replace("%men%", target.men.toString())
        msg.replace("%atkElemType%", target.stat.getAttackElement().name)
        msg.replace("%atkElemVal%", target.stat.getAttackElementValue(target.stat.getAttackElement()).toString())
        msg.replace("%fireDef%", target.stat.getDefenseElementValue(AttributeType.FIRE).toString())
        msg.replace("%waterDef%", target.stat.getDefenseElementValue(AttributeType.WATER).toString())
        msg.replace("%windDef%", target.stat.getDefenseElementValue(AttributeType.WIND).toString())
        msg.replace("%earthDef%", target.stat.getDefenseElementValue(AttributeType.EARTH).toString())
        msg.replace("%holyDef%", target.stat.getDefenseElementValue(AttributeType.HOLY).toString())
        msg.replace("%darkDef%", target.stat.getDefenseElementValue(AttributeType.UNHOLY).toString())
        msg.replace("%skill%", skill.name)
        msg.replace("%details%", sb.toString())

        attacker.sendPacket(msg)
    }

    fun sendItemDebug(player: Player, item: ItemInstance, set: StatsSet) {
        val sb = StringBuilder()
        for ((key, value) in set) {
            val parsesValue = parseValue(value)
            sb.append("<tr><td>$key</td><td><font color=\"LEVEL\">$parsesValue</font></td></tr>")
        }
        val msg = HtmlMessage()
        msg.setFile("admin/itemdebug.htm")
        msg.replace("%itemName%", item.name)
        msg.replace("%itemSlot%", getBodyPart(item.template.bodyPart))
        msg.replace("%itemType%", if (item.isArmor) "Armor" else if (item.isWeapon) "Weapon" else "Etc")
        msg.replace("%enchantLevel%", item.enchantLevel.toString())
        msg.replace("%isMagicWeapon%", item.template.isMagicWeapon.toString())
        msg.replace("%item%", item.toString())
        msg.replace("%details%", sb.toString())

        player.sendPacket(msg)
    }

    private fun parseValue(value: Any): String {
        return if (value is Double) {
            Util.formatDouble(value, "#.##")
        } else value.toString()
    }

    private fun getBodyPart(bodyPart: Long): String {
        for (entry in Bodypart.values()) {
            if (entry.mask() and bodyPart == bodyPart) {
                return entry.name
            }
        }
        return "Unknown"
    }

}