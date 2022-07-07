package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.gs2as.SetAccountInfo;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetAccountInfo extends ReceivablePacket {
    private final String _account;

    public GetAccountInfo(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _account = readString(byteBuf);
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        int playerSize = 0;
        List<Integer> deleteChars = Collections.emptyList();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?");
            statement.setString(1, _account);
            rset = statement.executeQuery();
            while (rset.next()) {
                ++playerSize;
                int d = rset.getInt("deletetime");
                if (d > 0) {
                    if (deleteChars.isEmpty())
                        deleteChars = new ArrayList<>(3);
                    deleteChars.add(d + Config.CHARACTER_DELETE_AFTER_HOURS * 60 * 60);
                }
            }
        } catch (Exception e) {
            LOGGER.error("GetAccountInfo:run()", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        GameServer.getInstance().getAuthServerCommunication().sendPacket(new SetAccountInfo(_account, playerSize, deleteChars));
    }
}
