package l2s.Phantoms.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.Phantoms.enums.SpawnLocation;
import l2s.Phantoms.enums.TypeOfShop;
import l2s.Phantoms.objects.PhantomTradeItem;
import l2s.Phantoms.parsers.Craft.CraftPhantom;
import l2s.Phantoms.parsers.Trade.ChatAdvertising;
import l2s.Phantoms.parsers.Trade.EnchantPrice;
import l2s.Phantoms.parsers.Trade.ItemsInfoHolder;
import l2s.Phantoms.parsers.Trade.PhantomItemInfo;
import l2s.Phantoms.parsers.Trade.TradePhantom;
import l2s.commons.math.SafeMath;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TradeHelper;


/**
 * @author 4ipolino
 */
public class PhantomTradeUtils
{
	private static final Logger _log = LoggerFactory.getLogger(PhantomTradeUtils.class);
	private static String[] singboard_simb = { " ", ",", "|", "/", };

	public static TradePhantom generateTradeList(int cell_min, int cell_max,int ads_time,int ads_chance)
	{
		SpawnLocation spawn_loc = ItemsInfoHolder.getInstance().getLocationSpawn();
		TypeOfShop type;
		if (Rnd.chance(ItemsInfoHolder.getInstance().getRatio()[0]))
			type = TypeOfShop.SALE;
		else
			type = TypeOfShop.BUY;
		
		List<PhantomItemInfo> rnd_item = ItemsInfoHolder.getInstance().getRdnItem(type, spawn_loc, Rnd.get(cell_min, cell_max));
		List<PhantomTradeItem> trade_items = new ArrayList<>();

		for(PhantomItemInfo pii : rnd_item)
		{
			switch(type)
			{
				case BUY:
				{
					trade_items.add(new PhantomTradeItem(pii.getId(), Rnd.get(pii.getCount()[0], pii.getCount()[1]), numberRound(Rnd.get(pii.getBuy_price_0()[0], pii.getBuy_price_0()[1])), 0));// ну вот примерно так создаем итемы в список
					break;
				}
				case SALE:
					PhantomTradeItem trade_item;
					if(pii.getEnchant_price().size() == 1)// заточки нет
					{
						trade_item = new PhantomTradeItem(pii.getId(), Rnd.get(pii.getCount()[0], pii.getCount()[1]), numberRound(Rnd.get(pii.getSell_price_0()[0],pii.getSell_price_0()[1])), 0);
					}
					else // возможна заточка
					{
						EnchantPrice ench = Rnd.get(pii.getEnchant_price()); // выбрали рандомно заточку
						if(Rnd.chance(ench.getEnchant_chance())) // шанс
						{
							trade_item = new PhantomTradeItem(pii.getId(), 1, Rnd.get(ench.getPrice()[0],ench.getPrice()[1]), ench.getEnchant());
						}
						else // не заточилось
						{
							trade_item = new PhantomTradeItem(pii.getId(), 1, numberRound(Rnd.get(pii.getSell_price_0()[0],pii.getSell_price_0()[1])), 0);
						}
					}
					trade_items.add(trade_item);
					break;
				default:
					break;
			}
		}


		TradePhantom im2 = new TradePhantom(trade_items, false, generateSingboard(rnd_item), type, spawn_loc, new ChatAdvertising(1, 40));
		return im2;
	}

	public static String generateSingboard(List<PhantomItemInfo> rnd_item)
	{
		String trade_text = "";
		String tmp_simb = Rnd.get(singboard_simb);

		if(Rnd.chance(80))
		{
			for(PhantomItemInfo tmp_items : rnd_item)
			{
				String SignboardText = Rnd.get(ItemsInfoHolder.getInstance().getItem(tmp_items.getId()).getSignboard());
				
				if(trade_text.length() + SignboardText.length() > 30)
					break;
				trade_text = trade_text + SignboardText + " " + tmp_simb + " ";
			}
			
			
			
			if(Rnd.chance(37))
			{
				if(Rnd.nextBoolean())
					trade_text = trade_text.toUpperCase();
				else
					trade_text = trade_text.toLowerCase();
			}
		}
		trade_text = trade_text.trim();
		return removeLastChar(trade_text);
	}

	public ChatAdvertising generateAds()
	{
		return null;
	}

	/*
	 * лавка крафта кузнецами
	 */
	public static boolean ManufacturePhantom(Player phantom, CraftPhantom Craft)
	{
		if(phantom == null || Craft._count == 0)
			return false;

		if(!TradeHelper.checksIfCanOpenStore(phantom, Player.STORE_PRIVATE_MANUFACTURE))
		{ return false; }

		if(Craft._count > Config.MAX_PVTCRAFT_SLOTS)
		{
			_log.warn("Max count Manufacture Phantom " + phantom + " " + Craft._count);
			return false;
		}

		List<ManufactureItem> createList = new CopyOnWriteArrayList<ManufactureItem>();
		for(int i = 0; i < Craft._count; i++)
		{
			int recipeId = Craft._recipes.get(i);
			long price = Craft._prices.get(i);

			phantom.registerRecipe(RecipeHolder.getInstance().getRecipeByRecipeId(recipeId), true);
			ManufactureItem mi = new ManufactureItem(recipeId, price);
			createList.add(mi);
		}

		if(!createList.isEmpty())
		{
			phantom.setManufactureName(Craft._Craftname);
			phantom.setCreateList(createList);
			// phantom.saveTradeList();
			phantom.setPrivateStoreType(Player.STORE_PRIVATE_MANUFACTURE);
			phantom.broadcastPrivateStoreInfo();
			//phantom.broadcastPacket(new RecipeShopMsg(phantom));
			phantom.sitDown(null);
			phantom.broadcastCharInfo();
			return true;
		}
		phantom.sendActionFailed();
		return false;
	}

	/*
	 * торговая лавка - покупка\продажа
	 */
	public static boolean TradeBuySellPhantom(Player phantom, TradePhantom Trade)
	{
		phantom.phantom_params.setTradeList(Trade);

		switch(Trade.getType())
		{
			case BUY:
			{
				phantom.setBuyStoreName(Trade._TradeBuyname);

				if(phantom == null || Trade.getCount() == 0)
				{
					_log.warn("Bay " + Trade.getCount() + " " + Trade._TradeBuyname);
					return false;
				}
				if(!TradeHelper.checksIfCanOpenStore(phantom, Trade._ispackage ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL))
				{
					phantom.sendActionFailed();
					return false;
				}
				List<TradeItem> buyList = new CopyOnWriteArrayList<TradeItem>();
				long totalCost = 0;
				try
				{
					loop: for(PhantomTradeItem t : Trade.getItems())// список наших итемов
					{

						ItemTemplate item = ItemHolder.getInstance().getTemplate(t.getId()); // получаем "темплейт" итема

						if(item == null || t.getId() == ItemTemplate.ITEM_ID_ADENA)
							continue; // аденой мы не торгуем, пропускаем
						phantom.getInventory().addItem(item.getItemId(), 1); // выдадим игроку итем

						if(t.getEnchant() > 0 && item.isEnchantable()) // если мин заточка больше нуля то точим, за одно
							// проверим можно ли точить итем иначе будут
							// заточены любые итемы
							phantom.getInventory().getItemByItemId(t.getId()).setEnchantLevel(t.getEnchant());// подтянем с сумки и заточим

						if(item.isStackable())// тут проверим стопкой ли итемы и посмотрим что у нас с ценой
							for(TradeItem bi : buyList)
								if(bi.getItemId() == t.getId())
								{
									bi.setOwnersPrice(t.getPrice());
									bi.setCount(bi.getCount() + t.getCount());
									totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(t.getCount(), t.getPrice()));
									continue loop;
								}

						// формируем список итемов в лавке
						TradeItem bi = new TradeItem();
						bi.setItemId(t.getId());
						bi.setCount(t.getCount());
						bi.setOwnersPrice(t.getPrice());
						totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(t.getCount(), t.getPrice()));
						buyList.add(bi);
					}
				}
				catch(ArithmeticException ae)
				{
					return false;
				}

				phantom.addAdena(totalCost + 100000);// с запасом

				if(totalCost > phantom.getAdena())
				{
					_log.warn("Bay " + totalCost + " > " + phantom.getAdena());
					return false;
				}

				if(!buyList.isEmpty())
				{

					String _ads_text = "";
					
					TradeItem info_temp = Rnd.get(buyList);
					
//					for(TradeItem tmp_items : buyList)// перебираем все наши итемы
//					{
						PhantomItemInfo tmp_iinfo = ItemsInfoHolder.getInstance().getItem(info_temp.getItemId());
						_ads_text = _ads_text + Rnd.get(tmp_iinfo.getAds());
//					}

					phantom.phantom_params.getTradeList().getAds().setAdsText(_ads_text);

					phantom.setBuyList(buyList);
					// phantom.saveTradeList();
					phantom.setPrivateStoreType(Player.STORE_PRIVATE_BUY); // открыть лавку покупки , значит точили на
					// покупку
					//phantom.broadcastPacket(new PrivateStoreMsgBuy(phantom));
					phantom.broadcastPrivateStoreInfo();
					phantom.sitDown(null);
					phantom.broadcastCharInfo();
					return true;
				}
				else
					_log.warn("buyList isEmpty " + Trade._TradeBuyname);
				return false;
			}
			// продажа
			case SALE:
			{
				phantom.setSellStoreName(Trade._TradeBuyname);

				if(phantom == null || Trade.getCount() == 0)
				{
					_log.warn("sell " + Trade.getCount() + " " + Trade._TradeBuyname);
					return false;
				}
				if(!TradeHelper.checksIfCanOpenStore(phantom, Trade._ispackage ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL))
				{
					phantom.sendActionFailed();
					return false;
				}

				List<TradeItem> sellList = new ArrayList<TradeItem>();
				List<ItemInstance> adsItem = new ArrayList<ItemInstance>();

				for(PhantomTradeItem t : Trade.getItems())
				{
					ItemInstance item = ItemFunctions.createItem(t.getId());

					if(item == null || !item.canBeTraded(phantom) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
						continue;

					if(item.isStackable())
					{
						ItemInstance tmp_item = phantom.getInventory().addItem(t.getId(), t.getCount());
						adsItem.add(tmp_item);

						TradeItem temp = new TradeItem(tmp_item);
						temp.setCount(t.getCount());
						temp.setOwnersPrice(t.getPrice());
						sellList.add(temp);
						continue;
					}
					else// тут надо дергать список итемов которые выдаем и по нему формировать текст
					{
						int c = 1;
						while(c <= t.getCount())// тут может быть странная дичь если продавать одинаковые предметы с разной
						// заточкой, мб и надо пофиксить, хз
						{
							ItemTemplate t_item = ItemHolder.getInstance().getTemplate(t.getId());// получим темплект

							ItemInstance tmp_item = phantom.getInventory().addItem(t.getId(), 1); // выдадим игроку
							adsItem.add(tmp_item);

							if(t.getEnchant() > 0 && t_item.isEnchantable()) // если мин заточка больше нуля то точим, за
								// одно проверим можно ли точить итем иначе
								// будут заточены любые итемы
								tmp_item.setEnchantLevel(t.getEnchant());// подтянем с сумки и
							// заточим

							TradeItem temp = new TradeItem(tmp_item);
							temp.setCount(1);
							temp.setOwnersPrice(t.getPrice());
							sellList.add(temp);
							c++;
						}
					}
				}

				String _ads_text = "";// текст который получим после наших манипуляций
				
				ItemInstance tmp_items = Rnd.get(adsItem); // Выбирает только 1 итем из списка 
				
//				for(ItemInstance tmp_items : adsItem)// перебираем все наши итемы
//				{
					PhantomItemInfo tmp_iinfo = ItemsInfoHolder.getInstance().getItem(tmp_items.getItemId()); // дергаем
					// инфу по ид итема  с нашего хранилища

					if(tmp_items.getEnchantLevel() > 0)
					{
						_ads_text = _ads_text + Rnd.get(tmp_iinfo.getAds()) + " +" + tmp_items.getEnchantLevel();
					}
					else
						_ads_text = _ads_text + Rnd.get(tmp_iinfo.getAds()); // выбираем рандомный текст
//				}
				
				
				// тут лупит нулпоинт так как обьекта адс нет, ты не создал 
				phantom.phantom_params.getTradeList().getAds().setAdsText(_ads_text);
				if(sellList.size() > phantom.getTradeLimit())
				{
					//phantom.sendPacket(new PrivateStoreManageListSell(phantom, Trade._ispackage));
					return false;
				}

				if(!sellList.isEmpty())
				{
					phantom.setSellList(Trade._ispackage, sellList);
					// phantom.saveTradeList();
					phantom.setPrivateStoreType(Trade._ispackage ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL); // открыть
					// лавку
					// продажи
					phantom.broadcastPrivateStoreInfo();
					//phantom.broadcastPacket(new PrivateStoreMsgSell(phantom));
					phantom.sitDown(null);
					phantom.broadcastCharInfo();
					return true;
				}
				else
					_log.warn("sellList isEmpty " + Trade._TradeBuyname);

				sellList.clear();
				return false;
			}
		}
		return false;
	}

	
	public static String removeLastChar(String s)
	{
		if(s == null || s.length() == 0)
			return s;
		for (String s2 : singboard_simb)
		{
			if (s.charAt(s.length() - 1) == s2.charAt(0))
				return s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	public static int numberRound (int price)
	{
		if (price < 999 && price >= 100)
			return Math.round( price / 10) * 10;
		
		if (price < 9999 && price >= 1000)
			return Math.round( price / 100) * 100;
		
		if (price < 99999 && price >= 10000)
			return Math.round( price / 1000) * 1000;
		
		if (price < 999999 && price >= 100000)
			return Math.round( price / 10000) * 10000;
		
		if (price < 9999999 && price >= 1000000)
			return Math.round( price / 100000) * 100000;
		
		if (price < 99999999 && price >= 10000000)
			return Math.round( price / 1000000) * 1000000;
		
		if (price >= 99999999)
			return Math.round( price / 10000000) * 10000000;
		
		return price;
	}
}