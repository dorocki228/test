package l2s.Phantoms.parsers.Trade;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

import l2s.Phantoms.enums.SpawnLocation;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;


public class ItemsInfoParser extends AbstractParser<ItemsInfoHolder>
{
	private static ItemsInfoParser _instance = new ItemsInfoParser();

	public static ItemsInfoParser getInstance()
	{
		return _instance;
	}

	protected ItemsInfoParser()
	{
		super(ItemsInfoHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/ItemForSaleBuy.xml");
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Element surveyElement : rootElement.elements())
		{
			if("item".equalsIgnoreCase(surveyElement.getName()))
			{
				int id = Integer.parseInt(surveyElement.attributeValue("id", "0"));
				List<SpawnLocation> spawn_location = Arrays.asList(surveyElement.attributeValue("location", "").split(",")).stream().map(l-> SpawnLocation.valueOf(l)).collect(Collectors.toList());
				
				int[] count = Arrays.asList(surveyElement.attributeValue("count", "1-1").split("-")).stream().mapToInt(Integer::parseInt).toArray();
				int[] sell_price_0 = null;
				int[] buy_price_0 = null;
				List<String> ads = new ArrayList<String>();
				List<String> signboard = new ArrayList<String>();
				List<EnchantPrice> enchant_price = new ArrayList<EnchantPrice>();

				for(Element second : surveyElement.elements())
				{
					if("sell".equalsIgnoreCase(second.getName()))
					{
						sell_price_0 = Arrays.asList(second.attributeValue("sell_price", "0-0").split("-")).stream().mapToInt(Integer::parseInt).toArray();
						enchant_price.add(new EnchantPrice(100, 0, sell_price_0));

						for(Element third : second.elements())
						{
							if("enchant".equalsIgnoreCase(third.getName()))
							{
								enchant_price.add(new EnchantPrice(Integer.parseInt(third.attributeValue("enchant_chance", "0")), Integer.parseInt(third.attributeValue("enchant", "0")), Arrays.asList(third.attributeValue("sell_price", "0-0").split("-")).stream().mapToInt(Integer::parseInt).toArray()));
							_log.info("enchant_price add enchant_chance" + third.attributeValue("enchant_chance", "0") + " enchant" + third.attributeValue("enchant", "0") + " sell_price " + third.attributeValue("sell_price", "0-0"));
							}
						}
					}
					if("buy".equalsIgnoreCase(second.getName()))
					{
						buy_price_0 = Arrays.asList(second.attributeValue("buy_price", "0-0").split("-")).stream().mapToInt(Integer::parseInt).toArray();
					}
					if("ads".equalsIgnoreCase(second.getName()))
					{
						for(Element third : second.elements())
							if("text".equalsIgnoreCase(third.getName()))
								ads.add(third.attributeValue("text", ""));
					}
					if("signboard".equalsIgnoreCase(second.getName()))
					{
						for(Element third : second.elements())
							if("text".equalsIgnoreCase(third.getName()))
								signboard.add(third.attributeValue("text", ""));
					}

				}

				getHolder().addItems(id, new PhantomItemInfo(id, count, sell_price_0, buy_price_0, ads, signboard, enchant_price,spawn_location));
			}
			if("chance_type".equalsIgnoreCase(surveyElement.getName()))
			{
				getHolder().setRatio(Arrays.asList(surveyElement.attributeValue("sell_buy", "50-50").split("-")).stream().mapToInt(Integer::parseInt).toArray());
			}
			if("spawn".equalsIgnoreCase(surveyElement.getName()))
			{
				String[] loc = surveyElement.attributeValue("location_chance", "").split(",");
				for (String tmp : loc)
				{
					String[] tmp1 = tmp.split(":");
					_log.info(SpawnLocation.valueOf(tmp1[0])+" "+Integer.parseInt(tmp1[1]));
					getHolder().addSpawnLoc(SpawnLocation.valueOf(tmp1[0]),Integer.parseInt(tmp1[1]));
					
				}
			}
		}
	}

	@Override
	public String getDTDFileName()
	{
		return "DTD/ItemForSaleBuy.dtd";
	}
}
