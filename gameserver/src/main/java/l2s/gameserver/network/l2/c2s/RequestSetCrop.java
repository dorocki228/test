package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.manor.CropProcure;
import l2s.gameserver.utils.NpcUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestSetCrop extends L2GameClientPacket
{
	private int _count;
	private int _manorId;
	private long[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 21 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new long[_count * 4];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 4 + 0] = readD();
			_items[i * 4 + 1] = readQ();
			_items[i * 4 + 2] = readQ();
			_items[i * 4 + 3] = readC();
			if(_items[i * 4 + 0] < 1L || _items[i * 4 + 1] < 0L || _items[i * 4 + 2] < 0L)
			{
				_count = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;
		if(activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(castle.getOwnerId() != activeChar.getClanId() || (activeChar.getClanPrivileges() & 0x20000) != 0x20000)
		{
			activeChar.sendActionFailed();
			return;
		}
		NpcInstance chamberlain = NpcUtils.canPassPacket(activeChar, this);
		if(chamberlain == null || chamberlain.getCastle() != castle)
		{
			activeChar.sendActionFailed();
			return;
		}
		List<CropProcure> crops = new ArrayList<>(_count);
		for(int i = 0; i < _count; ++i)
		{
			int id = (int) _items[i * 4 + 0];
			long sales = _items[i * 4 + 1];
			long price = _items[i * 4 + 2];
			int type = (int) _items[i * 4 + 3];
			if(id > 0)
			{
				//				final CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				//				crops.add(s);
			}
		}
		castle.setCropProcure(crops, 1);
		castle.saveCropData(1);
	}
}
