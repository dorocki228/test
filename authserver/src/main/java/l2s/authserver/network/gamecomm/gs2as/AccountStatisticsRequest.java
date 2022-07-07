package l2s.authserver.network.gamecomm.gs2as;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.AccountStatistics;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.as2gs.AccountStatisticsResponse;
import l2s.authserver.network.gamecomm.as2gs.AccountStatisticsResponseFinish;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import l2s.authserver.service.AccountsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class AccountStatisticsRequest extends ReceivablePacket {
    private final LocalDateTime dateTime;

    public AccountStatisticsRequest(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);
        dateTime = LocalDateTime.parse(readString(byteBuf), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        List<AccountStatistics> accountStatistics = AccountsService.getInstance().getAccountStatistics();
        List<List<AccountStatistics>> partitions = Lists.partition(accountStatistics, 500);
        partitions.forEach(list -> client.sendPacket(new AccountStatisticsResponse(dateTime, list)));
        client.sendPacket(new AccountStatisticsResponseFinish(dateTime));

        LOGGER.info("Sent {} account statistics for client {}.", accountStatistics.size(), client.getRemoteAddress());
    }
}
