package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.network.l2.s2c.AllianceCrestPacket;

public class RequestAllyCrest implements IClientIncomingPacket
{
	// format: cd

	private int _crestId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_crestId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(_crestId == 0)
			return;
		byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);
		if(data != null)
		{
			AllianceCrestPacket ac = new AllianceCrestPacket(_crestId, data);
			client.sendPacket(ac);
		}
	}
}