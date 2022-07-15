package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.RecipeShopItemInfoPacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.item.RecipeTemplate;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.utils.CraftHelper;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TradeHelper;

public class RequestRecipeShopMakeItem implements IClientIncomingPacket
{
	private int _manufacturerId;
	private int _recipeId;
	private long _price;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_manufacturerId = packet.readD();
		_recipeId = packet.readD();
		_price = packet.readQ();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player buyer = client.getActiveChar();
		if(buyer == null)
			return;

		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isInStoreMode())
		{
			buyer.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isFishing())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		if(buyer.isInTrainingCamp())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
			return;
		}

		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_____);
			return;
		}

		Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.checkInteractionDistance(buyer))
		{
			buyer.sendActionFailed();
			return;
		}

		RecipeTemplate recipe = CraftHelper.INSTANCE.findRecipeByIdAndPrice(manufacturer.getCreateList(), _recipeId, _price);

		if(recipe == null)
		{
			buyer.sendActionFailed();
			return;
		}

		IBroadcastPacket packet = CraftHelper.INSTANCE.checkConditions(recipe, manufacturer);
		if (packet != null) {
			if (!(packet instanceof ActionFailPacket)) {
				manufacturer.sendPacket(packet);
			}
			buyer.sendPacket(packet);
			if (packet != SystemMsg.THE_RECIPE_IS_INCORRECT) {
				buyer.sendPacket(new RecipeShopItemInfoPacket(buyer, manufacturer, recipe.getId(), _price, 0));
			}
			return;
		}

		long priceWithTax = _price - TradeHelper.getTax(manufacturer, _price);
		IBroadcastPacket packet2 = CraftHelper.INSTANCE.craft(recipe, buyer, manufacturer, _price, priceWithTax);
		if (packet2 != null) {
			buyer.sendPacket(packet2);
			buyer.sendPacket(new RecipeShopItemInfoPacket(buyer, manufacturer, recipe.getId(), _price, 0));
			return;
		}

		manufacturer.reduceCurrentMp(recipe.getMpConsume(), null);
		manufacturer.sendStatusUpdate(false, false, StatusUpdatePacket.CUR_MP);

		int success = 0;
		ChancedItemData product = recipe.getRandomProduct();
		if(product != null)
		{
			int itemId = product.getId();
			long itemsCount = product.getCount();

			int rate = recipe.getSuccessRate();
			rate += manufacturer.getStat().getValue(DoubleStat.CRAFT_CHANCE_BONUS);
			rate += buyer.getPremiumAccount().getCraftChanceBonus();
			rate += buyer.getVIP().getTemplate().getCraftChanceBonus();
			rate = Math.min(100, rate);

			if(Rnd.chance(rate))
			{
				//TODO [G1ta0] добавить проверку на перевес
				double criticalChance = recipe.isCanBeCriticalCrafted()
						? manufacturer.getStat().getValue(DoubleStat.CRAFT_CRITICAL_CREATION_CHANCE) : 0.0;
				if (Rnd.chance(criticalChance)) {
					ItemFunctions.addItem(buyer, itemId, itemsCount * 2, true);
					buyer.sendPacket(SystemMsg.CRAFTING_CRITICAL);
					manufacturer.sendPacket(SystemMsg.CRAFTING_CRITICAL);
				} else {
					ItemFunctions.addItem(buyer, itemId, itemsCount, true);
				}

				if(itemsCount > 1)
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA);
					sm.addName(manufacturer, buyer);
					sm.addItemName(itemId);
					sm.addLong(itemsCount);
					sm.addLong(_price);
					buyer.sendPacket(sm);

					sm = new SystemMessagePacket(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA);
					sm.addName(buyer, manufacturer);
					sm.addItemName(itemId);
					sm.addLong(itemsCount);
					sm.addLong(priceWithTax);
					manufacturer.sendPacket(sm);
				}
				else
				{
					SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_CREATED_S2_AFTER_RECEIVING_S3_ADENA);
					sm.addName(manufacturer, buyer);
					sm.addItemName(itemId);
					sm.addLong(_price);
					buyer.sendPacket(sm);

					sm = new SystemMessagePacket(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA);
					sm.addName(buyer, manufacturer);
					sm.addItemName(itemId);
					sm.addLong(priceWithTax);
					manufacturer.sendPacket(sm);
				}
				success = 1;
			}
			else
			{
				SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
				sm.addName(manufacturer, buyer);
				sm.addItemName(itemId);
				sm.addLong(_price);
				buyer.sendPacket(sm);

				sm = new SystemMessagePacket(SystemMsg.YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
				sm.addName(buyer, manufacturer);
				sm.addItemName(itemId);
				sm.addLong(priceWithTax);
				manufacturer.sendPacket(sm);
			}
		}
		else
		{
			SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
			sm.addName(manufacturer, buyer);
			sm.addItemName(recipe.getProducts()[0].getId());
			sm.addLong(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessagePacket(SystemMsg.YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
			sm.addName(buyer, manufacturer);
			sm.addItemName(recipe.getProducts()[0].getId());
			sm.addLong(priceWithTax);
			manufacturer.sendPacket(sm);
		}

		buyer.sendChanges();
		buyer.sendPacket(new RecipeShopItemInfoPacket(buyer, manufacturer, recipe.getId(), _price, success));
	}
}