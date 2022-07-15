package smartguard.integration;

import l2s.commons.network.IConnectionState;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.ConnectionState;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import smartguard.api.entity.IHwidEntity;
import smartguard.api.integration.AbstractSmartClient;
import smartguard.api.integration.ISmartPlayer;
import smartguard.api.network.NetworkStatus;
import smartguard.packet.RawPacket;

import java.nio.ByteBuffer;

public class SmartClient extends AbstractSmartClient {

    private GameClient client;

    public SmartClient(GameClient client) {
        this.client = client;
    }

    @Override
    public String getAccountName() {
        return client.getLogin();
    }

    @Override
    public void closeConnection(boolean sendServerClose, boolean defer) {
        if(sendServerClose)
            client.close(ServerCloseSocketPacket.STATIC);
        //else if(defer)
        //    client.closeLater();
        else
            client.closeNow();
    }

    @Override
    public NetworkStatus getConnectionStatus() {
        if (!client.isConnected())
            return NetworkStatus.DISCONNECTED;

        IConnectionState connectionState = client.getConnectionState();
        if (ConnectionState.CONNECTED.equals(connectionState) || ConnectionState.AUTHENTICATED.equals(connectionState)) {
            return NetworkStatus.CONNECTED;
        } else if (ConnectionState.IN_GAME.equals(connectionState)) {
            return NetworkStatus.IN_GAME;
        }
        return NetworkStatus.DISCONNECTED;
    }

    @Override
    public void setHwid(IHwidEntity iHwidEntity) {
        client.setHWID(iHwidEntity.getPlain());
    }

    @Override
    public void sendRawPacket(ByteBuffer byteBuffer) {
        client.sendPacket(new RawPacket(byteBuffer));
    }

    @Override
    public void closeWithRawPacket(ByteBuffer byteBuffer) {
        client.close(new RawPacket(byteBuffer));
    }

    @Override
    public void sendHtml(String s) {
        HtmlMessage html = new HtmlMessage(5);
        html.setHtml(s);
        Player activeChar = client.getActiveChar();
        if (activeChar != null)
            activeChar.sendPacket(html);
    }

    @Override
    public void sendMessage(String s) {
        client.sendPacket(new SystemMessage(s));
    }

    @Override
    public String getIpAddr() {
        try {
            return client.getIpAddr();
        } catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public Object getNativeClient() {
        return client;
    }

    @Override
    public ISmartPlayer getPlayer() {
        if (client.getActiveChar() == null)
            return null;

        return new SmartPlayer(client.getActiveChar(), this);
    }
}
