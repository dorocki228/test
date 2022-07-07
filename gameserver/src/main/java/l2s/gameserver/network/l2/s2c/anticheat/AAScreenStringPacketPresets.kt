package l2s.gameserver.network.l2.s2c.anticheat

import l2s.gameserver.Config
import l2s.gameserver.network.l2.s2c.L2GameServerPacket
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacket.Color
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacket.ScreenPos
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacket.WHITE
import java.time.Duration

/**
 * @author Java-man
 * @since 11.05.2019
 */
enum class AAScreenStringPacketPresets(
    private val screenPos: ScreenPos,
    private val offsetX: Int, private val offsetY: Int,
    private val color: Color = WHITE, private val fontSize: Int = 14,
    private val showTime: Duration = Duration.ZERO
) {

    AVAILABLE_EVENTS_HEADER(ScreenPos.TopRight, -10, +250, Color(255, 160, 0)),
    AVAILABLE_EVENTS(ScreenPos.TopRight, -10, +267),
    PROTECT_MESSAGE_HEADER(ScreenPos.TopRight, -10, +550, Color(52, 152, 219)),
    PROTECT_MESSAGE(ScreenPos.TopRight, -10, +565),
    COMBO_KILL(ScreenPos.BottomRight, 0, -145, showTime = Duration.ofSeconds(3)),
    SIEGE_ZONE(ScreenPos.TopRight, -220, 0),
    SIEGE_CRYSTALS(ScreenPos.TopRight, -10, +660),
    ANNOUNCE(ScreenPos.TopCenter, 0, +175, fontSize = 36, showTime = Duration.ofSeconds(5)),
    ARTIFACT_DEFENSE_HEADER(ScreenPos.MiddleRight, 0, -20, showTime = Duration.ofMinutes(5)),
    ARTIFACT_DEFENSE(ScreenPos.MiddleRight, 0, 0, showTime = Duration.ofMinutes(5));

    private val id: Int = ordinal + 1

    @JvmOverloads
    fun addOrUpdate(text: String, newColor: Color = color): L2GameServerPacket? {
        if (!Config.ACTIVE_ANTICHEAT.enableScreenString()) {
            return null
        }

        require(text.isNotEmpty()) { "Text can not be null or empty." }

        require(text.length < 2048) { "Text too long, please specify up to 2048 characters." }

        require(showTime >= Duration.ZERO) { "showTime < 0" }

        return AAScreenStringPacket.addOrEdit(
            id, text, screenPos, offsetX, offsetY, newColor, fontSize, showTime
        )
    }

    fun remove(): L2GameServerPacket? {
        if (!Config.ACTIVE_ANTICHEAT.enableScreenString()) {
            return null
        }

        return AAScreenStringPacket.remove(id)
    }

}