package as.handlers;

import as.ErrorConstants;
import com.mmobite.admin.model.packet.ReadPacket;
import com.mmobite.admin.model.server.ITcpServer;
import com.mmobite.admin.packets.OpcodeCS;
import io.netty.channel.ChannelHandlerContext;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.AdminJobLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckPlayerOnlinePacket extends ReadPacket
{

    private static Logger log = LoggerFactory.getLogger(CheckPlayerOnlinePacket.class.getName());

    private String admin_name_;
    private int objId;

    @Override
    public boolean read()
    {
        objId = readD();
        admin_name_ = readS();

        return true;
    }

    @Override
    public void run(ITcpServer server, ChannelHandlerContext ctx)
    {
        boolean resultOnline = GameObjectsStorage.getPlayer(objId) != null;

        var message = new AdminJobLogMessage(getClass().getSimpleName(), admin_name_, String.format("[objId=%s,resultOnline=%s]", objId, resultOnline));
        LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

        if(!resultOnline)
        {
            server.replyError(ctx, getOpcode(), ErrorConstants.RESULT_CHAR_NOT_LOGIN);
        }

        server.replyOk(ctx, getOpcode());
    }

    @Override
    public int getOpcode()
    {
        return OpcodeCS.CheckVersion;
    }
}
