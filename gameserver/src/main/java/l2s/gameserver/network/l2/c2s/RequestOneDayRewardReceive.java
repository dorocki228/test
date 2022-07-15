package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExOneDayRewardInfo;
import l2s.gameserver.network.l2.s2c.ExOneDayRewardList;

/**
 * @author Bonux
**/
public class RequestOneDayRewardReceive implements IClientIncomingPacket
{
	private int _missionId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_missionId = packet.readH();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getDailyMissionList().complete(_missionId)) {
			activeChar.sendPacket(new ExOneDayRewardInfo(activeChar));
			activeChar.sendPacket(new ExOneDayRewardList(activeChar));
		}
	}
}