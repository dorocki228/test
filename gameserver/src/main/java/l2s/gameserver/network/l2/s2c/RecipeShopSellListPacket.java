package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ManufactureItem;

import java.util.List;

public class RecipeShopSellListPacket extends L2GameServerPacket
{
	private final int objId;
	private final int curMp;
	private final int maxMp;
	private final long adena;
	private final List<ManufactureItem> createList;

	public RecipeShopSellListPacket(Player buyer, Player manufacturer)
	{
		objId = manufacturer.getObjectId();
		curMp = (int) manufacturer.getCurrentMp();
		maxMp = manufacturer.getMaxMp();
		adena = buyer.getAdena();
		createList = manufacturer.getCreateList();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(objId);
        writeD(curMp);
        writeD(maxMp);
		writeQ(adena);
        writeD(createList.size());
		for(ManufactureItem mi : createList)
		{
            writeD(mi.getRecipeId());
            writeD(0);
			writeQ(mi.getCost());
		}
	}
}
