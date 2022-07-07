package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import l2s.gameserver.Config;
import l2s.gameserver.dao.PremiumAccountDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerInGame;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.network.l2.s2c.LoginResultPacket;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.network.l2.s2c.TutorialCloseHtmlPacket;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;
import l2s.gameserver.punishment.PunishmentService;

public class PlayerAuthResponse extends ReceivablePacket {
    private final String account;
    private final boolean authed;
    private int playOkId1;
    private int playOkId2;
    private int loginOkId1;
    private int loginOkId2;
    private int bonus;
    private int bonusExpire;
    private int points;
    private String hwid;

    public PlayerAuthResponse(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
        authed = byteBuf.readUnsignedByte() == 1;
        if (authed) {
            playOkId1 = byteBuf.readInt();
            playOkId2 = byteBuf.readInt();
            loginOkId1 = byteBuf.readInt();
            loginOkId2 = byteBuf.readInt();
            bonus = byteBuf.readInt();
            bonusExpire = byteBuf.readInt();
            points = byteBuf.readInt();
            hwid = readString(byteBuf);
        }
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
        GameClient gameClient = AuthServerClientService.INSTANCE.removeWaitingClient(account);
        if (gameClient == null)
            return;
        if (authed && gameClient.getSessionKey().equals(skey)) {
            if (Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_IP > 0 && AuthServerClientService.INSTANCE.getAuthedClient(account) == null) {
                boolean ignored = Arrays.stream(Config.MAX_ACTIVE_ACCOUNTS_IGNORED_IP)
                        .anyMatch(ignoredIP -> ignoredIP.equalsIgnoreCase(gameClient.getIpAddr()));
                if (!ignored) {
                    int limit = Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_IP;
                    int activeWindows = AuthServerClientService.INSTANCE.getAuthedClientsByIP(gameClient.getIpAddr()).size();
                    if (activeWindows >= limit) {
                        String html = HtmCache.getInstance().getHtml("windows_limit_ip.htm", gameClient.getLanguage());
                        if (html != null) {
                            html = html.replace("<?active_windows?>", String.valueOf(activeWindows));
                            html = html.replace("<?windows_limit?>", String.valueOf(limit));
                            gameClient.close(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.NORMAL_WINDOW, html));
                        } else {
                            gameClient.close(new LoginResultPacket(LoginResultPacket.ACCESS_FAILED_TRY_LATER));
                        }
                        return;
                    }
                }
            }

            if (Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID > 0 && AuthServerClientService.INSTANCE.getAuthedClient(account) == null) {
                int limit2 = Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID;
                int activeWindows2 = AuthServerClientService.INSTANCE.getAuthedClientsByHWID(gameClient.getHwidString()).size();
                if (activeWindows2 >= limit2) {
                    String html2 = HtmCache.getInstance().getHtml("windows_limit_hwid.htm", gameClient.getLanguage());
                    if (html2 != null) {
                        html2 = html2.replace("<?active_windows?>", String.valueOf(activeWindows2));
                        html2 = html2.replace("<?windows_limit?>", String.valueOf(limit2));
                        gameClient.close(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.NORMAL_WINDOW, html2));
                    } else {
                        gameClient.close(new LoginResultPacket(LoginResultPacket.ACCESS_FAILED_TRY_LATER));
                    }
                    return;
                }
            }

            if (PunishmentService.INSTANCE.isPunished(gameClient)) {
                gameClient.close(new LoginResultPacket(LoginResultPacket.ACCESS_FAILED_TRY_LATER));
                return;
            }

            // TODO: for what ?
            if (Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_IP > 0 || Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID > 0)
                gameClient.sendPacket(TutorialCloseHtmlPacket.STATIC);

            gameClient.setAuthed(true);
            gameClient.setState(GameClient.GameClientState.AUTHED);
            if (Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER) {
                int[] bonuses = PremiumAccountDAO.getInstance().select(account);
                bonus = bonuses[0];
                bonusExpire = bonuses[1];
            }
            gameClient.setPremiumAccountType(bonus);
            gameClient.setPremiumAccountExpire(bonusExpire);
            gameClient.setPoints(points);

            GameClient oldClient = AuthServerClientService.INSTANCE.addAuthedClient(gameClient);
            if (oldClient != null) {
                oldClient.setAuthed(false);
                Player activeChar = oldClient.getActiveChar();
                if (activeChar != null) {
                    activeChar.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
                    activeChar.logout();
                } else {
                    oldClient.close(ServerCloseSocketPacket.STATIC);
                }
            }

            client.sendPacket(new PlayerInGame(gameClient.getLogin()));
            CharacterSelectionInfoPacket csi = new CharacterSelectionInfoPacket(gameClient);
            gameClient.sendPacket(LoginResultPacket.GP_STATIC_LOGIN_PACKET, csi);

            gameClient.setCharSelection(csi.getCharInfo());
        } else {
            gameClient.close(new LoginResultPacket(LoginResultPacket.ACCESS_FAILED_TRY_LATER));
        }
    }
}
