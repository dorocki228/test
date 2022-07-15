package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.model.Player;

public class RecipeShopMsgPacket implements IClientOutgoingPacket
{
	private int _objectId;
	private String _storeName;

	public RecipeShopMsgPacket(Player player, boolean showName)
	{
		_objectId = player.getObjectId();
		_storeName = showName ? StringUtils.defaultString(player.getManufactureName()) : StringUtils.EMPTY;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.RECIPE_SHOP_MSG.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeS(_storeName);

		return true;
	}
}