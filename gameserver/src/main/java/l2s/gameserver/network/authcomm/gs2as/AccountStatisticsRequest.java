package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class AccountStatisticsRequest extends SendablePacket {
    private final LocalDateTime dateTime;

    public AccountStatisticsRequest(LocalDateTime next) {
        dateTime = next;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 19;
    }
}
