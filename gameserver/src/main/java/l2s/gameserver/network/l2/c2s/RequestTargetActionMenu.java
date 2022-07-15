package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

public class RequestTargetActionMenu implements IClientIncomingPacket
{
	private int targetObjectId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		targetObjectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		GameObject target = GameObjectsStorage.findObject(targetObjectId);
		if(target == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!target.isTargetable(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!target.isInRange(activeChar, 1400))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setTarget(target);
	}
}