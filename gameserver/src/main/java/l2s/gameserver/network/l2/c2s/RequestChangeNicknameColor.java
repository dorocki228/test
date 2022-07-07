package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.handler.items.impl.NameColorItemHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final int[] COLORS;
	private int _colorNum;
	private int _itemObjectId;
	private String _title;

	@Override
	protected void readImpl()
	{
		_colorNum = readD();
		_title = readS();
		_itemObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_colorNum < 0 || _colorNum >= COLORS.length)
			return;
		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if(item == null)
			return;
		if(!(item.getTemplate().getHandler() instanceof NameColorItemHandler))
			return;
		if(activeChar.consumeItem(item.getItemId(), 1L, true))
		{
			activeChar.setTitle(_title);
			activeChar.broadcastUserInfo(true);
		}
	}

	static
	{
		COLORS = new int[] { 9671679, 8145404, 9959676, 16423662, 16735635, 64672, 10528257, 7903407, 4743829, 10066329 };
	}
}
