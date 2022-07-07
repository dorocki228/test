package l2s.commons.bitmask

/**
 * @author Java-man
 * @since 21.05.2019
 */
interface Flags {

    val bit: Int

    fun toBitMask(): BitMask = BitMask(bit)

}