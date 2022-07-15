package l2s.authserver.network.gamecomm

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import l2s.commons.net.netty.ReceivablePacket

abstract class ReceivablePacket(
    client: GameServerConnection,
    byteBuf: ByteBuf
) : ReceivablePacket<GameServerConnection>(client, byteBuf)