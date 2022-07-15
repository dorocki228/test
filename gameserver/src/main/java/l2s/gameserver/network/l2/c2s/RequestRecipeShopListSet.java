package l2s.gameserver.network.l2.c2s;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.TradeHelper;

public class RequestRecipeShopListSet implements IClientIncomingPacket
{
	private int[] _recipes;
	private long[] _prices;
	private int _count;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_count = packet.readD();
		if(_count * 12 > packet.getReadableBytes() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return false;
		}
		_recipes = new int[_count];
		_prices = new long[_count];
		for(int i = 0; i < _count; i++)
		{
			_recipes[i] = packet.readD();
			_prices[i] = packet.readQ();
			if(_prices[i] < 0)
			{
				_count = 0;
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player manufacturer = client.getActiveChar();
		if(manufacturer == null || _count == 0)
			return;

		if(!TradeHelper.checksIfCanOpenStore(manufacturer, Player.STORE_PRIVATE_MANUFACTURE))
		{
			manufacturer.sendActionFailed();
			return;
		}

		if(_count > Config.MAX_PVTCRAFT_SLOTS)
		{
			manufacturer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		Map<Integer, ManufactureItem> createList = new LinkedHashMap<>();
		for(int i = 0; i < _count; i++)
		{
			int recipeId = _recipes[i];
			long price = _prices[i];
			if(!manufacturer.findRecipe(recipeId))
				continue;

			ManufactureItem mi = new ManufactureItem(recipeId, price);
			createList.put(mi.getRecipeId(), mi);
		}

		if(!createList.isEmpty())
		{
			manufacturer.setCreateList(createList);
			manufacturer.setPrivateStoreType(Player.STORE_PRIVATE_MANUFACTURE);
			manufacturer.storePrivateStore();
			manufacturer.broadcastPrivateStoreInfo();
			manufacturer.sitDown(null);
			manufacturer.broadcastCharInfo();
		}

		manufacturer.sendActionFailed();
	}
}