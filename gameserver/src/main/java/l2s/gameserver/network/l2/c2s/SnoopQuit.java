package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

public class SnoopQuit extends L2GameClientPacket
{
	private int _snoopID;

	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = (Player) GameObjectsStorage.findObject(_snoopID);
		if(player == null)
			return;
		Player player2 = getClient().getActiveChar();
		if(player2 == null)
			return;
		player.removeSnooper(player);
		player.removeSnooped(player);
	}
}
