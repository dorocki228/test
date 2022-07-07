package l2s.commons.bitmask

/**
 * @author Java-man
 * @since 21.05.2019
 */
infix fun Flags.and(other: Int): BitMask = BitMask(bit and other)

infix fun <T : Flags> Flags.or(other: T): BitMask = BitMask(bit or other.bit)

infix operator fun Flags.plus(other: Flags): BitMask = BitMask(bit or other.bit)

inline fun <reified T> enabledValues(mask: BitMask): List<T> where T : Enum<T>, T : Flags {
    return enumValues<T>().filter {
        mask hasFlag it
    }
}

infix fun BitMask.or(other: Flags): BitMask = BitMask(value or other.bit)

infix operator fun BitMask.plus(other: BitMask): BitMask = BitMask(value or other.value)
infix operator fun BitMask.plus(other: Flags): BitMask = BitMask(value or other.bit)

infix fun <T : Flags> BitMask.hasFlag(which: T): Boolean {
    // an Undefined flag is a special case.
    if (value == 0 || (value > 0 && which.bit == 0)) return false

    return value and which.bit == which.bit
}

infix fun <T : Flags> BitMask.unset(which: T): BitMask = BitMask(value xor which.bit)