package l2s.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TradeHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TradeHelper.class);

	public static boolean checksIfCanOpenStore(Player player, int storeType)
	{
		if(player.getLevel() < Config.SERVICES_TRADE_MIN_LEVEL)
		{
			player.sendMessage(new CustomMessage("trade.NotHavePermission").addNumber(Config.SERVICES_TRADE_MIN_LEVEL));
			return false;
		}
		String tradeBan = player.getVar("tradeBan");
		if(tradeBan != null && ("-1".equals(tradeBan) || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			player.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_BLOCKED_FROM_USING_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP);
			return false;
		}
		String BLOCK_ZONE = storeType == 5 ? "open_private_workshop" : "open_private_store";
		if(player.isActionBlocked(BLOCK_ZONE) && (!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && player.isInOfflineMode()))
		{
			player.sendPacket(storeType == 5 ? SystemMsg.YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE : SystemMsg.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
			return false;
		}
		if(player.isCastingNow())
		{
			player.sendPacket(SystemMsg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			return false;
		}
		if(player.isInCombat())
		{
			player.sendPacket(SystemMsg.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}
		if(player.isActionsDisabled() || player.isMounted() || player.isInOlympiadMode() || player.isInDuel() || player.isProcessingRequest())
			return false;
		if (player.isPrivateBuffer()) {
			return false;
		}
		if(Config.SERVICES_TRADE_ONLY_FAR)
		{
			boolean tradenear = false;
			for(Player p : World.getAroundPlayers(player, Config.SERVICES_TRADE_RADIUS, 200))
				if(p.isInStoreMode())
				{
					tradenear = true;
					break;
				}
			if(!World.getAroundNpc(player, Config.SERVICES_TRADE_RADIUS + 100, 200).isEmpty())
				tradenear = true;
			if(tradenear)
			{
				player.sendMessage(new CustomMessage("trade.OtherTradersNear"));
				return false;
			}
		}
		return true;
	}

	public static boolean validateStore(Player player)
	{
		return validateStore(player, 0L);
	}

	public static boolean validateStore(Player player, long adena)
	{
		if(player.isDead())
			return false;
		if(player.getLevel() < Config.SERVICES_TRADE_MIN_LEVEL)
			return false;
		String tradeBan = player.getVar("tradeBan");
		if(tradeBan != null && ("-1".equals(tradeBan) || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			return false;
		String BLOCK_ZONE = player.getPrivateStoreType() == 5 ? "open_private_workshop" : "open_private_store";
		if(player.isActionBlocked(BLOCK_ZONE) && (!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && player.isInOfflineMode()))
			return false;
		switch(player.getPrivateStoreType())
		{
			case 3:
			{
				return validateBuyStore(player, adena);
			}
			case 1:
			case 8:
			{
				return true;
			}
			case 5:
			{
				return true;
			}
			default:
			{
				if(Config.SERVICES_TRADE_ONLY_FAR)
					for(Creature c : World.getAroundCharacters(player, Config.SERVICES_TRADE_RADIUS, 200))
					{
						if(c.isNpc())
							return false;
						if(!c.isPlayer())
							continue;
						Player p = c.getPlayer();
						if(p.isInStoreMode())
							return false;
					}
				return false;
			}
		}
	}

	public static boolean validateBuyStore(Player player, long adena)
	{
		List<TradeItem> buyList = player.getBuyList();
		if(buyList.isEmpty())
			return false;
		if(buyList.size() > player.getTradeLimit())
			return false;
		long totalCost = adena;
		int slots = 0;
		long weight = 0L;
		try
		{
			for(TradeItem item : buyList)
			{
				ItemTemplate template = item.getItem();
				totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(item.getCount(), item.getOwnersPrice()));
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), template.getWeight()));
				if(!template.isStackable() || player.getInventory().getItemByItemId(item.getItemId()) == null)
					++slots;
			}
		}
		catch(ArithmeticException ae)
		{
			return false;
		}
		return totalCost <= player.getAdena() && player.getInventory().validateWeight(weight) && player.getInventory().validateCapacity(slots);
	}

	public static final void purchaseItem(Player buyer, Player seller, TradeItem item)
	{
		long price = item.getCount() * item.getOwnersPrice();
		if(!item.getItem().isStackable())
		{
			if(item.getEnchantLevel() > 0)
			{
				seller.sendPacket(new SystemMessagePacket(SystemMsg.S2S3_HAS_BEEN_SOLD_TO_C1_AT_THE_PRICE_OF_S4_ADENA).addName(buyer).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()).addNumber(price));
				buyer.sendPacket(new SystemMessagePacket(SystemMsg.S2S3_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S4_ADENA).addName(seller).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()).addNumber(price));
			}
			else
			{
				seller.sendPacket(new SystemMessagePacket(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA).addName(buyer).addItemName(item.getItemId()).addNumber(price));
				buyer.sendPacket(new SystemMessagePacket(SystemMsg.S2_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S3_ADENA).addName(seller).addItemName(item.getItemId()).addNumber(price));
			}
		}
		else
		{
			seller.sendPacket(new SystemMessagePacket(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA).addName(buyer).addItemName(item.getItemId()).addNumber(item.getCount()).addNumber(price));
			buyer.sendPacket(new SystemMessagePacket(SystemMsg.S3_S2_HAS_BEEN_PURCHASED_FROM_C1_FOR_S4_ADENA).addName(seller).addItemName(item.getItemId()).addNumber(item.getCount()).addNumber(price));
		}
	}

	public static final long getTax(Player seller, long price)
	{
		return 0;
		//FIXME: Temporarily disabled due to error
/*		long tax = (long) (price * Config.SERVICES_TRADE_TAX / 100.0);
		if(seller.isInZone(Zone.ZoneType.offshore))
			tax = (long) (price * Config.SERVICES_OFFSHORE_TRADE_TAX / 100.0);
		if(Config.SERVICES_TRADE_TAX_ONLY_OFFLINE && !seller.isInOfflineMode())
			tax = 0L;
		if(Config.SERVICES_PARNASSUS_NOTAX && seller.getReflection() == ReflectionManager.PARNASSUS)
			tax = 0L;

		Fraction f = seller.getFraction();
		Castle c = null;

		if(f == Fraction.FIRE)
			c = ResidenceHolder.getInstance().getResidence(Castle.class, 4);
		else if(f == Fraction.WATER)
			c = ResidenceHolder.getInstance().getResidence(Castle.class, 4);

		if(c != null && c.getFraction().canAttack(f))
			tax = (long) (price * c.getSellTaxPercent() / 100.0);

		return tax;*/
	}

	public static void cancelStore(Player activeChar)
	{
		activeChar.setPrivateStoreType(0);
		if(activeChar.isInOfflineMode())
		{
			activeChar.setOfflineMode(false);
			activeChar.kick();
		}
		else
			activeChar.broadcastCharInfo();
	}

	public static int restoreOfflineTraders() throws Exception
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
			statement.setInt(1, (int) (System.currentTimeMillis() / 1000L));
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int objectId = rset.getInt("obj_id");
				if (PunishmentService.INSTANCE.isPunished(PunishmentType.CHARACTER, String.valueOf(objectId))) {
					continue;
				}

				var accountName = CharacterDAO.getInstance().getAccountNameByObjectId(objectId);
                if (PunishmentService.INSTANCE.isPunished(PunishmentType.ACCOUNT, accountName)) {
                    continue;
                }

				int expireTimeSecs = rset.getInt("value");

				String lastHwid = CharacterVariablesDAO.getInstance().getVarFromPlayer(objectId, "last_hwid");
				HwidHolder hwidHolder = lastHwid != null ? HwidUtils.INSTANCE.createHwidHolder(lastHwid) : null;
				if (hwidHolder == null) {
					LOGGER.info("Offline trader {} don't have last_hwid variable.", objectId);
					continue;
				}

				if (PunishmentService.INSTANCE.isPunished(PunishmentType.HWID, hwidHolder.asString())) {
					continue;
				}

				Player p = Player.restore(objectId, hwidHolder);
				if(p == null)
					continue;
				if(!validateStore(p) && !p.isPrivateBuffer())
				{
					p.setPrivateStoreType(0);
					p.setOfflineMode(false);
					p.kick();
				}
				else
				{
					for(AbnormalEffect ae : Config.SERVICES_OFFLINE_TRADE_ABNORMAL_EFFECT)
						p.startAbnormalEffect(ae);
					p.setOfflineMode(true);
					p.setOnlineStatus(true);
					p.entering = false;
					p.spawnMe();
					if(p.getClan() != null && p.getClan().getAnyMember(p.getObjectId()) != null)
						p.getClan().getAnyMember(p.getObjectId()).setPlayerInstance(p, false);
					if(expireTimeSecs != Integer.MAX_VALUE)
						p.startKickTask(expireTimeSecs * 1000L - System.currentTimeMillis());
					++count;
				}
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count;
	}

	public static int getOfflineTradersCount()
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			return 0;
		int count = 0;
		for(Player player : GameObjectsStorage.getPlayers())
			if(player.isInOfflineMode())
				++count;
		return count;
	}
}
