package l2s.Phantoms.ai.merchants;

import l2s.Phantoms.ai.abstracts.PhantomAITask;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;

public class MerchantsTask extends PhantomAITask
{
	public MerchantsTask(Player ph)
	{
		super(ph);
	}

	@Override
	public void runImpl()
	{
		String[] ads_prefix_symbols = {
				"-- %",
				"--- %",
				"++ %",
				"+++ %",
				"> %",
				">> %",
				">>> %",
				"-> %",
				"--> %",
				"-->> %",
				"% !!",
				"% !",
				"% !!",
				"-- % --",
				"--- % ---",
				"++ % ++",
				"+++ % +++",
				"> % <",
				">> % <<",
				">>> % <<<",
				"-> % <-",
				"--> % <--",
				"-->> % <<--", };

		String[] ads_prefix_location = {
				"% (afk)",
				"% (Dion)",
				"% (DION)",
				"% Dion",
				"% dion",
				"% DION",
				"% /target dion afk",
				"% /target " + phantom.getName(),
				"% /target " + phantom.getName() + " (Dion)",
				"% /target " + phantom.getName() + " (afk)", };

		String[] ads_prefix_sale = {
				"ПРОДАМ %",
				"ПРОДАЮ %",
				"Продам %",
				"продам %",
				"Продаю %",
				"продаю %",
				"ВТС %",
				"Втс %",
				"втс %", };
		
		String[] ads_prefix_sale_en = {
				"SELL %",
				"Sell %",
				"sell %",
				"WTS %",
				"Wts %",
				"wts %", };

		String[] ads_prefix_buy = {
				"ПОКУПАЮ %",
				"Покупаю %",
				"покупаю %",
				"КУПЛЮ %",
				"Куплю %",
				"куплю %",
				"СКУПАЮ %",
				"Скупаю %",
				"скупаю %",
				"ВТБ %",
				"Втб %",
				"втб %", };

		String[] ads_prefix_buy_en = {
				"BUY %",
				"Buy %",
				"buy %",
				"WTB %",
				"Wtb %",
				"wtb %",};

//		String[] ads_suffix_sale = { "% ДЕШЕВО", "% Дешево", "% дешево", "% СРОЧНО", "% Срочно", "% срочно", };
//		String[] ads_suffix_sale_en = { "% CHEAP", "% Cheap", "% cheap", "% FAST", "% Fast", "% fast", };

		try
		{
			// напиши дтд хсему и проверяй kk
			if(phantom.phantom_params.getTradeList().getAds().getAdsText() == null || phantom.phantom_params.getTradeList().getAds().getAdsText().isEmpty())
				return;

			long now = System.currentTimeMillis();
			if(now > phantom.phantom_params.getChatTradeTime())
			{
				phantom.phantom_params.setChatTradeTime(now + (phantom.phantom_params.getTradeList().getAds().getAdsTime() * 60 * 1000));
				if(Rnd.chance(phantom.phantom_params.getTradeList().getAds().getAdsChance()))
				{
					String ads_text = phantom.phantom_params.getTradeList().getAds().getAdsText();// тут мы раньше рандомно выбирали текст. 

					switch(phantom.phantom_params.getTradeList().getType())
					{
						case BUY:
						{
							if(isCyrillic(ads_text))
							{
								String current_prefix = Rnd.get(ads_prefix_buy);
								if(current_prefix.equals(current_prefix.toUpperCase())) // поднимем в верхний регистр, сравним с базовой, если совпало - вся строка в верхнем регистре
								{
									ads_text = current_prefix.replace("%", ads_text.toUpperCase());// поднимем остальную строку в верхний регистр
									break;
								}
								else // строка в разном регистре
								{
									ads_text = current_prefix.replace("%", ads_text);
									break;
								}
							}
							else//латиница, по аналогу
							{
								ads_text = Rnd.get(ads_prefix_buy_en).replace("%", ads_text);
								break;
							}
						}
						case SALE:
						{
							if(isCyrillic(ads_text))
							{
								String current_prefix = Rnd.get(ads_prefix_sale);
								if(current_prefix.equals(current_prefix.toUpperCase())) // поднимем в верхний регистр, сравним с базовой, если совпало - вся строка в верхнем регистре
								{
									ads_text = current_prefix.replace("%", ads_text.toUpperCase());// поднимем остальную строку в верхний регистр
									break;
								}
								else // строка в разном регистре
								{
									ads_text = current_prefix.replace("%", ads_text);
									break;
								}
							}
							else//латиница, по аналогу
							{
								ads_text = Rnd.get(ads_prefix_sale_en).replace("%", ads_text);
								break;
							}
							}
							default:
							break;
						}

					if(Rnd.chance(10))
					{
						ads_text = Rnd.get(ads_prefix_symbols).replace("%", ads_text);
					}
					else if(Rnd.chance(15))
					{
						ads_text = Rnd.get(ads_prefix_location).replace("%", ads_text);
					}

					SayPacket2 cs = new SayPacket2(phantom.getObjectId(), Rnd.nextBoolean() ? ChatType.SHOUT : ChatType.SHOUT, phantom.getName(), ads_text);
					for(Player temp : GameObjectsStorage.getPlayers())
					{
						if(temp.isPhantom() || temp == phantom || phantom.getReflection() != temp.getReflection() || temp.isBlockAll() /*|| temp.isInBlockList(phantom)*/)
							continue;
						temp.sendPacket(cs);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isCyrillic(String s)
	{
		boolean result = false;
		for(char a : s.toCharArray())
		{
			if(Character.UnicodeBlock.of(a) == Character.UnicodeBlock.CYRILLIC)
			{
				result = !result;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean doAction()
	{
		return true;
	}
}