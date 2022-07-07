package as;

import as.dao.CharacterDAO;
import as.handlers.CheckPlayerOnlinePacket;
import as.handlers.SetBanCharacterPacket;
import com.mmobite.admin.packets.PacketManager;
import com.mmobite.admin.server.AdminServer;
import com.mmobite.api.AdminActionImpl;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.AdminJobLogMessage;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import l2s.gameserver.utils.TradeHelper;
import smartguard.api.detection.DetectAction;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Mangol
 * @author Java-man
 */
public class AdminJobServerInitializer
{
    public static void start()
    {
        PacketManager.addPacket(1, CheckPlayerOnlinePacket.class);
        PacketManager.addPacket(39, SetBanCharacterPacket.class);

        AdminServer.init("config/antispam/admin_job.properties", new AdminActionImplImpl());
    }

    private static class AdminActionImplImpl implements AdminActionImpl
    {
        private final PunishmentService banManager = PunishmentService.INSTANCE;

        @Override
        public boolean kickPlayer(int char_id, String admin_name)
        {
            Player player = GameObjectsStorage.getPlayer(char_id);

            var message = new AdminJobLogMessage(getClass().getSimpleName(), admin_name,
                    String.format("[objId=%s]", char_id));
            LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

            if(player == null)
                return false;

            player.kick();

            return true;
        }

        @Override
        public boolean deleteUserPost(int char_id, int account_id, String admin_name)
        {
            return false;
        }

        @Override
        public boolean punishChar(int char_id, int punish_type, int time, String admin_name)
        {
            boolean success = false;
            if(time > 0)
                success = chatBan(char_id, time, admin_name);
            else if(time == 0)
                success = chatUnBan(char_id, admin_name);

            var message = new AdminJobLogMessage(getClass().getSimpleName(), "AntiSpam",
                    String.format("[char_id=%s,time=%s,admin_name=%s,success=%s]", char_id, time, admin_name, success));
            LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

            return success;
        }

        @Override
        public boolean sendMessageToGame(int msg_id, String[] s_params_, int time_, int delay_, String admin_name)
        {
            if(msg_id == 3)
            {
                String hwid = s_params_[0].toLowerCase();
                String account = s_params_[1];
                PunishType punishType = PunishType.valueOf(s_params_[2]);
                String comment = s_params_[3];

                var message = new AdminJobLogMessage(getClass().getSimpleName(), "AntiSpam",
                        String.format("[id=%s,hwid=%s,account=%s,punishType=%s,time=%s,comment=%s]",
                                3, hwid, account, punishType.name(), time_, delay_));
                LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

                return hwid != null && addBan(hwid, account, punishType, time_, delay_, comment);
            }

            if(msg_id == 4)
            {
                String hwid = s_params_[0].toLowerCase();
                String account = s_params_[1];

                banManager.removePunishments(PunishmentType.HWID, hwid);

                GameClient client = AuthServerClientService.INSTANCE.getAuthedClient(account);
                if(client != null)
                {
                    Player player = client.getActiveChar();
                    if(player != null)
                    {
                        player.kick();
                    }
                }

                var message = new AdminJobLogMessage(getClass().getSimpleName(), "AntiSpam",
                        String.format("[id=%s,hwid=%s,account=%s]", 4, hwid, account));
                LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

                return true;
            }

            if(msg_id == 8)
            {
                String account = s_params_[0];
                String comment = s_params_[1];

                var endDate = ZonedDateTime.now().plusYears(100);
                banManager.addPunishment(PunishmentType.ACCOUNT, account, endDate, "AntiSpam", comment);

                GameClient client = AuthServerClientService.INSTANCE.getAuthedClient(account);
                if(client != null)
                {
                    Player player = client.getActiveChar();
                    if(player != null)
                    {
                        player.kick();
                    }
                }

                var message = new AdminJobLogMessage(getClass().getSimpleName(), "AntiSpam",
                        String.format("[id=%s,account=%s,comment=%s]", 8, account, comment));
                LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

                return true;
            }

            if(msg_id == 9)
            {
                String account = s_params_[0];
                String comment = s_params_[1];

                banManager.removePunishments(PunishmentType.ACCOUNT, account);

                var message = new AdminJobLogMessage(getClass().getSimpleName(), "AntiSpam",
                        String.format("[id=%s,account=%s,comment=%s]", 9, account, comment));
                LogService.getInstance().log(LoggerType.ADMIN_JOBS, message);

                return true;
            }

            return false;
        }

        private boolean addBan(String hwid, String account, PunishType punishType, int timeMins, int delay, String comment)
        {
            switch(punishType)
            {
                case TEMPORARY:
                {
                    if(timeMins <= 0)
                        return false;

                    var bannedUntil = ZonedDateTime.now().plus(timeMins, ChronoUnit.MINUTES);
                    banManager.addPunishment(PunishmentType.HWID, hwid, bannedUntil, "AntiSpam", comment);

                    return true;
                }
                case DELAYED:
                {
                    if(delay <= 0)
                        return false;

                    /* TODO: delayed ban
                    IBan ban = banManager.getBanManager().getDelayedBan(hwid);
                    if(ban == null)
                    {
                        ban = new BanParser.Ban(hwid, comment);
                        banManager.getBanManager().addBanDelayed(ban, TimeUnit.MINUTES.toMillis(delay));
                        return true;
                    }

                    return false;*/

                    var endDate = ZonedDateTime.now().plusYears(100);
                    banManager.addPunishment(PunishmentType.HWID, hwid, endDate, "AntiSpam", comment);

                    return true;
                }
                case NORMAL:
                {
                    var endDate = ZonedDateTime.now().plusYears(100);
                    banManager.addPunishment(PunishmentType.HWID, hwid, endDate, "AntiSpam", comment);

                    return true;
                }
                case TEMPORARY_DELAYED:
                    if(timeMins <= 0 || delay <= 0)
                        return false;
                    /* TODO: delayed temporary ban
                    IBan ban = banManager.getBanManager().getDelayedBan(hwid);
                    if(ban == null)
                    {
                        long bannedUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(timeMins) + TimeUnit.MINUTES.toMillis(delay);
                        ban = new BanParser.Ban(hwid, bannedUntil, comment);
                        banManager.getBanManager().addBanDelayed(ban, TimeUnit.MINUTES.toMillis(delay));
                        return true;
                    }
                    return false;*/

                    var bannedUntil = ZonedDateTime.now().plus(timeMins, ChronoUnit.MINUTES);
                    banManager.addPunishment(PunishmentType.HWID, hwid, bannedUntil, "AntiSpam", comment);

                    return true;
            }
            return false;
        }

        private static boolean chatBan(int objId, int period, String admin)
        {
            boolean valid = CharacterDAO.isValidCharacter(objId);
            if(!valid)
                return false;

            var bannedUntil = ZonedDateTime.now().plus(period, ChronoUnit.MINUTES);
            PunishmentService.INSTANCE.addPunishment(PunishmentType.CHAT, String.valueOf(objId), bannedUntil, "AntiSpam", admin);

            Player player = GameObjectsStorage.getPlayer(objId);
            if(player != null)
            {
                player.sendMessage(new CustomMessage("l2p.Util.AutoBan.ChatBan").addString(admin).addNumber(period));
                if(player.isInStoreMode())
                    TradeHelper.cancelStore(player);
                player.cancelPrivateBuffer();
            }

            return true;
        }

        private static boolean chatUnBan(int objId, String admin)
        {
            boolean valid = CharacterDAO.isValidCharacter(objId);
            if(!valid)
                return false;
            Player player = GameObjectsStorage.getPlayer(objId);
            if(player != null)
            {
                player.sendMessage(new CustomMessage("l2p.Util.AutoBan.ChatUnBan").addString(admin));
            }

            PunishmentService.INSTANCE.removePunishments(PunishmentType.CHAT, String.valueOf(objId));

            return true;
        }
    }

    public enum PunishType {
        NORMAL(10, DetectAction.BAN, DetectAction.LOG),
        TEMPORARY(9, DetectAction.BAN),
        TEMPORARY_PROGRESSIVE(8, DetectAction.BAN),
        TEMPORARY_DELAYED(7, DetectAction.BAN),
        DELAYED(6, DetectAction.BAN);

        private final int priority;
        private final DetectAction[] allowedActions;

        PunishType(int p, DetectAction... actions) {
            priority = p;
            allowedActions = actions;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isActionAllowed(DetectAction action) {
            DetectAction[] var2 = allowedActions;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                DetectAction a = var2[var4];
                if (a == action) {
                    return true;
                }
            }

            return false;
        }
    }

}
