package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.StringTokenizer;

public class AdminBan implements IAdminCommandHandler
{
    private static final Logger LOGGER = LogManager.getLogger(AdminBan.class);

    private enum Commands
    {
        admin_ban,
        admin_ba,
        admin_unban,
        admin_cban,
        admin_chatban,
        admin_chatunban,
        admin_accban,
        admin_accunban,
        admin_ban_hwid,
        admin_trade_ban,
        admin_trade_unban,
        admin_jail,
        admin_unjail,
        admin_permaban,
        admin_removeitems
    }

    @Override
    public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
    {
        Commands command = (Commands) comm;

        StringTokenizer st = new StringTokenizer(fullString);

        if(activeChar.getPlayerAccess().CanTradeBanUnban)
            switch(command)
            {
                case admin_trade_ban:
                    return tradeBan(st, activeChar);
                case admin_trade_unban:
                    return tradeUnban(st, activeChar);
            }

        if(activeChar.getPlayerAccess().CanBan)
            switch(command)
            {
                case admin_ban:
                case admin_ba:
                    ban(st, activeChar);
                    break;
                case admin_accban:
                {
                    st.nextToken();
                    String account = st.nextToken();

                    var endDate = ZonedDateTime.now().plusYears(100);
                    PunishmentService.INSTANCE.addPunishment(PunishmentType.ACCOUNT, account, endDate, activeChar.getName(), "admin ban");
                    GameClient client = AuthServerClientService.INSTANCE.getAuthedClient(account);
                    if(client != null)
                    {
                        Player player = client.getActiveChar();
                        if(player != null)
                        {
                            player.kick();
                            activeChar.sendMessage("Player " + player.getName() + " kicked.");
                        }
                    }
                    break;
                }
                case admin_accunban:
                {
                    st.nextToken();
                    String account = st.nextToken();
                    PunishmentService.INSTANCE.removePunishments(PunishmentType.ACCOUNT, account);
                    break;
                }
                case admin_trade_ban:
                    return tradeBan(st, activeChar);
                case admin_trade_unban:
                    return tradeUnban(st, activeChar);
                case admin_chatban:
                    try
                    {
                        st.nextToken();
                        String player = st.nextToken();
                        String period = st.nextToken();
                        String bmsg = "admin_chatban " + player + " " + period + " ";
                        String msg = fullString.substring(bmsg.length());
                        int obj_id = CharacterDAO.getInstance().getObjectIdByName(player);
                        if(obj_id == 0) {
                            activeChar.sendMessage("Can't find char " + player + ".");
                            return false;
                        }

                        var bannedUntil = ZonedDateTime.now().plus(Long.parseLong(period), ChronoUnit.MINUTES);

                        PunishmentService.INSTANCE.addPunishment(PunishmentType.CHAT, String.valueOf(obj_id), bannedUntil, activeChar.getName(), msg);
                        activeChar.sendMessage("You ban chat for " + player + ".");
                    }
                    catch(Exception e)
                    {
                        activeChar.sendMessage("Command syntax: //chatban char_name period reason");
                    }
                    break;
                case admin_chatunban:
                    try
                    {
                        st.nextToken();
                        String player = st.nextToken();
                        int obj_id = CharacterDAO.getInstance().getObjectIdByName(player);
                        if(obj_id == 0) {
                            activeChar.sendMessage("Can't find char " + player + ".");
                            return false;
                        }

                        PunishmentService.INSTANCE.removePunishments(PunishmentType.CHAT, String.valueOf(obj_id));
                        activeChar.sendMessage("You unban chat for " + player + ".");
                    }
                    catch(Exception e)
                    {
                        activeChar.sendMessage("Command syntax: //chatunban char_name");
                    }
					break;
				case admin_jail:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String period = st.nextToken();
						String reason = st.nextToken();

						Player target = GameObjectsStorage.getPlayer(player);

						if(target != null)
						{
                            target.toJail(Integer.parseInt(period));
                            target.sendMessage("You moved to jail, time to escape - " + period + " minutes, reason - " + reason + " .");
							activeChar.sendMessage("You jailed " + player + ".");
						}
						else
							activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //jail char_name period reason");
					}
                    break;
                case admin_unjail:
                    try
                    {
                        st.nextToken();
                        String player = st.nextToken();

						Player target = GameObjectsStorage.getPlayer(player);

                        if(target != null && target.isInJail())
                        {
                            if(target.fromJail())
                                target.sendMessage("You unjailed " + player + ".");
                            else
                                target.sendMessage("Cannot unjailed " + player + ".");
                        }
                        else
                            activeChar.sendMessage("Can't find char " + player + ".");
                    }
                    catch(Exception e)
                    {
                        activeChar.sendMessage("Command syntax: //unjail char_name");
                    }
                    break;
                case admin_cban:
                    activeChar.sendPacket(new HtmlMessage(5).setFile("admin/cban.htm"));
                    break;
                case admin_permaban: {
                    if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                        activeChar.sendMessage("Target should be set and be a player.");
                        return false;
                    }
                    Player banned = activeChar.getTarget().getPlayer();
                    String banaccount = banned.getAccountName();
                    var endDate = ZonedDateTime.now().plusYears(100);
                    PunishmentService.INSTANCE.addPunishment(PunishmentType.ACCOUNT, banaccount, endDate, activeChar.getName(), "admin ban");
                    if (banned.isInOfflineMode())
                        banned.setOfflineMode(false);
                    banned.kick();
                    activeChar.sendMessage("Player account " + banaccount + " is banned, player " + banned.getName() + " kicked.");
                    break;
                }
                case admin_ban_hwid: {
                    GameObject charTarget = activeChar.getTarget();
                    if (charTarget == null || !charTarget.isPlayer())
                    {
                        activeChar.sendMessage("Target should be set and be a player.");
                        return false;
                    }

                    Player banned = charTarget.getPlayer();
                    String target = banned.getHwidHolder().asString();
                    var endDate = ZonedDateTime.now().plusYears(100);

                    PunishmentService.INSTANCE.addPunishment(PunishmentType.HWID, target, endDate, activeChar.getName(), "admin ban");
                    if (banned.isInOfflineMode())
                        banned.setOfflineMode(false);

                    banned.kick();

                    activeChar.sendMessage("Player hwid " + target + " is banned, player " + banned.getName() + " kicked.");

                    break;
                }
                case admin_removeitems:
                    if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
                    {
                        activeChar.sendMessage("Target should be set and be a player instance");
                        return false;
                    }

                    var banned = activeChar.getTarget().getPlayer();
                    GameClient gameClient = banned.getNetConnection();
                    activeChar.kick();
                    LOGGER.info(PunishmentService.INSTANCE.removeItemsFromAllCharacters(gameClient, new int[]{57}));
                    break;
            }

        return true;
    }

    private boolean tradeBan(StringTokenizer st, Player activeChar)
    {
        if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
            return false;
        st.nextToken();
        Player targ = (Player) activeChar.getTarget();
        long days = -1;
        long time = -1;
        if(st.hasMoreTokens())
        {
            days = Long.parseLong(st.nextToken());
            time = days * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
        }
        targ.setVar("tradeBan", String.valueOf(time), -1);
        String msg = activeChar.getName() + " заблокировал торговлю персонажу " + targ.getName() + (days == -1 ? " на бессрочный период." : " на " + days + " дней.");

        String messagePattern = "{} tradeBan target {}: days {}: trade:{}";
        ParameterizedMessage message = new ParameterizedMessage(messagePattern, activeChar, targ, days,
                tradeToString(targ, targ.getPrivateStoreType()));
        LogService.getInstance().log(LoggerType.ADMIN_ACTIONS, message);

        if(targ.isInOfflineMode())
        {
            targ.setOfflineMode(false);
            targ.kick();
        }
        else if(targ.isInStoreMode())
        {
            targ.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
            targ.standUp();
            targ.broadcastCharInfo();
            targ.getBuyList().clear();
        } else {
            targ.cancelPrivateBuffer();
        }

        if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
            Announcements.getInstance().announceToAll(msg);
        else
            Announcements.shout(activeChar, msg, ChatType.CRITICAL_ANNOUNCE);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static String tradeToString(Player targ, int trade)
    {
        String ret;
        Collection<?> list;
        switch(trade)
        {
            case Player.STORE_PRIVATE_BUY:
                list = targ.getBuyList();
                if(list == null || list.isEmpty())
                    return "";
                ret = ":buy:";
                for(TradeItem i : (Collection<TradeItem>) list)
                    ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
                return ret;
            case Player.STORE_PRIVATE_SELL:
            case Player.STORE_PRIVATE_SELL_PACKAGE:
                list = targ.getSellList();
                if(list == null || list.isEmpty())
                    return "";
                ret = ":sell:";
                for(TradeItem i : (Collection<TradeItem>) list)
                    ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
                return ret;
            case Player.STORE_PRIVATE_MANUFACTURE:
                list = targ.getCreateList();
                if(list == null || list.isEmpty())
                    return "";
                ret = ":mf:";
                for(ManufactureItem i : (Collection<ManufactureItem>) list)
                    ret += i.getRecipeId() + ";" + i.getCost() + ":";
                return ret;
            default:
                return "";
        }
    }

    private boolean tradeUnban(StringTokenizer st, Player activeChar)
    {
        if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
            return false;
        Player targ = (Player) activeChar.getTarget();

        targ.unsetVar("tradeBan");

		if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
            Announcements.getInstance().announceToAll(activeChar + " разблокировал торговлю персонажу " + targ + ".");
        else
            Announcements.shout(activeChar, activeChar + " разблокировал торговлю персонажу " + targ + ".", ChatType.CRITICAL_ANNOUNCE);

        String messagePattern = "{} tradeUnBan target {}";
        ParameterizedMessage message = new ParameterizedMessage(messagePattern, activeChar, targ);
        LogService.getInstance().log(LoggerType.ADMIN_ACTIONS, message);

        return true;
    }

    private boolean ban(StringTokenizer st, Player activeChar)
    {
        try
        {
            st.nextToken();

            String player = st.nextToken();

            int time = 0;

            if(st.hasMoreTokens())
                time = Integer.parseInt(st.nextToken());

            String msg = "";
            if(st.hasMoreTokens())
            {
                msg = "admin_ban " + player + " " + time + " ";
                while(st.hasMoreTokens())
                    msg += st.nextToken() + " ";
                msg.trim();
            }

            int obj_id = CharacterDAO.getInstance().getObjectIdByName(player);
            if(obj_id == 0) {
                activeChar.sendMessage("Can't find char " + player + ".");
                return false;
            }

            var bannedUntil = ZonedDateTime.now().plus(time, ChronoUnit.DAYS);
            PunishmentService.INSTANCE.addPunishment(PunishmentType.CHARACTER, String.valueOf(obj_id), bannedUntil, activeChar.getName(), msg);

			Player plyr = GameObjectsStorage.getPlayer(obj_id);
            if(plyr != null)
            {
                plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM"));
                plyr.kick();
            }

            activeChar.sendMessage("You banned " + plyr.getName());
        }
        catch(Exception e)
        {
            activeChar.sendMessage("Command syntax: //ban char_name days reason");
        }
        return true;
    }

    @Override
    public Enum<?>[] getAdminCommandEnum()
    {
        return Commands.values();
    }
}
