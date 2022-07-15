package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType

/**
 * @author Java-man
 */
class equip_weapon(params: StatsSet) : SkillCondition(params) {

    private val weaponTypes: List<WeaponType>

    init {
        val conditions = params.getString("equip_weapon_param1")
        weaponTypes = conditions.split(";").map {
            runCatching<WeaponType> {
                val name = it.toUpperCase()
                WeaponType.valueOf(name)
            }
                    .onFailure { error("Can't find item type value: $it") }
                    .getOrThrow()
        }

        require(weaponTypes.isNotEmpty())
    }

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (!caster.isPlayer) {
            return true
        }

        val weaponTemplate = caster.activeWeaponTemplate
        if (weaponTemplate != null && weaponTypes.contains(weaponTemplate.itemType)) {
            return true
        }

        return false
    }

}