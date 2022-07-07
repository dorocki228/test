package l2s.Phantoms.parsers.Trade;

import java.util.List;

import l2s.Phantoms.enums.SpawnLocation;

public class PhantomItemInfo
{
	private int id;
	private int[] count;
	private int[] sell_price_0;
	private int[] buy_price_0;
	private List<String> ads;
	private List<String> signboard;
	private List<EnchantPrice> enchant_price;
	private List<SpawnLocation> spawn_location;

	public PhantomItemInfo(int id, int[] count, int[] sell_price_0, int[] buy_price_0, List<String> ads, List<String> signboard, List<EnchantPrice> enchant_price, List<SpawnLocation> spawn_location)
	{
		super();
		this.id = id;
		this.count = count;
		this.sell_price_0 = sell_price_0;
		this.buy_price_0 = buy_price_0;
		this.ads = ads;
		this.signboard = signboard;
		this.enchant_price = enchant_price;
		this.spawn_location = spawn_location;
	}

	public int getId()
	{
		return id;
	}

	public int[] getCount()
	{
		return count;
	}

	public int[] getSell_price_0()
	{
		return sell_price_0;
	}

	public int[] getBuy_price_0()
	{
		return buy_price_0;
	}

	public List<String> getAds()
	{
		return ads;
	}

	public List<String> getSignboard()
	{
		return signboard;
	}

	public List<EnchantPrice> getEnchant_price()
	{
		return enchant_price;
	}

	public List<SpawnLocation> getSpawnLocation()
	{
		return spawn_location;
	}

}
