package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import l2s.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class LockAccountIP extends ReceivablePacket {
    private final String _accname;
    private final String _IP;
    private final int _time;

    public LockAccountIP(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _accname = readString(byteBuf);
        _IP = readString(byteBuf);
        _time = byteBuf.readInt();
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE accounts SET allow_ip = ?, lock_expire = ? WHERE login = ?");
            statement.setString(1, _IP);
            statement.setInt(2, _time);
            statement.setString(3, _accname);
            statement.executeUpdate();
            DbUtils.closeQuietly(statement);
        } catch (Exception e) {
            LOGGER.error("Failed to lock/unlock account.", e);
        } finally {
            DbUtils.closeQuietly(con);
        }
    }
}
