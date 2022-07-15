package l2s.gameserver.network.l2.c2s;

import com.google.common.flogger.FluentLogger;

public class RequestExFishRanking implements IClientIncomingPacket
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		LOGGER.atInfo().log("C5: RequestExFishRanking");
	}
}