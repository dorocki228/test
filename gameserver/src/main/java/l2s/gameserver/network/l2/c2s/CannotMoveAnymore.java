package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.ObservePoint;
import l2s.gameserver.model.Player;

public class CannotMoveAnymore implements IClientIncomingPacket
{
	protected final Location _loc = new Location();

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_loc.x = packet.readD();
		_loc.y = packet.readD();
		_loc.z = packet.readD();
		_loc.h = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isInObserverMode())
		{
			ObservePoint observer = activeChar.getObservePoint();
			if(observer != null)
				observer.getMovement().stopMove();
			return;
		}

		activeChar.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, _loc, null);
	}
}