package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EnchantItemHolder;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EnchantResultPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.EnchantScroll;
import l2s.gameserver.templates.item.support.EnchantVariation;
import l2s.gameserver.templates.premiumaccount.PremiumAccountTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final int ENCHANT_DELAY = 1500;
	private static final Logger _log = LoggerFactory.getLogger(RequestEnchantItem.class);
	private static final int SUCCESS_VISUAL_EFF_ID = 5965;
	private static final int FAIL_VISUAL_EFF_ID = 5949;
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();

		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInTrade())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(System.currentTimeMillis() <= player.getLastEnchantItemTime() + ENCHANT_DELAY)
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInStoreMode() || player.isPrivateBuffer())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResultPacket.CANCEL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendActionFailed();
			return;
		}

		PcInventory inventory = player.getInventory();
		inventory.writeLock();
		try
		{
			ItemInstance item = inventory.getItemByObjectId(_objectId);
			ItemInstance scroll = player.getEnchantScroll();

			if(item == null || scroll == null)
			{
				player.sendActionFailed();
				return;
			}

			EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());

			if(enchantScroll == null)
			{
				player.sendActionFailed();
				return;
			}

			if(enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant())
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(!enchantScroll.getItems().isEmpty())
			{
				if(!enchantScroll.getItems().contains(item.getItemId()))
				{
					player.sendPacket(EnchantResultPacket.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
			}
			else
			{
				if(!enchantScroll.containsGrade(item.getGrade()))
				{
					player.sendPacket(EnchantResultPacket.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}

				int itemType = item.getTemplate().getType2();

				switch(enchantScroll.getType())
				{
					case ARMOR:
					{
						if(itemType == 0 || item.getTemplate().isHairAccessory())
						{
							player.sendPacket(EnchantResultPacket.CANCEL);
							player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							player.sendActionFailed();
							return;
						}
						break;
					}
					case WEAPON:
					{
						if(itemType == 1 || itemType == 2 || item.getTemplate().isHairAccessory())
						{
							player.sendPacket(EnchantResultPacket.CANCEL);
							player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							player.sendActionFailed();
							return;
						}
						break;
					}
					case HAIR_ACCESSORY:
					{
						if(!item.getTemplate().isHairAccessory())
						{
							player.sendPacket(EnchantResultPacket.CANCEL);
							player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
							player.sendActionFailed();
							return;
						}
						break;
					}
				}
			}

			if(!enchantScroll.getItems().contains(item.getItemId()) && !item.canBeEnchanted())
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(!inventory.destroyItem(scroll, 1L))
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendActionFailed();
				return;
			}

			EnchantVariation variation = EnchantItemHolder.getInstance().getEnchantVariation(enchantScroll.getVariationId());

			if(variation == null)
			{
				player.sendActionFailed();
				_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
				return;
			}

			int newEnchantLvl = item.getEnchantLevel() + Rnd.get(enchantScroll.getMinEnchantStep(), enchantScroll.getMaxEnchantStep());
			newEnchantLvl = Math.min(newEnchantLvl, enchantScroll.getMaxEnchant());

			if(newEnchantLvl < item.getEnchantLevel())
			{
				player.sendPacket(EnchantResultPacket.CANCEL);
				player.sendActionFailed();
				return;
			}

			EnchantVariation.EnchantLevel enchantLevel = variation.getLevel(item.getEnchantLevel() + 1);

			if(enchantLevel == null)
			{
				player.sendActionFailed();
				_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] enchant level[" + (item.getEnchantLevel() + 1) + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
				return;
			}

			double chance = enchantLevel.getBaseChance();

			if(item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
				chance = enchantLevel.getFullBodyChance();
			else if(item.getTemplate().isMagicWeapon())
				chance = enchantLevel.getMagicWeaponChance();

			PremiumAccountTemplate premiumAccount = player.getPremiumAccount();
			chance *= premiumAccount.getModifiers().getEnchant();
			if(premiumAccount.getBonus().getEnchantChance() > 0.0)
				chance += premiumAccount.getBonus().getEnchantChance();

			if(item.getGrade() != ItemGrade.NONE)
				chance *= player.getEnchantChanceModifier();

			if(Rnd.chance(chance))
			{
				item.setEnchantLevel(newEnchantLvl);
				item.setJdbcState(JdbcEntityState.UPDATED);
				item.update();

				inventory.sendModifyItem(item);
				inventory.refreshEquip();

				player.sendPacket(new EnchantResultPacket(0, 0, 0L, item.getEnchantLevel()));

				if(enchantLevel.haveSuccessVisualEffect())
				{
					player.broadcastPacket(new SystemMessage(3013).addName(player).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
					player.broadcastPacket(new MagicSkillUse(player, player, SUCCESS_VISUAL_EFF_ID, 1, 500, 1500L));
				}

				player.getListeners().onEnchant(item);
			}
			else
				switch(enchantScroll.getResultType())
				{
					case CRYSTALS:
					{
						if(item.isEquipped())
							player.sendDisarmMessage(item);

						ItemLogMessage message = new ItemLogMessage(player, ItemLogProcess.EnchantFail, item);
						LogService.getInstance().log(LoggerType.ITEM, message);

						if(!inventory.destroyItem(item, 1L))
						{
							player.sendActionFailed();
							return;
						}
						int crystalId = 0;
						int crystalAmount = 0;
						ItemGrade grade = item.getGrade();
						crystalId = Config.ON_ENCHANT_FAIL.getOrDefault(grade, 0);
						if(crystalId != 0)
						{
							if(item.isWeapon())
							{
								int enchant = Math.min(item.getEnchantLevel(), 15);

								switch(enchant)
								{
									case 3:
										crystalAmount = 10;
										break;
									case 4:
										crystalAmount = 15;
										break;
									case 5:
										crystalAmount = 20;
										break;
									case 6:
										crystalAmount = 30;
										break;
									case 7:
										crystalAmount = 45;
										break;
									case 8:
										crystalAmount = 65;
										break;
									case 9:
										crystalAmount = 85;
										break;
									case 10:
										crystalAmount = 115;
										break;
									case 11:
										crystalAmount = 135;
										break;
									case 12:
										crystalAmount = 155;
										break;
									case 13:
										crystalAmount = 180;
										break;
									case 14:
										crystalAmount = 230;
										break;
									case 15:
										crystalAmount = 300;
										break;
								}
							}
							else if(item.isArmor())
							{
								int enchant = Math.min(item.getEnchantLevel(), 15);

								switch(enchant)
								{
									case 3:
										crystalAmount = 2;
										break;
									case 4:
										crystalAmount = 4;
										break;
									case 5:
										crystalAmount = 6;
										break;
									case 6:
										crystalAmount = 8;
										break;
									case 7:
										crystalAmount = 12;
										break;
									case 8:
										crystalAmount = 16;
										break;
									case 9:
										crystalAmount = 19;
										break;
									case 10:
										crystalAmount = 24;
										break;
									case 11:
										crystalAmount = 27;
										break;
									case 12:
										crystalAmount = 30;
										break;
									case 13:
										crystalAmount = 33;
										break;
									case 14:
										crystalAmount = 36;
										break;
									case 15:
										crystalAmount = 39;
										break;
								}
							}
							else
								crystalAmount = item.getEnchantLevel() - 2;
						}
						else
						{
							crystalId = item.getGrade().getCrystalId();
							crystalAmount = item.getCrystalCountOnEchant();
						}

						if(crystalId > 0 && crystalAmount > 0 && !item.isFlagNoCrystallize())
						{
							player.sendPacket(new EnchantResultPacket(1, crystalId, crystalAmount, 0));
							ItemFunctions.addItem(player, crystalId, crystalAmount, true);
						}
						else
							player.sendPacket(EnchantResultPacket.FAILED_NO_CRYSTALS);

						if(enchantScroll.showFailEffect())
						{
							player.broadcastPacket(new MagicSkillUse(player, player, FAIL_VISUAL_EFF_ID, 1, 500, 1500L));
							break;
						}
						break;
					}
					case DROP_ENCHANT:
					{
						int enchantDropCount = enchantScroll.getEnchantDropCount();
						item.setEnchantLevel(Math.max(item.getEnchantLevel() - enchantDropCount, 0));
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();

						inventory.sendModifyItem(item);
						inventory.refreshEquip();

						player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
						player.sendPacket(EnchantResultPacket.BLESSED_FAILED);
						break;
					}
					case NOTHING:
					{
						player.sendPacket(EnchantResultPacket.ANCIENT_FAILED);
						break;
					}
				}
		}
		finally
		{
			inventory.writeUnlock();
			player.updateStats();
		}

		player.setLastEnchantItemTime(System.currentTimeMillis());
	}
}
