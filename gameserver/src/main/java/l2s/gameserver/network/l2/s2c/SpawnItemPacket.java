package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.items.ItemInstance;

public class SpawnItemPacket extends L2GameServerPacket
{
	private final ItemInstance item;

	public SpawnItemPacket(ItemInstance item)
	{
		this.item = item;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(item.getObjectId());
        writeD(item.getItemId());
        writeD(item.getX());
        writeD(item.getY());
        writeD(item.getZ() + Config.CLIENT_Z_SHIFT);
        writeD(item.isStackable() ? 1 : 0);
		writeQ(item.getCount());
        writeD(0);

        writeC(item.getEnchantLevel()); // Grand Crusade
		writeC(item.isAugmented() ? 1 : 0); // Grand Crusade
		writeC(item.getNormalEnsouls().length + item.getSpecialEnsouls().length); // Grand Crusade
	}
}
