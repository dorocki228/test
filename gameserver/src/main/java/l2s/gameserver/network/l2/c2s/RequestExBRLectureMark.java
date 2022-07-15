package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;

/**
 * @author VISTALL
 */
public class RequestExBRLectureMark implements IClientIncomingPacket
{
	public static final int INITIAL_MARK = 1;
	public static final int EVANGELIST_MARK = 2;
	public static final int OFF_MARK = 3;

	private int _mark;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_mark = packet.readC();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null || !Config.EX_LECTURE_MARK)
			return;

		switch(_mark)
		{
			case INITIAL_MARK:
			case EVANGELIST_MARK:
			case OFF_MARK:
				//TODO [VISTALL] проверить ли можно включать - от первого чара 6 месяцев
				player.setLectureMark(_mark);
				player.broadcastUserInfo(true);
				break;
		}
	}
}