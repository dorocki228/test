package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangeAllowedHwid extends ReceivablePacket {
    private final String account;
    private final String hwid;

    public ChangeAllowedHwid(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
        hwid = readString(byteBuf);
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        Connection con = null;
        PreparedStatement statement;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE accounts SET allow_hwid=? WHERE login=?");
            statement.setString(1, hwid);
            statement.setString(2, account);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            LOGGER.warn("ChangeAllowedIP: Could not write data.", e);
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

}
