package as.handlers;

import as.ErrorConstants;
import as.dao.CharacterDAO;
import com.mmobite.admin.model.packet.ReadPacket;
import com.mmobite.admin.model.server.ITcpServer;
import io.netty.channel.ChannelHandlerContext;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.AdminJobLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class SetBanCharacterPacket extends ReadPacket
{
    private String admin;
    private int objId;
    private int hours;

    @Override
    public boolean read()
    {
        objId = readD();
        hours = readD();
        admin = readS();

        return true;
    }

    @Override
    public void run(ITcpServer server, ChannelHandlerContext ctx)
    {
        var message = new AdminJobLogMessage(getClass().getSimpleName(), admin, String.format("[objId=%s,hours=%s]", objId, hours));
        LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

        boolean validChar = CharacterDAO.isValidCharacter(objId);
        if(!validChar)
        {
            server.replyError(ctx, getOpcode(), ErrorConstants.RESULT_ERROR);
        }
        else
        {
            Player player = GameObjectsStorage.getPlayer(objId);
            if(player != null)
                player.logout();

            var bannedUntil = ZonedDateTime.now().plus(hours, ChronoUnit.HOURS);
            PunishmentService.INSTANCE.addPunishment(PunishmentType.CHARACTER, String.valueOf(objId), bannedUntil, admin, "SetBanCharacterPacket");
            server.replyOk(ctx, getOpcode());
        }
    }

    @Override
    public int getOpcode()
    {
        return 39;
    }
}
