package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.Location;

public class DropItemPacket extends L2GameServerPacket
{
	private final int playerId;
	private final ItemInstance item;

	public DropItemPacket(int playerId, ItemInstance item)
	{
		this.playerId = playerId;
		this.item = item;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(playerId);
        writeD(item.getObjectId());
        writeD(item.getItemId());
		Location loc = item.getLoc();
		writeD(loc.x);
        writeD(loc.y);
        writeD(loc.z + Config.CLIENT_Z_SHIFT);
        writeC(item.isStackable() ? 1 : 0);
		writeQ(item.getCount());

        //writeC(1);
		writeC(0x00);
		// packet.writeD(0x01); if above C == true (1) then packet.readD()

		writeC(item.getEnchantLevel()); // Grand Crusade
		writeC(item.isAugmented() ? 1 : 0); // Grand Crusade
		writeC(item.getNormalEnsouls().length + item.getSpecialEnsouls().length); // Grand Crusade
	}
}
