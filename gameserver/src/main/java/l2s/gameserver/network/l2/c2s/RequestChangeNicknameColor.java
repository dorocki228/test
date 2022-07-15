package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.handler.items.impl.NameColorItemHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class RequestChangeNicknameColor implements IClientIncomingPacket
{
	private static final int COLORS[] = { 0x9393FF, // Pink
			0x7C49FC, // Rose Pink
			0x97F8FC, // Lemon Yellow
			0xFA9AEE, // Lilac
			0xFF5D93, // Cobalt Violet
			0x00FCA0, // Mint Green
			0xA0A601, // Peacock Green
			0x7898AF, // Yellow Ochre
			0x486295, // Chocolate
			0x999999 // Silver
	};

	private int _colorNum, _itemObjectId;
	private String _title;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_colorNum = packet.readD();
		_title = packet.readS();
		_itemObjectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(_colorNum < 0 || _colorNum >= COLORS.length)
			return;

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if(item == null)
			return;

		if(!(item.getTemplate().getHandler() instanceof NameColorItemHandler))
			return;

		if(activeChar.consumeItem(item.getItemId(), 1, true))
		{
			activeChar.setTitleColor(COLORS[_colorNum]);
			activeChar.setTitle(_title);
			activeChar.broadcastUserInfo(true);
		}
	}
}