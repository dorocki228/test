package l2s.gameserver.punishment

import l2s.gameserver.GameServer
import l2s.gameserver.model.GameObjectsStorage
import l2s.gameserver.model.Player
import l2s.gameserver.network.authcomm.AuthServerClientService
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel
import l2s.gameserver.network.l2.components.CustomMessage
import l2s.gameserver.network.l2.s2c.EtcStatusUpdatePacket
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket
import java.time.Duration
import java.time.ZonedDateTime

/*
 * @author Java-man
 * @since 07.06.2019
 */
enum class PunishmentType {
    ACCOUNT {
        override fun extractPunishmentTarget(player: Player): String = player.accountName

        override fun onAddition(
            target: String,
            endDate: ZonedDateTime,
            punishedBy: String,
            reason: String
        ) {
            val message = ChangeAccessLevel(target, -100, 0, reason)
            GameServer.getInstance().authServerCommunication.sendPacket(message)

            val client = AuthServerClientService.getAuthedClient(target)
            if (client != null) {
                val player = client.activeChar
                if (player != null)
                    player.kick()
                else {
                    println("[GameClient.close] PunishmentType: on addition account")
                    client.close(ServerCloseSocketPacket.STATIC)
                }
            }
        }

        override fun onRemoval(target: String) {
            GameServer.getInstance().authServerCommunication.sendPacket(ChangeAccessLevel(target, 0, 0, ""))
        }
    },
    HWID {
        override fun extractPunishmentTarget(player: Player): String = player.hwidHolder.asString()

        override fun onAddition(
            target: String,
            endDate: ZonedDateTime,
            punishedBy: String,
            reason: String
        ) {
            val clients = AuthServerClientService.getAuthedClientsByHWID(target)
            clients.forEach {
                val player = it.activeChar
                if (player != null)
                    player.kick()
                else {
                    println("[GameClient.close] PunishmentType: on addition hwid")
                    it.close(ServerCloseSocketPacket.STATIC)
                }
            }
        }

        override fun onRemoval(target: String) {
        }
    },
    IP {
        override fun extractPunishmentTarget(player: Player): String = player.ip

        override fun onAddition(
            target: String,
            endDate: ZonedDateTime,
            punishedBy: String,
            reason: String
        ) {
            val clients = AuthServerClientService.getAuthedClientsByIP(target)
            clients.forEach {
                val player = it.activeChar
                if (player != null)
                    player.kick()
                else {
                    println("[GameClient.close] PunishmentType: on addition ip")
                    it.close(ServerCloseSocketPacket.STATIC)
                }
            }
        }

        override fun onRemoval(target: String) {
        }
    },
    CHARACTER {
        override fun extractPunishmentTarget(player: Player): String = player.objectId.toString()

        override fun onAddition(
            target: String,
            endDate: ZonedDateTime,
            punishedBy: String,
            reason: String
        ) {
            val player = GameObjectsStorage.getPlayer(target.toInt())
            player?.kick()
        }

        override fun onRemoval(target: String) {
        }
    },
    CHAT {
        override fun extractPunishmentTarget(player: Player): String = player.objectId.toString()

        override fun onAddition(
            target: String,
            endDate: ZonedDateTime,
            punishedBy: String,
            reason: String
        ) {
            val player = GameObjectsStorage.getPlayer(target.toInt())
            if (player != null) {
                player.sendPacket(EtcStatusUpdatePacket(player))
                val period = Duration.between(ZonedDateTime.now(), endDate).toMinutes()
                player.sendMessage(CustomMessage("l2s.Util.AutoBan.ChatBan").addString(punishedBy).addNumber(period))
            }
        }

        override fun onRemoval(target: String) {
        }
    };

    abstract fun extractPunishmentTarget(player: Player): String

    abstract fun onAddition(
        target: String,
        endDate: ZonedDateTime,
        punishedBy: String,
        reason: String
    )

    abstract fun onRemoval(
        target: String
    )
}