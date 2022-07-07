package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class TradeStartPacket extends L2GameServerPacket
{
	private static final int IS_FRIEND = 1;
	private static final int CLAN_MEMBER = 2;
	private static final int ALLY_MEMBER = 8;
	private final List<ItemInfo> _tradelist;
	private final int _targetId;
	private final int _targetLevel;
	private int _flags;

	public TradeStartPacket(Player player, Player target)
	{
		_tradelist = new ArrayList<>();
		_flags = 0;
		_targetId = target.getObjectId();
		_targetLevel = target.getLevel();
		if(player.getFriendList().contains(target.getObjectId()))
			_flags |= 0x1;
		if(player.getClan() != null && player.getClan() == target.getClan())
			_flags |= 0x2;
		if(player.getAlliance() != null && player.getAlliance() == target.getAlliance())
			_flags |= 0x8;

		ItemInstance[] items = player.getInventory().getItems();
		for(ItemInstance item : items)
			if(item.canBeTraded(player))
				_tradelist.add(new ItemInfo(item, item.getTemplate().isBlocked(player, item)));
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_targetId);
        writeC(_flags);
        writeC(_targetLevel);
        writeH(_tradelist.size());
		for(ItemInfo item : _tradelist)
            writeItemInfo(item);
	}
}
