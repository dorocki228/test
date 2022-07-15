package l2s.gameserver.model.actor.stat

import l2s.commons.math.MathUtils
import l2s.gameserver.Config
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Skill.SkillMagicType
import l2s.gameserver.model.actor.MoveType
import l2s.gameserver.model.base.AttributeType
import l2s.gameserver.model.base.ElementalElement
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.model.skill.SkillConditionScope
import l2s.gameserver.skills.*
import l2s.gameserver.stats.*
import l2s.gameserver.stats.calculators.CalculationType
import l2s.gameserver.stats.funcs.Func
import l2s.gameserver.templates.item.ItemGrade
import l2s.gameserver.utils.PositionUtils.Position
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.ceil
import kotlin.math.max

open class CreatureStat<T : Creature>(val owner: T) {

    private val lock = ReentrantReadWriteLock()

    private val doubleStats: MutableMap<DoubleStat, DoubleStatValue> = EnumMap(DoubleStat::class.java)
    private val fixedValue: MutableMap<DoubleStat, Double> = ConcurrentHashMap()
    private val booleanStats: MutableSet<BooleanStat> = EnumSet.noneOf(BooleanStat::class.java)
    private val positionStats: MutableMap<DoubleStat, MutableMap<Position, Double>> =
        ConcurrentHashMap()
    private val moveTypeStats: MutableMap<DoubleStat, MutableMap<MoveType, Double>> =
        ConcurrentHashMap()
    private val reuseStat: MutableMap<SkillMagicType, Double> = ConcurrentHashMap()
    private val castingStat: MutableMap<SkillMagicType, Double> = EnumMap(SkillMagicType::class.java)
    private val mpConsumeStat: MutableMap<SkillMagicType, Double> = ConcurrentHashMap()
    private val skillEvasionStat: MutableMap<SkillMagicType, LinkedList<Double>> =
        ConcurrentHashMap()

    private val attackTraitValues = DoubleArray(TraitType.values().size)
    private val defenceTraitValues = DoubleArray(TraitType.values().size)
    private val attackTraits = EnumSet.noneOf(TraitType::class.java)
    private val defenceTraits = EnumSet.noneOf(TraitType::class.java)
    private val invulnerableTraits = EnumSet.noneOf(TraitType::class.java)

    private val _blockActionsAllowedSkills = HashSet<Int>()
    private val _blockActionsAllowedItems = HashSet<Int>()

    /** Values to be recalculated after every stat update  */
    private var attackSpeedMultiplier = 1.0
    private var mAttackSpeedMultiplier = 1.0

    private val abnormalShieldBlocks = AtomicInteger()

    private var maxBuffCount = Config.ALT_BUFF_LIMIT
    private var vampiricSum = 0.0

    private var expertiseLevel = ItemGrade.NONE
    private var expertisePenaltyBonus = 0

    val calculators = arrayOfNulls<Calculator>(DoubleStat.NUM_STATS)

    @Deprecated("")
    fun addFuncs(vararg funcs: Func) {
        synchronized(calculators) {
            for (func in funcs) {
                val stat = func.stat.ordinal
                val temp = calculators[stat]
                val calculator = if (temp != null) temp else {
                    val temp = Calculator(func.stat, owner)
                    calculators[stat] = temp
                    temp
                }
                if (func.passive) {
                    calculator.addPassive(func)
                } else {
                    calculator.addFunc(func)
                }
            }
        }
    }

    @Deprecated("")
    fun removeFuncsByOwner(owner: Any) {
        synchronized(calculators) {
            for (calculator in calculators) {
                calculator?.removeByOwner(owner)
            }
        }
    }

    /**
     * Merges the double stat add
     * @param doubleStat the double stat
     * @param value the value
     */
    fun mergeAdd(doubleStat: DoubleStat, value: Double, skill: Skill) {
        val doubleStatValue = doubleStats.getOrPut(doubleStat) {
            DoubleStatValue(doubleStat)
        }

        when {
            skill.isPassive -> doubleStatValue.passiveAdd = doubleStat.add(doubleStatValue.passiveAdd, value)
            skill.isPassive -> doubleStatValue.passiveAdd = doubleStat.add(doubleStatValue.passiveAdd, value)
            else -> doubleStatValue.add = doubleStat.add(doubleStatValue.add, value)
        }
    }

    /**
     * Merges the double stat add
     * @param doubleStat the double stat
     * @param value the value
     */
    fun mergeAdd(doubleStat: DoubleStat, value: Double, skill: SkillEntry) {
        val doubleStatValue = doubleStats.getOrPut(doubleStat) {
            DoubleStatValue(doubleStat)
        }

        when {
            skill.entryType == SkillEntryType.ENSOUL -> {
                doubleStatValue.add = doubleStat.add(doubleStatValue.add, value)
            }
            skill.template.isPassive -> {
                doubleStatValue.passiveAdd = doubleStat.add(doubleStatValue.passiveAdd, value)
            }
            else -> doubleStatValue.add = doubleStat.add(doubleStatValue.add, value)
        }
    }

    /**
     * Merges the double stat mul
     * @param doubleStat the double stat
     * @param mul the mul
     */
    fun mergeMul(doubleStat: DoubleStat, mul: Double, skill: Skill) {
        val doubleStatValue = doubleStats.getOrPut(doubleStat) {
            DoubleStatValue(doubleStat)
        }

        when {
            skill.isPassive -> doubleStatValue.passiveMul = doubleStat.mul(doubleStatValue.passiveMul, mul)
            else -> doubleStatValue.mul = doubleStat.mul(doubleStatValue.mul, mul)
        }
    }

    /**
     * Merges the double stat mul
     * @param doubleStat the double stat
     * @param mul the mul
     */
    fun mergeMul(doubleStat: DoubleStat, mul: Double, skill: SkillEntry) {
        val doubleStatValue = doubleStats.getOrPut(doubleStat) {
            DoubleStatValue(doubleStat)
        }

        when {
            skill.entryType == SkillEntryType.ENSOUL -> {
                doubleStatValue.mul = doubleStat.mul(doubleStatValue.mul, mul)
            }
            skill.template.isPassive -> {
                doubleStatValue.passiveMul = doubleStat.mul(doubleStatValue.passiveMul, mul)
            }
            else -> doubleStatValue.mul = doubleStat.mul(doubleStatValue.mul, mul)
        }
    }

    /**
     * @param doubleStat
     * @return the add value
     */
    fun getAdd(doubleStat: DoubleStat): Double {
        return lock.read {
            val doubleStatValue = doubleStats[doubleStat]
            doubleStatValue?.add ?: doubleStat.resetAddValue
        }
    }

    /**
     * @param doubleStat
     * @return the add value
     */
    fun getPassiveAdd(doubleStat: DoubleStat): Double {
        return lock.read {
            val doubleStatValue = doubleStats[doubleStat]
            doubleStatValue?.passiveAdd ?: doubleStat.resetAddValue
        }
    }

    /**
     * @param doubleStat
     * @return the mul value
     */
    fun getMul(doubleStat: DoubleStat): Double {
        return lock.read {
            val doubleStatValue = doubleStats[doubleStat]
            doubleStatValue?.mul ?: doubleStat.resetMulValue
        }
    }

    /**
     * @param doubleStat
     * @return the mul value
     */
    fun getPassiveMul(doubleStat: DoubleStat): Double {
        return lock.read {
            val doubleStatValue = doubleStats[doubleStat]
            doubleStatValue?.passiveMul ?: doubleStat.resetMulValue
        }
    }

    /**
     * @param stat
     * @param baseValue
     * @return the final value of the stat
     */
    fun getValue(
            stat: DoubleStat,
            calculationType: CalculationType = CalculationType.FULL_VALUE,
            baseValue: Double
    ): Double {
        return fixedValue[stat] ?: stat.calculate(owner, calculationType, baseValue)
    }

    /**
     * @param stat
     * @return the final value of the stat
     */
    @JvmOverloads
    fun getValue(
            stat: DoubleStat,
            calculationType: CalculationType = CalculationType.FULL_VALUE
    ): Double {
        return fixedValue[stat] ?: stat.calculate(owner, calculationType, null)
    }

    /**
     * @param stat
     * @param baseValue
     * @return the final value of the stat
     */
    fun getValue(
            stat: DoubleStat,
            baseValue: Double
    ): Double {
        return fixedValue[stat] ?: stat.calculate(
                owner,
                CalculationType.FULL_VALUE,
                baseValue
        )
    }

    fun set(stat: BooleanStat) {
        booleanStats.add(stat)
    }

    fun has(stat: BooleanStat): Boolean {
        return lock.read {
            return@has booleanStats.contains(stat)
        }
    }

    /**
     * @param stat
     * @param value
     * @return true if the there wasn't previously set fixed value, `false` otherwise
     */
    fun addFixedValue(stat: DoubleStat, value: Double): Boolean {
        return fixedValue.put(stat, value) == null
    }

    /**
     * @param stat
     * @return `true` if fixed value is removed, `false` otherwise
     */
    fun removeFixedValue(stat: DoubleStat): Boolean {
        return fixedValue.remove(stat) != null
    }

    fun getPositionTypeValue(stat: DoubleStat, position: Position): Double {
        val map = positionStats[stat] ?: return 1.0
        return map[position] ?: 1.0
    }

    fun mergePositionTypeValue(
            stat: DoubleStat,
            position: Position,
            value: Double,
            func: (Double, Double) -> Double
    ) {
        positionStats.computeIfAbsent(stat) { ConcurrentHashMap() }
            .merge(position, value, func)
    }

    fun getMoveTypeValue(stat: DoubleStat, type: MoveType): Double {
        val map = moveTypeStats[stat] ?: return 0.0
        return map[type] ?: 0.0
    }

    fun mergeMoveTypeValue(stat: DoubleStat, type: MoveType, value: Double) {
        moveTypeStats.computeIfAbsent(stat) { ConcurrentHashMap() }
            .merge(type, value, MathUtils::add)
    }

    fun getReuseTypeValue(magicType: SkillMagicType): Double {
        return lock.read {
            reuseStat.getOrDefault(magicType, 1.0)
        }
    }

    fun mergeReuseTypeValue(
        magicType: SkillMagicType,
        value: Double,
        func: (Double, Double) -> Double
    ) {
        reuseStat.merge(magicType, value, func)
    }

    fun getCastChanceValue(magicType: SkillMagicType): Double {
        return lock.read {
            castingStat.getOrDefault(magicType, 0.0)
        }
    }

    fun mergeCastChanceValue(
            magicType: SkillMagicType,
            value: Double,
            func: (Double, Double) -> Double
    ) {
        castingStat.merge(magicType, value, func)
    }

    fun getMpConsumeTypeValue(magicType: SkillMagicType): Double {
        val value = lock.read {
            mpConsumeStat.getOrDefault(magicType, 0.0)
        }

        return max(1 + value, 0.0)
    }

    fun mergeMpConsumeTypeValue(
            magicType: SkillMagicType,
            value: Double,
            func: (Double, Double) -> Double
    ) {
        mpConsumeStat.merge(magicType, value, func)
    }

    fun getSkillEvasionTypeValue(magicType: SkillMagicType): Double {
        val skillEvasions = skillEvasionStat[magicType]
        return if (skillEvasions != null && !skillEvasions.isEmpty()) {
            skillEvasions.peekLast()
        } else 0.0
    }

    fun addSkillEvasionTypeValue(magicType: SkillMagicType, value: Double) {
        skillEvasionStat
            .computeIfAbsent(magicType) { LinkedList() }
            .add(value)
    }

    fun removeSkillEvasionTypeValue(magicType: SkillMagicType, value: Double) {
        skillEvasionStat.computeIfPresent(magicType) { k, v ->
            v.remove(value)
            if (!v.isEmpty()) v else null
        }
    }

    fun mergeAttackTrait(traitType: TraitType, value: Double) {
        attackTraitValues[traitType.ordinal] *= value
        attackTraits.add(traitType)
    }

    fun getAttackTrait(traitType: TraitType): Double {
        return lock.read {
            attackTraitValues[traitType.ordinal]
        }
    }

    fun hasAttackTrait(traitType: TraitType): Boolean {
        return lock.read {
            attackTraits.contains(traitType)
        }
    }

    fun mergeDefenceTrait(traitType: TraitType, value: Double) {
        defenceTraitValues[traitType.ordinal] *= value
        defenceTraits.add(traitType)
    }

    fun getDefenceTrait(traitType: TraitType): Double {
        return lock.read {
            defenceTraitValues[traitType.ordinal]
        }
    }

    fun hasDefenceTrait(traitType: TraitType): Boolean {
        return lock.read {
            defenceTraits.contains(traitType)
        }
    }

    fun mergeInvulnerableTrait(traitType: TraitType) {
        invulnerableTraits.add(traitType)
    }

    fun isInvulnerableTrait(traitType: TraitType): Boolean {
        return lock.read {
            invulnerableTraits.contains(traitType)
        }
    }

    fun addBlockActionsAllowedSkill(skillId: Int) {
        _blockActionsAllowedSkills.add(skillId)
    }

    fun isBlockedActionsAllowedSkill(skill: Skill): Boolean {
        lock.read {
            return _blockActionsAllowedSkills.contains(skill.id)
        }
    }

    fun addBlockActionsAllowedItem(itemId: Int) {
        _blockActionsAllowedItems.add(itemId)
    }

    fun isBlockedActionsAllowedItem(item: ItemInstance): Boolean {
        lock.read {
            return _blockActionsAllowedItems.contains(item.itemId)
        }
    }

    protected open fun resetStats() {
        doubleStats.forEach { (k: DoubleStat, v: DoubleStatValue) -> v.reset(k) }
        booleanStats.clear()
        _blockActionsAllowedSkills.clear()
        _blockActionsAllowedItems.clear()
        castingStat.clear()
        vampiricSum = 0.0
        maxBuffCount = Config.ALT_BUFF_LIMIT
        Arrays.fill(attackTraitValues, 1.0)
        Arrays.fill(defenceTraitValues, 1.0)
        attackTraits.clear()
        defenceTraits.clear()
        invulnerableTraits.clear()
        mpConsumeStat.clear()
        reuseStat.clear()
    }

    /**
     * Locks and resets all stats and recalculates all
     * @param broadcast
     */
    fun recalculateStats(broadcast: Boolean) {
        val changedDoubleStats: MutableSet<DoubleStat> = mutableSetOf()

        lock.write {
            // Copy old data before wiping it out
            if (broadcast) {
                doubleStats.values.forEach(DoubleStatValue::mark)
            }

            // Wipe all the data
            resetStats()

            // Collect all necessary effects
            val abnormals = owner.abnormalList
                .filter { it.isActive }

            // Call pump to each effect
            abnormals.forEach { abnormal ->
                abnormal.effects.filter {
                    it.checkPumpConditionImpl(abnormal, abnormal.effector, abnormal.effected)
                }.forEach {
                    it.pump(abnormal.effected, null)
                }
            }

            // Apply all passives
            owner.allSkills.asSequence()
                .filter { it.template.isPassive }
                .filter {
                    it.template.checkConditions(SkillConditionScope.PASSIVE, owner, owner)
                }
                .forEach {
                    val effects = it.template.getEffectTemplates(EffectUseType.NORMAL)
                    for (effect in effects) {
                        val handler = effect.handler
                        if (handler.checkPumpConditionImpl(null, owner, owner)) {
                            handler.pump(owner, it)
                        }
                    }
                }

            if (owner.isSummon) {
                val summonOwner = owner.player
                val ownerAbnormalList = summonOwner.abnormalList
                if (summonOwner != null && ownerAbnormalList.contains(AbnormalType.ABILITY_CHANGE)) {
                    ownerAbnormalList
                        .filter { abnormal ->
                            abnormal.checkAbnormalType(AbnormalType.ABILITY_CHANGE)
                        }
                        .filter { it.isActive }
                        .forEach { abnormal ->
                            abnormal.effects.filter {
                                it.checkPumpConditionImpl(abnormal, owner, owner)
                            }.forEach {
                                it.pump(owner, null)
                            }
                        }
                }
            }

            // Merge with additional stats
            /* TODO
            additionalAdd.stream()
                .filter(Predicate<StatsHolder> { holder: StatsHolder -> holder.verifyCondition(_activeChar) }).forEach(
                    Consumer<StatsHolder> { holder: StatsHolder -> mergeAdd(holder.getStat(), holder.getValue()) }
                )
            additionalMul.stream()
                .filter(Predicate<StatsHolder> { holder: StatsHolder -> holder.verifyCondition(_activeChar) }).forEach(
                    Consumer<StatsHolder> { holder: StatsHolder -> mergeMul(holder.getStat(), holder.getValue()) }
                )*/

            attackSpeedMultiplier = Formulas.calcAtkSpdMultiplier(owner)
            mAttackSpeedMultiplier = Formulas.calcMAtkSpdMultiplier(owner)

            if (broadcast) { // Calculate the difference between old and new stats
                for ((key, value) in doubleStats) {
                    if (value.hasChanged()) {
                        changedDoubleStats.add(key)
                    }
                }
            }
        }

        if (changedDoubleStats.isNotEmpty()) {
            // TODO owner.broadcastModifiedStats(changedDoubleStats)
        }

        owner.updateStats()

        // Notify recalculation to child classes
        onRecalculateStats(broadcast)
    }

    protected open fun onRecalculateStats(broadcast: Boolean) {
        val maxHp = getMaxHp()
        val maxCp = getMaxCp()
        val maxMp = getMaxMp()

        // Check if current HP/MP/CP is lower than max, and regeneration is not running, start it.
        if (owner.currentHp < maxHp || owner.currentMp < maxMp || owner.currentCp < maxCp) {
            owner.startRegeneration()
        } else {
            // Check if Max HP/MP/CP is lower than current due to new stats.
            if (owner.currentHp > maxHp) {
                owner.setCurrentHp(maxHp, false, false)
            }
            if (owner.currentMp > maxMp) {
                owner.setCurrentMp(maxMp, false)
            }
            if (owner.currentCp > maxCp) {
                owner.setCurrentCp(maxCp, false)
            }
        }
    }

    fun getMaxHp(): Double {
        return getValue(DoubleStat.MAX_HP)
    }

    fun getMaxRecoverableHp(): Int {
        return getValue(DoubleStat.MAX_RECOVERABLE_HP, getMaxHp()).toInt()
    }

    fun getMaxMp(): Double {
        return getValue(DoubleStat.MAX_MP)
    }

    fun getMaxRecoverableMp(): Int {
        return getValue(DoubleStat.MAX_RECOVERABLE_MP, getMaxMp()).toInt()
    }

    fun getMaxCp(): Double {
        return getValue(DoubleStat.MAX_CP)
    }

    fun getMaxRecoverableCp(): Int {
        return getValue(DoubleStat.MAX_RECOVERABLE_CP, getMaxCp()).toInt()
    }

    /**
     * Return the MAtk (base+modifier) of the L2Character.<br></br>
     * <B><U>Example of use</U>: Calculate Magic damage
     * @return
    </B> */
    fun getMAtk(): Double {
        return getValue(DoubleStat.MAGICAL_ATTACK)
    }

    /**
     * @return the MAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
     */
    fun getMAtkSpd(): Int {
        return getValue(DoubleStat.MAGICAL_ATTACK_SPEED).toInt()
    }

    /**
     * @param init
     * @return the Critical Damage rate (base+modifier) of the L2Character.
     */
    fun getCriticalDmg(init: Double): Double {
        return getValue(DoubleStat.CRITICAL_DAMAGE, init)
    }

    /**
     * @return the Critical Hit rate (base+modifier) of the L2Character.
     */
    fun getCriticalHit(): Int {
        return getValue(DoubleStat.CRITICAL_RATE).toInt()
    }

    /**
     * @return the Magic Critical Hit rate (base+modifier) of the L2Character.
     */
    fun getMCriticalHit(): Int {
        return getValue(DoubleStat.MAGIC_CRITICAL_RATE).toInt()
    }

    /**
     * <B><U>Example of use </U>: Calculate Magic damage.
     * @return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.
    </B> */
    fun getMDef(): Double {
        return getValue(DoubleStat.MAGICAL_DEFENCE)
    }

    /**
     * @return the PAtk (base+modifier) of the L2Character.
     */
    fun getPAtk(): Int {
        return getValue(DoubleStat.PHYSICAL_ATTACK).toInt()
    }

    /**
     * @return the PAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
     */
    fun getPAtkSpd(): Int {
        return getValue(DoubleStat.PHYSICAL_ATTACK_SPEED).toInt()
    }

    /**
     * @return the PDef (base+modifier) of the L2Character.
     */
    fun getPDef(): Int {
        return getValue(DoubleStat.PHYSICAL_DEFENCE).toInt()
    }

    /**
     * @return the Accuracy (base+modifier) of the L2Character in function of the Weapon Expertise Penalty.
     */
    fun getAccuracy(): Int {
        return getValue(DoubleStat.ACCURACY_COMBAT).toInt()
    }

    /**
     * @return the Magic Accuracy (base+modifier) of the L2Character
     */
    fun getMagicAccuracy(): Int {
        return getValue(DoubleStat.ACCURACY_MAGIC).toInt()
    }

    /**
     * @return the Attack Evasion rate (base+modifier) of the L2Character.
     */
    fun getEvasionRate(): Int {
        return getValue(DoubleStat.EVASION_RATE).toInt()
    }

    /**
     * @return the Attack Evasion rate (base+modifier) of the L2Character.
     */
    fun getMagicEvasionRate(): Int {
        return getValue(DoubleStat.MAGIC_EVASION_RATE).toInt()
    }

    /**
     * @param skill
     * @return the mpConsume.
     */
    fun getMpConsume(skill: Skill?): Int {
        if (skill == null) {
            return 1
        }

        var mpConsume = skill.mpConsume2
        if (skill.isMusic) {
            val nextDanceMpCost = ceil(mpConsume / 2.0)
            val danceCount = owner.abnormalList.filter { it.skill.isMusic }.distinct().count()
            if (/*PlayerConfig.DANCE_CONSUME_ADDITIONAL_MP &&*/ danceCount > 0) {
                mpConsume += danceCount * nextDanceMpCost
            }
        }

        val mpConsumeTypeValue = getMpConsumeTypeValue(skill.magicType)
        return (mpConsume * mpConsumeTypeValue).toInt()
    }

    open fun getAttackElement(): AttributeType {
        val weaponInstance = owner.activeWeaponInstance
        // 1st order - weapon element
        if (weaponInstance != null) {
            val weaponAttackElement = weaponInstance.attackElement
            if (weaponAttackElement != AttributeType.NONE) {
                return weaponAttackElement
            }
        }

        // Find the greatest attack element attribute greater than 0.
        return AttributeType.VALUES.asSequence()
            .filter { getAttackElementValue(it) > 0 }
            .maxBy { getAttackElementValue(it) } ?: AttributeType.NONE
    }

    open fun getAttackElementValue(attackAttribute: AttributeType): Int {
        val stat = attackAttribute.attack ?: return 0
        return getValue(stat).toInt()
    }

    open fun getDefenseElementValue(defenseAttribute: AttributeType): Int {
        val stat = defenseAttribute.defence ?: return 0
        return getValue(stat).toInt()
    }

    open fun getElementalAttackPower(element: ElementalElement): Double {
        return -1.0
    }

    open fun getElementalDefence(element: ElementalElement): Double {
        return 0.0
    }

    open fun getElementalCritRate(element: ElementalElement): Double {
        return 0.0
    }

    open fun getElementalCritAttack(element: ElementalElement): Double {
        return 0.0
    }

    /**
     * Calculates the time required for this skill to be used again.
     * @param skill the skill from which reuse time will be calculated.
     * @return the time in milliseconds this skill is being under reuse.
     */
    fun getReuseTime(skill: Skill): Long {
        val reuseDelay =  skill.reuseDelay.toLong()
        return when {
            skill.isHandler || skill.isItemSkill -> reuseDelay
            skill.isReuseDelayPermanent -> reuseDelay
            else -> (skill.reuseDelay * getReuseTypeValue(skill.magicType)).toLong()
        }
    }

    /**
     * @return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations.
     */
    fun getAttackSpeedMultiplier(): Double {
        return attackSpeedMultiplier
    }

    fun getMAttackSpeedMultiplier(): Double {
        return mAttackSpeedMultiplier
    }

    /**
     * Sets amount of debuffs that player can avoid
     * @param times
     */
    fun setAbnormalShieldBlocks(times: Int) {
        abnormalShieldBlocks.set(times)
    }

    /**
     * @return the amount of debuffs that player can avoid
     */
    fun getAbnormalShieldBlocks(): Int {
        return abnormalShieldBlocks.get()
    }

    /**
     * @return the amount of debuffs that player can avoid
     */
    fun decrementAbnormalShieldBlocks(): Int {
        return abnormalShieldBlocks.decrementAndGet()
    }

    /**
     * Gets the maximum buff count.
     * @return the maximum buff count
     */
    fun getMaxBuffCount(): Int {
        return lock.read {
            return@getMaxBuffCount maxBuffCount
        }
    }

    /**
     * Sets the maximum buff count.
     * @param buffCount the buff count
     */
    fun mergeMaxBuffCount(buffCount: Int) {
        maxBuffCount += buffCount
    }

    fun addToVampiricSum(sum: Double) {
        vampiricSum += sum
    }

    fun getVampiricSum(): Double {
        return lock.read {
            vampiricSum
        }
    }

    /**
     * Expertise of the player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7, R=8, R95=9, R99=10)
     * @return ItemGrade representing expertise level..
     */
    fun getExpertiseLevel(): ItemGrade {
        return expertiseLevel
    }

    fun setExpertiseLevel(crystalType: ItemGrade) {
        expertiseLevel = crystalType
    }


    fun getExpertisePenaltyBonus(): Int {
        return expertisePenaltyBonus
    }

    fun setExpertisePenaltyBonus(bonus: Int) {
        expertisePenaltyBonus = bonus
    }

}