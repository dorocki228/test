package l2s.gameserver.handler.effects.impl

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.items.Inventory
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.stats.conditions.Condition
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType
import l2s.gameserver.templates.item.ItemType
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType
import l2s.gameserver.templates.skill.EffectTemplate


/**
 * @author Sdw
 * @author Java-man
 */
abstract class AbstractDoubleStatConditionalItemTypeEffect(
        template: EffectTemplate,
        protected val mulStat: DoubleStat,
        protected val addStat: DoubleStat
) : EffectHandler(template) {

    protected val amount = params.getDouble(javaClass.simpleName + "_param2")
    protected val modifierType: StatModifierType =
            params.getEnum(
                    javaClass.simpleName + "_param3",
                    StatModifierType::class.java,
                    true
            )

    private val armorTypes: List<ArmorType>
    private val weaponTypes: List<WeaponType>

    constructor(
            template: EffectTemplate,
            stat: DoubleStat
    ) : this(template, stat, stat)

    init {
        val conditions = params.getString(javaClass.simpleName + "_param1")
        if (conditions == "all") {
            this.armorTypes = emptyList()
            this.weaponTypes = emptyList()
        } else {
            val armorTypes = mutableListOf<ArmorType>()
            val weaponTypes = mutableListOf<WeaponType>()
            conditions.split(";").map {
                runCatching<ItemType> {
                    when {
                        it.startsWith("armor_") -> ArmorType.valueOf(it.replace("armor_", "").toUpperCase())
                        else -> WeaponType.valueOf(it.toUpperCase())
                    }
                }
                        .onFailure { error("Can't find item type value: $it") }
                        .getOrThrow()
            }.forEach {
                when (it) {
                    is ArmorType -> armorTypes.add(it)
                    is WeaponType -> weaponTypes.add(it)
                }
            }

            this.armorTypes = armorTypes.toList()
            this.weaponTypes = weaponTypes.toList()
        }
    }

    override fun getCondition(): Condition? {
        return null
    }

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (!caster.isPlayer) {
            return true
        }

        if (weaponTypes.isNotEmpty()) {
            val weaponTemplate = caster.activeWeaponTemplate
            if (weaponTemplate == null) {
                if (!weaponTypes.contains(WeaponType.NONE)) {
                    return false
                }
            } else if (!weaponTypes.contains(weaponTemplate.itemType)) {
                return false
            }
        }

        if (armorTypes.isEmpty()) {
            return true
        }

        val inv: Inventory = caster.player.inventory

        for (type in armorTypes) {
            when (type) {
                ArmorType.NONE -> {
                    val chest = inv.getPaperdollItem(InventorySlot.CHEST)
                    if (chest == null) {
                        return true
                    }
                }
                ArmorType.LIGHT, ArmorType.HEAVY, ArmorType.MAGIC -> {
                    val chest = inv.getPaperdollItem(InventorySlot.CHEST)
                    if (chest != null) {
                        if (type == chest.template.itemType) {
                            return true
                        }
                    }
                }
                ArmorType.SIGIL -> {
                    val item = caster.secondaryWeaponInstance
                    if (item != null && item.itemType == ArmorType.SIGIL) {
                        return true
                    }
                }
            }
        }

        return false
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (modifierType) {
            StatModifierType.DIFF -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(addStat, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(addStat, amount, skill)
                }
            }
            StatModifierType.PER -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(mulStat, amount / 100.0 + 1.0, skillEntry)
                } else {
                    target.stat.mergeMul(mulStat, amount / 100.0 + 1.0, skill)
                }
            }
        }
    }

}