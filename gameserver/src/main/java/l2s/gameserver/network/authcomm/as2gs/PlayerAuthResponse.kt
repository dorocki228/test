package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.gameserver.Config
import l2s.gameserver.dao.HardwareLimitsDAO
import l2s.gameserver.dao.PremiumAccountDAO
import l2s.gameserver.data.htm.HtmCache
import l2s.gameserver.network.authcomm.AuthServerClientService
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.SessionKey
import l2s.gameserver.network.authcomm.gs2as.PlayerInGame
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import l2s.gameserver.network.l2.ConnectionState
import l2s.gameserver.network.l2.GameClient
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.*

class PlayerAuthResponse(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String
    private val authed: Boolean
    private val playOkId1: Int
    private val playOkId2: Int
    private val loginOkId1: Int
    private val loginOkId2: Int
    private var bonus: Int
    private var bonusExpire: Int
    private val points: Int
    private val hwid: String?
    private val phoneNumber: Long

    init {
        account = readString(byteBuf)
        authed = byteBuf.readUnsignedByte() == 1.toShort()
        if (authed) {
            playOkId1 = byteBuf.readInt()
            playOkId2 = byteBuf.readInt()
            loginOkId1 = byteBuf.readInt()
            loginOkId2 = byteBuf.readInt()
            bonus = byteBuf.readInt()
            bonusExpire = byteBuf.readInt()
            points = byteBuf.readInt()
            hwid = readString(byteBuf)
            phoneNumber = if (isReadable)
                byteBuf.readLong()
            else
                0
        } else {
            playOkId1 = 0
            playOkId2 = 0
            loginOkId1 = 0
            loginOkId2 = 0
            bonus = 0
            bonusExpire = 0
            points = 0
            hwid = null
            phoneNumber = 0
        }
    }

    override fun runImpl(client: AuthServerConnection) {
        val skey = SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2)
        val gameClient =
            AuthServerClientService.removeWaitingClient(account) ?: return
        if (authed && gameClient.sessionKey == skey) {
            if (Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_IP > 0
                && AuthServerClientService.getAuthedClient(account) == null
            ) {
                val ignored = Config.MAX_ACTIVE_ACCOUNTS_IGNORED_IP
                    .any { it.equals(gameClient.ipAddr, ignoreCase = true) }
                if (!ignored) {
                    var limit = Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_IP
                    val limits =
                        HardwareLimitsDAO.getInstance().select(gameClient.ipAddr)
                    if (limits[1] == -1 || limits[1] > System.currentTimeMillis() / 1000) limit += limits[0]
                    val clients: MutableList<GameClient> =
                        AuthServerClientService.getAuthedClientsByHWID(gameClient.ipAddr).toMutableList()
                    clients.add(gameClient)
                    for (c in clients) {
                        val limitsByAccount =
                            HardwareLimitsDAO.getInstance().select(c.login)
                        if (limitsByAccount[1] == -1 || limitsByAccount[1] > System.currentTimeMillis() / 1000) limit += limitsByAccount[0]
                    }
                    val activeWindows =
                        AuthServerClientService.getAuthedClientsByIP(gameClient.ipAddr).size
                    if (activeWindows >= limit) {
                        var html: String? = HtmCache.getInstance()
                            .getIfExists("windows_limit_ip.htm", gameClient.language)
                        if (html != null) {
                            html = html.replace("<?active_windows?>", activeWindows.toString())
                            html = html.replace("<?windows_limit?>", limit.toString())
                            gameClient.close(
                                TutorialShowHtmlPacket(
                                    TutorialShowHtmlPacket.NORMAL_WINDOW,
                                    html
                                )
                            )
                        } else gameClient.close(LoginFail.ACCESS_FAILED_TRY_LATER)
                        return
                    }
                }
            }
            if (Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID > 0
                && AuthServerClientService.getAuthedClient(account) == null
            ) {
                var limit = Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID
                val limits =
                    HardwareLimitsDAO.getInstance().select(gameClient.hwid)
                if (limits[1] == -1 || limits[1] > System.currentTimeMillis() / 1000) limit += limits[0]
                val clients: MutableList<GameClient> =
                    AuthServerClientService.getAuthedClientsByHWID(gameClient.hwid).toMutableList()
                clients.add(gameClient)
                for (c in clients) {
                    val limitsByAccount =
                        HardwareLimitsDAO.getInstance().select(c!!.login)
                    if (limitsByAccount[1] == -1 || limitsByAccount[1] > System.currentTimeMillis() / 1000) limit += limitsByAccount[0]
                }
                val activeWindows = clients.size - 1
                if (activeWindows >= limit) {
                    var html: String? = HtmCache.getInstance()
                        .getIfExists("windows_limit_hwid.htm", gameClient.language)
                    if (html != null) {
                        html = html.replace("<?active_windows?>", activeWindows.toString())
                        html = html.replace("<?windows_limit?>", limit.toString())
                        gameClient.close(
                            TutorialShowHtmlPacket(
                                TutorialShowHtmlPacket.NORMAL_WINDOW,
                                html
                            )
                        )
                    } else gameClient.close(LoginFail.ACCESS_FAILED_TRY_LATER)
                    return
                }
            }
            if (Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_IP > 0 || Config.MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID > 0) gameClient.sendPacket(
                TutorialCloseHtmlPacket.STATIC
            )

            gameClient.isAuthed = true
            gameClient.connectionState = ConnectionState.AUTHENTICATED
            gameClient.sendPacket(LoginFail.SUCCESS)

            if (Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER) {
                val bonuses = PremiumAccountDAO.getInstance().select(account)
                bonus = bonuses[0]
                bonusExpire = bonuses[1]
            }
            gameClient.premiumAccountType = bonus
            gameClient.premiumAccountExpire = bonusExpire
            gameClient.points = points
            val oldClient =
                AuthServerClientService.addAuthedClient(gameClient)
            if (oldClient != null) {
                oldClient.isAuthed = false
                oldClient.connectionState = ConnectionState.DISCONNECTED
                val activeChar = oldClient.activeChar
                if (activeChar != null) {
                    //FIXME [G1ta0] сообщение чаще всего не показывается, т.к. при закрытии соединения очередь на отправку очищается
                    activeChar.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT)
                    activeChar.logout(false)
                } else {
                    oldClient.close(ServerCloseSocketPacket.STATIC)
                }
            }
            client.sendPacket(PlayerInGame(gameClient.login))
            val csi = CharacterSelectionInfoPacket(gameClient)
            gameClient.sendPacket(csi)
            gameClient.setCharSelection(csi.charInfo)
            gameClient.checkHwid(hwid)
            gameClient.phoneNumber = phoneNumber
        } else {
            gameClient.close(LoginFail.ACCESS_FAILED_TRY_LATER)
        }
    }
}