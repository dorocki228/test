package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.PremiumItem;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Gnacik
 * @corrected by n0nam3
 **/
public class ExGetPremiumItemListPacket implements IClientOutgoingPacket
{
	private final int _objectId;
	private final PremiumItem[] _list;

	public ExGetPremiumItemListPacket(Player activeChar)
	{
		_objectId = activeChar.getObjectId();
		_list = activeChar.getPremiumItemList().values();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PREMIUM_ITEM_LIST.writeId(packetWriter);
		packetWriter.writeD(_list.length);
		for(int i = 0; i < _list.length; i++)
		{
			packetWriter.writeD(i);
			packetWriter.writeD(_objectId);
			packetWriter.writeD(_list[i].getItemId());
			packetWriter.writeQ(_list[i].getItemCount());
			packetWriter.writeD(0);
			packetWriter.writeS(_list[i].getSender());
		}

		return true;
	}

}