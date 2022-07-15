package l2s.gameserver.network.authcomm

import io.netty.buffer.ByteBuf
import l2s.commons.net.netty.ReceivablePacket
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection

abstract class ReceivablePacket(
    client: AuthServerConnection,
    byteBuf: ByteBuf
) : ReceivablePacket<AuthServerConnection>(
    client,
    byteBuf
)