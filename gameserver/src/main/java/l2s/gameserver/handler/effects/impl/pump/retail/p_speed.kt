package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.items.Inventory
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.stats.conditions.Condition
import l2s.gameserver.templates.item.ArmorTemplate
import l2s.gameserver.templates.item.ItemTemplate
import l2s.gameserver.templates.item.ItemType
import l2s.gameserver.templates.item.WeaponTemplate
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_speed(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("p_speed_param2")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "p_speed_param3",
                    StatModifierType::class.java,
                    true
            )

    private val armorTypes: List<ArmorTemplate.ArmorType>
    private val weaponTypesMask: Long

    init {
        val conditions = params.getString("p_speed_param1")
        if (conditions == "all") {
            this.armorTypes = emptyList()
            this.weaponTypesMask = 0
        } else {
            val armorTypes = mutableListOf<ArmorTemplate.ArmorType>()
            var weaponTypesMask: Long = 0

            conditions.split(";").map {
                runCatching<ItemType> {
                    when {
                        it.startsWith("armor_") -> ArmorTemplate.ArmorType.valueOf(it.replace("armor_", "").toUpperCase())
                        else -> WeaponTemplate.WeaponType.valueOf(it.toUpperCase())
                    }
                }
                        .onFailure { error("Can't find item type value: $it") }
                        .getOrThrow()
            }.forEach {
                when (it) {
                    is ArmorTemplate.ArmorType -> armorTypes.add(it)
                    is WeaponTemplate.WeaponType -> weaponTypesMask = weaponTypesMask or it.mask()
                }
            }

            this.armorTypes = armorTypes.toList()
            this.weaponTypesMask = weaponTypesMask
        }
    }

    override fun getCondition(): Condition? {
        return null
    }

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (!caster.isPlayer) {
            return true
        }

        val inv: Inventory = caster.player.inventory

        if (weaponTypesMask != 0L) {
            if (weaponTypesMask and inv.wearedMask == 0L) {
                return false
            }
        }

        if (armorTypes.isEmpty()) {
            return true
        }

        for (type in armorTypes) {
            when (type) {
                ArmorTemplate.ArmorType.LIGHT, ArmorTemplate.ArmorType.HEAVY, ArmorTemplate.ArmorType.MAGIC -> {
                    val chest = inv.getPaperdollItem(InventorySlot.CHEST)
                    if (chest != null) {
                        val chestOk = type.mask() and chest.template.itemMask != 0L
                        if (chestOk && chest.template.bodyPart == ItemTemplate.SLOT_FULL_ARMOR) {
                            return true
                        }
                        val legs = inv.getPaperdollItem(InventorySlot.LEGS)
                        if (legs != null) {
                            if (chestOk && type.mask() and legs.template.itemMask != 0L) {
                                return true
                            }
                        }
                    }
                }
                ArmorTemplate.ArmorType.SIGIL -> {
                    val item: ItemInstance? = target.secondaryWeaponInstance
                    if (item != null && item.itemType == ArmorTemplate.ArmorType.SIGIL) {
                        return true
                    }
                }
                /*SHIELD -> {
                    val item: ItemInstance? = target.secondaryWeaponInstance
                    return item != null && item.itemType == ArmorType.SHIELD
                }*/
                //else -> return true
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
                    target.stat.mergeAdd(DoubleStat.RUN_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.RUN_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.WALK_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.WALK_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.SWIM_RUN_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.SWIM_RUN_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.SWIM_WALK_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.SWIM_WALK_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.FLY_RUN_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.FLY_RUN_SPEED, amount, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeAdd(DoubleStat.FLY_WALK_SPEED, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(DoubleStat.FLY_WALK_SPEED, amount, skill)
                }
            }
            StatModifierType.PER -> {
                val mul = amount / 100.0 + 1
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.RUN_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.RUN_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.WALK_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.WALK_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.SWIM_RUN_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.SWIM_RUN_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.SWIM_WALK_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.SWIM_WALK_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.FLY_RUN_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.FLY_RUN_SPEED, mul, skill)
                }
                if (skillEntry != null) {
                    target.stat.mergeMul(DoubleStat.FLY_WALK_SPEED, mul, skillEntry)
                } else {
                    target.stat.mergeMul(DoubleStat.FLY_WALK_SPEED, mul, skill)
                }
            }
        }
    }

}