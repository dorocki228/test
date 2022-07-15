package l2s.gameserver.network.l2.c2s

import l2s.commons.network.PacketReader
import l2s.gameserver.model.AutoplaySettings
import l2s.gameserver.model.NearTargetMode
import l2s.gameserver.model.NextTargetMode
import l2s.gameserver.network.l2.GameClient

class RequestExAutoplaySetting : IClientIncomingPacket {

    private var autoplaySettings: AutoplaySettings? = null

    override fun readImpl(
        client: GameClient,
        packet: PacketReader
    ): Boolean {
        val settingsSize = packet.readH()
        val enable = packet.readC()
        val pickup = packet.readC()
        val nextTargetMode = NextTargetMode.findByOrdinal(packet.readH()) ?: return false
        val nearTargetMode = NearTargetMode.findByOrdinal(packet.readC().toInt()) ?: return false
        val hpPotionPercent = 80
        val mannerMode = 0.toShort()

        autoplaySettings = AutoplaySettings(
                settingsSize,
                enable,
                pickup,
                nextTargetMode,
                nearTargetMode,
                hpPotionPercent,
                mannerMode
        )

        return true
    }

    override fun run(client: GameClient) {
        val autoplaySetting = autoplaySettings ?: return
        val player = client.activeChar ?: return
        player.ai.setAutoplaySettings(autoplaySettings)
        if (autoplaySetting.enabled()) {
            player.ai.startAutoplay()
        } else {
            player.ai.stopAutoplay()
        }
    }

}