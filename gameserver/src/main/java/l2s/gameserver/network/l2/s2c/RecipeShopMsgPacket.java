package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import org.apache.commons.lang3.StringUtils;

public class RecipeShopMsgPacket extends L2GameServerPacket
{
	private final int _objectId;
	private final String _storeName;

	public RecipeShopMsgPacket(Player player, boolean showName)
	{
		_objectId = player.getObjectId();
		_storeName = showName ? StringUtils.defaultString(player.getManufactureName()) : "";
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
		writeS(_storeName);
	}
}
