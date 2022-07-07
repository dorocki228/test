package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.SendablePacket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class AccountStatisticsResponseFinish extends SendablePacket {
    private final LocalDateTime dateTime;

    public AccountStatisticsResponseFinish(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 8;
    }
}
