package l2s.gameserver.network.l2.c2s;

import l2s.commons.net.nio.impl.ReceivablePacket;
import l2s.gameserver.GameServer;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.BufferUnderflowException;
import java.util.List;

public abstract class L2GameClientPacket extends ReceivablePacket<GameClient>
{
	private static final Logger LOGGER = LogManager.getLogger(L2GameClientPacket.class);

	@Override
	public final boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch(BufferUnderflowException e)
		{
			_client.onPacketReadFail();
			LOGGER.error("Client: {} - Failed reading: {} - Server Version: {}", _client, getType(),
					GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}
		catch(Exception e2)
		{
			LOGGER.error("Client: {} - Failed reading: {} - Server Version: {}", _client, getType(),
					GameServer.getInstance().getVersion().getRevisionNumber(), e2);
		}
		return false;
	}

	protected abstract void readImpl() throws Exception;

	@Override
	public final void run()
	{
		GameClient client = getClient();
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			LOGGER.error("Client: {} - Failed running: {} - Server Version: {}", client, getType(),
					GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}
	}

	protected abstract void runImpl() throws Exception;

	protected String readS(int len)
	{
		String ret = readS();
		return ret.length() > len ? ret.substring(0, len) : ret;
	}

	protected void sendPacket(L2GameServerPacket packet)
	{
		getClient().sendPacket(packet);
	}

	protected void sendPacket(L2GameServerPacket... packets)
	{
		getClient().sendPacket(packets);
	}

	protected void sendPackets(List<L2GameServerPacket> packets)
	{
		getClient().sendPackets(packets);
	}

	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
}
