package l2s.commons.net.netty;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public abstract class SendablePacket {
    protected static final Logger LOGGER = LogManager.getLogger(SendablePacket.class);

    protected void writeString(ByteBuf byteBuf, CharSequence charSequence) {
        byteBuf.writeShort(charSequence.length());
        byteBuf.writeCharSequence(charSequence, StandardCharsets.UTF_8);
    }

    public ByteBuf write(ByteBuf byteBuf) {
        try {
            byteBuf.writeByte(getOpCode());
            return writeImpl(byteBuf);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
        }

        return null;
    }

    protected abstract ByteBuf writeImpl(ByteBuf byteBuf);

    protected abstract byte getOpCode();
}
