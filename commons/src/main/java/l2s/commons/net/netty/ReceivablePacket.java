package l2s.commons.net.netty;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public abstract class ReceivablePacket<T> implements Runnable {
    protected static final Logger LOGGER = LogManager.getLogger(ReceivablePacket.class);

    private final T client;
    private final ByteBuf byteBuf;

    public ReceivablePacket(T client, ByteBuf byteBuf) {
        this.client = client;
        this.byteBuf = byteBuf;
    }

    public boolean isReadable() {
        return byteBuf.isReadable();
    }

    protected String readString(ByteBuf byteBuf) {
        var length = byteBuf.readShort();
        return byteBuf.readCharSequence(length, StandardCharsets.UTF_8).toString();
    }

    @Override
    public final void run() {
        try {
            runImpl(client);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    protected abstract void runImpl(T client);
}
