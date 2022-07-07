package l2s.Phantoms.parsers.Trade;

public class EnchantPrice
{
	private int _enchant_chance;
	private int _enchant; 
	private int[] _price;
	
	public int getEnchant_chance()
	{
		return _enchant_chance;
	}

	public int getEnchant()
	{
		return _enchant;
	}

	public int[] getPrice()
	{
		return _price;
	}

	public EnchantPrice(int enchant_chance,int enchant, int[] price)
	{
		_enchant_chance = enchant_chance; 
		_enchant=enchant;
		_price=price;
	}
	
}
