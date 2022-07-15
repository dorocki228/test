package l2s.gameserver.model

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import l2s.gameserver.skills.AbnormalType

/**
 * @author Java-man
 * @since 09.09.2019
 */
class AbnormalTypeList private constructor(
    private val abnormalTypes: Collection<AbnormalType>
) {

    val isNone: Boolean = abnormalTypes.isEmpty()
    private val clientIds = abnormalTypes.map { it.clientId }
        .distinct()
        .ifEmpty { emptyClientIds }

    fun contains(abnormalType: AbnormalType): Boolean {
        return abnormalTypes.contains(abnormalType)
    }

    fun any(predicate: (AbnormalType) -> Boolean): Boolean {
        return abnormalTypes.any { predicate(it) }
    }

    inline fun containsAnyOf(collection: Collection<AbnormalType>): Boolean {
        return any { collection.contains(it) }
    }

    inline fun containsAnyOf(typeList: AbnormalTypeList): Boolean {
        return any { typeList.contains(it) }
    }

    fun all(predicate: (AbnormalType) -> Boolean): Boolean {
        return abnormalTypes.all { predicate(it) }
    }

    fun forEachClientId(action: (Int) -> Unit) {
        clientIds.forEach { action(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbnormalTypeList) return false

        if (abnormalTypes != other.abnormalTypes) return false

        return true
    }

    override fun hashCode(): Int {
        return abnormalTypes.hashCode()
    }

    companion object {

        private val emptyClientIds = arrayOf(AbnormalType.NONE).map { it.clientId }

        fun parse(str: String): AbnormalTypeList {
            val set: Set<AbnormalType> = str.split(";")
                .map {
                    AbnormalType.valueOf(it.toUpperCase())
                }.let {
                    if (it.all { type -> type == AbnormalType.NONE })
                        ImmutableSet.of()
                    else
                        Sets.immutableEnumSet(it)
                }
            return AbnormalTypeList(set)
        }

    }

}