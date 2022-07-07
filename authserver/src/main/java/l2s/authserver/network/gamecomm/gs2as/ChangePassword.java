package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.Config;
import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.as2gs.ChangePasswordResponse;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import l2s.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangePassword extends ReceivablePacket {
    private final String _accname;
    private final String _oldPass;
    private final String _newPass;
    private final String _hwid;

    public ChangePassword(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _accname = readString(byteBuf);
        _oldPass = readString(byteBuf);
        _newPass = readString(byteBuf);
        _hwid = readString(byteBuf);
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        String dbPassword = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            try {
                statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
                statement.setString(1, _accname);
                rs = statement.executeQuery();
                if (rs.next())
                    dbPassword = rs.getString("password");
            } catch (Exception e) {
                LOGGER.warn("Can't receive old password for account {}", _accname, e);
            } finally {
                DbUtils.closeQuietly(statement, rs);
            }
            try {
                if (!Config.DEFAULT_CRYPT.compare(_oldPass, dbPassword)) {
                    ChangePasswordResponse cp1 = new ChangePasswordResponse(_accname, false);
                    client.sendPacket(cp1);
                } else {
                    statement = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?");
                    statement.setString(1, Config.DEFAULT_CRYPT.encrypt(_newPass));
                    statement.setString(2, _accname);
                    int result = statement.executeUpdate();
                    ChangePasswordResponse cp2 = new ChangePasswordResponse(_accname, result != 0);
                    client.sendPacket(cp2);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            } finally {
                DbUtils.closeQuietly(statement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con);
        }
    }
}
