package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Functions;

public class ChangePasswordResponse extends ReceivablePacket {
    private final String _account;
    private final boolean _changed;

    public ChangePasswordResponse(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _account = readString(byteBuf);
        _changed = byteBuf.readInt() == 1;
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        GameClient gameClient = AuthServerClientService.INSTANCE.getAuthedClient(_account);
        if (gameClient == null)
            return;
        Player activeChar = gameClient.getActiveChar();
        if (activeChar == null)
            return;
        if (_changed)
            Functions.show(new CustomMessage("scripts.commands.user.password.ResultTrue"), activeChar);
        else
            Functions.show(new CustomMessage("scripts.commands.user.password.ResultFalse"), activeChar);
    }
}
