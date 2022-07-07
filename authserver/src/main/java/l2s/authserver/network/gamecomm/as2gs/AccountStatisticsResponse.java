package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.AccountStatistics;
import l2s.authserver.network.gamecomm.SendablePacket;

import java.nio.BufferUnderflowException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class AccountStatisticsResponse extends SendablePacket {
    private final LocalDateTime dateTime;
    private final List<AccountStatistics> accountStatistics;

    public AccountStatisticsResponse(LocalDateTime dateTime, List<AccountStatistics> accountStatistics) {
        this.dateTime = dateTime;
        this.accountStatistics = accountStatistics;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        byteBuf.writeInt(accountStatistics.size());
        try {
            accountStatistics.forEach(entry -> {
                writeString(byteBuf, entry.getLogin());
                writeString(byteBuf, entry.getEmail());
                writeString(byteBuf, entry.getSource());
                byteBuf.writeInt(entry.getLoggedInCount());
            });
        } catch (BufferUnderflowException e) {
            LOGGER.error("Can't send {} account statistics.", accountStatistics, e);
        }
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 7;
    }
}
