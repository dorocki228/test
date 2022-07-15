package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @reworked to Ertheia by Bonux
**/
public class TradeStartPacket extends AbstractItemPacket
{
	// TODO find more masks
	private static final int IS_FRIEND = 1 << 0;
	private static final int CLAN_MEMBER = 1 << 1;
	private static final int ALLY_MEMBER = 1 << 3;

	private final int _type;
	private final List<ItemInfo> _tradelist = new ArrayList<ItemInfo>();
	private final int _targetId;
	private final int _targetLevel;

	private int _flags = 0;

	public TradeStartPacket(int type, Player player, Player target)
	{
		_type = type;
		_targetId = target.getObjectId();
		_targetLevel = target.getLevel();

		if(player.getFriendList().contains(target.getObjectId()))
			_flags |= IS_FRIEND;

		if(player.getClan() != null && player.getClan() == target.getClan())
			_flags |= CLAN_MEMBER;

		if(player.getAlliance() != null && player.getAlliance() == target.getAlliance())
			_flags |= ALLY_MEMBER;

		ItemInstance[] items = player.getInventory().getItems();
		for(ItemInstance item : items)
			if(item.canBeTraded(player))
				_tradelist.add(new ItemInfo(player, item, item.getTemplate().isBlocked(player, item)));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TRADE_START.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			packetWriter.writeD(_targetId);
			packetWriter.writeC(_flags);
			packetWriter.writeC(_targetLevel);
			packetWriter.writeC(0x00); // UNK 140
			packetWriter.writeH(0x00); // UNK 140
			packetWriter.writeC(0x00); // UNK 140
		}
		else if(_type == 2)
		{
			packetWriter.writeD(_tradelist.size());
			packetWriter.writeH(_tradelist.size());
			packetWriter.writeC(0x00); // UNK 140
			packetWriter.writeC(0x00); // UNK 140
			for(ItemInfo item : _tradelist)
				writeItem(packetWriter, item);
		}

		return true;
	}
}