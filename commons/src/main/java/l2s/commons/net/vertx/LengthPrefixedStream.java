package l2s.commons.net.vertx;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.streams.ReadStream;

public class LengthPrefixedStream implements ReadStream<Buffer> {
    private final RecordParser recordParser;

    private FrameToken expectedToken = FrameToken.SIZE;

    public LengthPrefixedStream(ReadStream<Buffer> stream) {
        recordParser = RecordParser.newFixed(4, stream);
    }

    @Override
    public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        recordParser.exceptionHandler(handler);
        return this;
    }

    @Override
    public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        if (handler == null) {
            recordParser.handler(null);
            return this;
        }

        recordParser.handler(buffer -> {
            switch (expectedToken) {
                case SIZE:
                    int frameSize = buffer.getInt(0);
                    recordParser.fixedSizeMode(frameSize);
                    expectedToken = FrameToken.PAYLOAD;
                    break;
                case PAYLOAD:
                    recordParser.fixedSizeMode(4);
                    expectedToken = FrameToken.SIZE;
                    handler.handle(buffer);
                    break;
            }
        });

        return this;
    }

    @Override
    public ReadStream<Buffer> pause() {
        recordParser.pause();
        return this;
    }

    @Override
    public ReadStream<Buffer> resume() {
        recordParser.resume();
        return this;
    }

    @Override
    public ReadStream<Buffer> fetch(long amount) {
        recordParser.fetch(amount);
        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        recordParser.endHandler(endHandler);
        return this;
    }
}