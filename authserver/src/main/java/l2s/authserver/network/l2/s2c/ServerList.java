package l2s.authserver.network.l2.s2c;

import l2s.authserver.GameServerManager;
import l2s.authserver.accounts.Account;
import l2s.authserver.network.gamecomm.GameServerDescription;
import l2s.authserver.network.gamecomm.GameServerDescription.HostInfo;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import l2s.commons.net.utils.NetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class ServerList extends L2LoginServerPacket
{
	private final List<ServerData> _servers;
	private final int _lastServer;
	private int _paddedBytes;

	public ServerList(Account account)
	{
		_servers = new ArrayList<>();
		_lastServer = account.getLastServer();
		_paddedBytes = 1;
		for(GameServerConnection gs : GameServerManager.getInstance().getGameServers()) {
			GameServerDescription gameServerDescription = gs.getGameServerDescription();
			for(HostInfo host : gameServerDescription.getHosts())
			{
				InetAddress ip;
				try
				{
					String ipStr = null;
					if(NetUtils.isInternalIP(account.getLastIP()))
						ipStr = host.getInnerIP();
					if(ipStr == null)
						ipStr = host.getIP();
					if(ipStr == null)
						break;
					if(ipStr.equals("*"))
						ipStr = gs.getRemoteAddress().host();
					ip = InetAddress.getByName(ipStr);
				}
				catch(UnknownHostException e)
				{
					break;
				}
				Pair<Integer, int[]> entry = account.getAccountInfo(host.getId());
				_paddedBytes += 3 + 4 * (entry == null ? 0 : entry.getValue().length);
				int size = entry == null ? 0 : entry.getKey();
				int[] d = entry == null ? ArrayUtils.EMPTY_INT_ARRAY : entry.getValue();
				_servers.add(new ServerData(host.getId(), ip, host.getPort(), gameServerDescription, size, d));
			}
		}

	}

	@Override
	protected void writeImpl()
	{
		writeC(4);
		writeC(_servers.size());
		writeC(_lastServer);
		for(ServerData server : _servers)
		{
			writeC(server.serverId);
			InetAddress i4 = server.ip;
			byte[] raw = i4.getAddress();
			writeC(raw[0] & 0xFF);
			writeC(raw[1] & 0xFF);
			writeC(raw[2] & 0xFF);
			writeC(raw[3] & 0xFF);
			writeD(server.port);
			writeC(server.ageLimit);
			writeC(server.pvp ? 1 : 0);
			writeH(0);
			writeH(server.maxPlayers);
			writeC(server.status ? 1 : 0);
			writeD(server.type);
			writeC(server.brackets ? 1 : 0);
		}
		writeH(_paddedBytes);
		writeC(_servers.size());
		for(ServerData server : _servers)
		{
			writeC(server.serverId);
			writeC(server.playerSize);
			writeC(server.deleteChars.length);
			for(int t : server.deleteChars)
				writeD((int) (t - System.currentTimeMillis() / 1000L));
		}
	}

	private static class ServerData
	{
		int serverId;
		InetAddress ip;
		int port;
		//		int online;
		int maxPlayers;
		boolean status;
		boolean pvp;
		boolean brackets;
		int type;
		int ageLimit;
		int playerSize;
		int[] deleteChars;

		ServerData(int serverId, InetAddress ip, int port, boolean pvp, boolean brackets, int type, int online,
				   int maxPlayers, boolean status, int ageLimit, int size, int[] d)
		{
			this.serverId = serverId;
			this.ip = ip;
			this.port = port;
			this.pvp = pvp;
			this.brackets = brackets;
			this.type = type != 1024 ? type : 1;//Костыль для отображения серверов
			//			this.online = online;
			this.maxPlayers = maxPlayers;
			this.status = status;
			playerSize = size;
			this.ageLimit = ageLimit;
			deleteChars = d;
		}

		ServerData(int serverId, InetAddress ip, int port, GameServerDescription gameServerDescription, int size, int[] d)
		{
			this(serverId, ip, port,  gameServerDescription.isPvp(), gameServerDescription.isShowingBrackets(), gameServerDescription.getServerType(),
					gameServerDescription.getOnline(), gameServerDescription.getMaxPlayers(), gameServerDescription.isOnline(), gameServerDescription.getAgeLimit(),
					size, d);
		}
	}
}
