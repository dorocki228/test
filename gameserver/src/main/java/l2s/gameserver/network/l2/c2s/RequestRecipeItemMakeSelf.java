package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.RecipeItemMakeInfoPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.RecipeTemplate;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.ItemFunctions;

public class RequestRecipeItemMakeSelf implements IClientIncomingPacket
{
	private int _recipeId;

	/**
	 * packet type id 0xB8
	 * format:		cd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_recipeId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.isInTrainingCamp())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
			return;
		}

		RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);

		if(recipe == null || recipe.getMaterials().length == 0 || recipe.getProducts().length == 0)
		{
			activeChar.sendPacket(SystemMsg.THE_RECIPE_IS_INCORRECT);
			return;
		}

		//TODO: Должно ли быть сообщение?
		if (!recipe.isCommon()) {
			if (recipe.getLevel() > activeChar.getStat().getCreateItemLevel()) {
				activeChar.sendActionFailed();
				return;
			}
		} else {
			if (recipe.getLevel() > activeChar.getSkillLevel(Skill.SKILL_COMMON_CRAFTING)) {
				activeChar.sendActionFailed();
				return;
			}
		}

		if(activeChar.getCurrentMp() < recipe.getMpConsume())
		{
			activeChar.sendPacket(SystemMsg.NOT_ENOUGH_MP, RecipeItemMakeInfoPacket.Companion.failure(activeChar, recipe, false));
			return;
		}

		if(!activeChar.findRecipe(_recipeId))
		{
			activeChar.sendPacket(SystemMsg.PLEASE_REGISTER_A_RECIPE, ActionFailPacket.STATIC);
			return;
		}

		activeChar.getInventory().writeLock();
		try
		{
			ItemData[] materials = recipe.getMaterials();

			for(ItemData material : materials)
			{
				if(material.getCount() == 0)
					continue;

				if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(material.getId()).getItemType() == EtcItemType.RECIPE)
				{
					RecipeTemplate rp = RecipeHolder.getInstance().getRecipeByRecipeItem(material.getId());
					if(activeChar.hasRecipe(rp))
						continue;
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION,
							RecipeItemMakeInfoPacket.Companion.failure(activeChar, recipe, false));
					return;
				}

				ItemInstance item = activeChar.getInventory().getItemByItemId(material.getId());
				if(item == null || item.getCount() < material.getCount())
				{
					activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION,
							RecipeItemMakeInfoPacket.Companion.failure(activeChar, recipe, false));
					return;
				}
			}

			for(ItemData material : materials)
			{
				if(material.getCount() == 0)
					continue;

				if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(material.getId()).getItemType() == EtcItemType.RECIPE)
					activeChar.unregisterRecipe(RecipeHolder.getInstance().getRecipeByRecipeItem(material.getId()).getId());
				else
				{
					if(!activeChar.getInventory().destroyItemByItemId(material.getId(), material.getCount()))
						continue;//TODO audit
					activeChar.sendPacket(SystemMessagePacket.removeItems(material.getId(), material.getCount()));
				}
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		activeChar.resetWaitSitTime();
		activeChar.reduceCurrentMp(recipe.getMpConsume(), null);

		double rate = recipe.getSuccessRate();
		rate += activeChar.getStat().getValue(DoubleStat.CRAFT_CHANCE_BONUS);
		rate += activeChar.getPremiumAccount().getCraftChanceBonus();
		rate += activeChar.getVIP().getTemplate().getCraftChanceBonus();
		rate = Math.min(100, rate);

		ChancedItemData product = recipe.getRandomProduct();
		if(product != null)
		{
			int itemId = product.getId();
			long itemsCount = product.getCount();

			if(Rnd.chance(rate))
			{
				//TODO [G1ta0] добавить проверку на перевес
				double criticalChance = recipe.isCanBeCriticalCrafted()
						? activeChar.getStat().getValue(DoubleStat.CRAFT_CRITICAL_CREATION_CHANCE) : 0.0;
				boolean createCriticalSuccess = Rnd.chance(criticalChance);
				itemsCount = createCriticalSuccess ? itemsCount * 2 : itemsCount;

				ItemFunctions.addItem(activeChar, itemId, itemsCount, true);
				if (createCriticalSuccess) {
					activeChar.sendPacket(SystemMsg.CRAFTING_CRITICAL);
				}

				activeChar.sendPacket(RecipeItemMakeInfoPacket.Companion.success(activeChar, recipe, false,
						itemsCount, createCriticalSuccess));
				return;
			}
			else
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_FAILED_TO_MANUFACTURE_S1).addItemName(itemId));
		}
		else
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_FAILED_TO_MANUFACTURE_S1).addItemName(recipe.getProducts()[0].getId()));

		activeChar.sendPacket(RecipeItemMakeInfoPacket.Companion.failure(activeChar, recipe, false));
	}
}