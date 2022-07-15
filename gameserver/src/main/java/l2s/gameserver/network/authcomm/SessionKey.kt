package l2s.gameserver.network.authcomm

/**
 *
 * This class is used to represent session keys used by the client to authenticate in the gameserver
 *
 * A SessionKey is made up of two 8 bytes keys. One is send in the LoginOk packet and the other is sent in PlayOk
 *
 * @author -Wooden-
 */
class SessionKey(val loginOkID1: Int, val loginOkID2: Int, val playOkID1: Int, val playOkID2: Int) {

    private val hashCode: Int

    init {
        var hashCode = playOkID1
        hashCode *= 17
        hashCode += playOkID2
        hashCode *= 37
        hashCode += loginOkID1
        hashCode *= 51
        hashCode += loginOkID2
        this.hashCode = hashCode
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        if (o.javaClass == this.javaClass) {
            val skey = o as SessionKey
            return playOkID1 == skey.playOkID1 && playOkID2 == skey.playOkID2 && loginOkID1 == skey.loginOkID1 && loginOkID2 == skey.loginOkID2
        }
        return false
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun toString(): String {
        return StringBuilder().append("[playOkID1: ").append(playOkID1).append(" playOkID2: ")
            .append(playOkID2).append(" loginOkID1: ").append(loginOkID1).append(" loginOkID2: ").append(loginOkID2)
            .append("]").toString()
    }

}