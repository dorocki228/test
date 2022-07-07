package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccountStatisticsResponseFinish extends ReceivablePacket {
    private final LocalDateTime dateTime;

    public AccountStatisticsResponseFinish(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        dateTime = LocalDateTime.parse(readString(byteBuf), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        //StatisticsService.getInstance().saveStatistics(dateTime);
    }
}
