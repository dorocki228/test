package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;
import l2s.gameserver.statistics.AccountStatistics;
import l2s.gameserver.statistics.StatisticsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccountStatisticsResponse extends ReceivablePacket {
    private final LocalDateTime dateTime;
    private final List<AccountStatistics> accountStatistics;

    public AccountStatisticsResponse(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        dateTime = LocalDateTime.parse(readString(byteBuf), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        int initialCapacity = byteBuf.readInt();
        accountStatistics = new ArrayList<>(initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            AccountStatistics stats = new AccountStatistics(readString(byteBuf), readString(byteBuf),
                    readString(byteBuf), byteBuf.readInt());
            accountStatistics.add(stats);
        }
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        StatisticsService.getInstance().handleResponse(dateTime, accountStatistics);
    }
}
