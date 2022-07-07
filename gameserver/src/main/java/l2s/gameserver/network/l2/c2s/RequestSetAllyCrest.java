package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;

public class RequestSetAllyCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == 192 && _length == _buf.remaining())
            readB(_data = new byte[_length]);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Alliance ally = activeChar.getAlliance();
		if(ally != null && activeChar.isAllyLeader())
		{
			int crestId = 0;
			if(_data != null && CrestCache.isValidCrestData(_data))
				crestId = CrestCache.getInstance().saveAllyCrest(ally.getAllyId(), _data);
			else if(ally.hasAllyCrest())
				CrestCache.getInstance().removeAllyCrest(ally.getAllyId());
			ally.setAllyCrestId(crestId);
			ally.broadcastAllyStatus();
		}
	}
}
