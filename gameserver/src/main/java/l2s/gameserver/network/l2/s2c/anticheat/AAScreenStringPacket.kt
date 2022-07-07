package l2s.gameserver.network.l2.s2c.anticheat

import l2s.gameserver.network.l2.s2c.L2GameServerPacket
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacket.Color
import java.time.Duration

/**
 * @author Java-man
 * @since 18.04.2019
 *
 * структура пакета:
 *
 * 1 байт - 0xFF
 * 1 байт - 0x05
 * 4 байта - id сообщения
 * 1 байт - id действия ( 0x01 - добавление/редактирование, 0x02 - удаление )
 *
 * если действие - 0x01, то дальше:
 * 1 байт - якорь по оси Х ( 0x02 - по центру, 0x03 - справа, любой другой байт - слева )
 * 1 байт - якорь по оси Y  ( 0x02 - по центру, 0x03 - снизу, любой другой байт - сверху )
 * 4 байта - оффсет по оси X
 * 4 байта - оффсет по оси Y
 * 1 байт - цвет R
 * 1 байт - цвет G
 * 1 байт - цвет B
 * 1 байт - размер текста в пикселях
 * 4 байта - время в миллисекундах, в течении которого игрок будет видеть сообщение
 *   ( если 0 - то будет видеть до того момента как придет пакет на удаление сообщения с этим id )
 * UNICODE строка - само сообщение
 */
object AAScreenStringPacket {

    fun addOrEdit(
        id: Int, text: String, screenPos: ScreenPos, offsetX: Int, offsetY: Int,
        color: Color, fontSize: Int, showTime: Duration
    ): L2GameServerPacket {
        return AAScreenStringPacketAddEdit(
            id, text, screenPos.x, screenPos.y, offsetX, offsetY, color, fontSize,
            showTime.toMillis().toInt()
        )
    }

    fun addOrEdit(
        id: Int, text: String, screenPosX: Int, screenPosY: Int, offsetX: Int, offsetY: Int,
        color: Color, fontSize: Int, showTime: Duration
    ): L2GameServerPacket {
        return AAScreenStringPacketAddEdit(
            id, text, screenPosX, screenPosY, offsetX, offsetY, color, fontSize,
            showTime.toMillis().toInt()
        )
    }

    fun remove(id: Int): L2GameServerPacket {
        return AAScreenStringPacketRemove(id)
    }

    enum class ScreenPos(val x: Int, val y: Int) {
        TopRight(3, 1),
        TopLeft(1, 1),
        TopCenter(2, 1),
        MiddleRight(3, 2),
        MiddleLeft(1, 2),
        MiddleCenter(2, 2),
        BottomRight(3, 3),
        BottomLeft(1, 3),
        BottomCenter(2, 3)
    }

    data class Color(val r: Int, val g: Int, val b: Int) {
        init {
            require(r in 0..255)
            require(g in 0..255)
            require(b in 0..255)
        }
    }

    val WHITE = Color(255, 255, 255)

}

private const val OPCODE = 0xFF
private const val SUBCODE = 0x05

private enum class Action(val id: Int) {
    ADD_EDIT(1),
    REMOVE(2)
}

private class AAScreenStringPacketAddEdit(
    private val id: Int, private val text: String, private val screenPosX: Int, private val screenPosY: Int,
    private val offsetX: Int, private val offsetY: Int, private val color: Color, private val fontSize: Int,
    private val showTime: Int
) : L2GameServerPacket() {

    override fun writeOpcodes(): Boolean {
        writeC(OPCODE)
        writeC(SUBCODE)

        return true
    }

    override fun writeImpl() {
        writeD(id)
        writeC(Action.ADD_EDIT.id)

        writeC(screenPosX)
        writeC(screenPosY)

        writeD(offsetX)
        writeD(offsetY)

        writeC(color.r)
        writeC(color.g)
        writeC(color.b)

        writeC(fontSize)

        writeD(showTime)

        writeS(text)
    }

}

private class AAScreenStringPacketRemove(private val id: Int) : L2GameServerPacket() {

    override fun writeOpcodes(): Boolean {
        writeC(OPCODE)
        writeC(SUBCODE)

        return true
    }

    override fun writeImpl() {
        writeD(id)
        writeC(Action.REMOVE.id)
    }

}
