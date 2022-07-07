package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;

import java.util.concurrent.TimeUnit;

public class RequestReload extends L2GameClientPacket
{
	private static final String REQUEST_RELOAD_DELAY = "REQUEST_RELOAD_DELAY";

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		boolean delay = player.getVarBoolean(REQUEST_RELOAD_DELAY, false);
		if(delay)
			return;

		player.sendUserInfo(true);
		World.showObjectsToPlayer(player);

		long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
		player.setVar(REQUEST_RELOAD_DELAY, true, expirationTime);
	}
}
