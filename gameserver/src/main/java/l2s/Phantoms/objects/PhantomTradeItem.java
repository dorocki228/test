package l2s.Phantoms.objects;

public class PhantomTradeItem
{
	private int id;
	private int count;
	private int price;
	private int enchant;
	
	public PhantomTradeItem(int id, int count, int price, int enchant)
	{
		this.id = id;
		this.count=count;
		this.price=price;
		this.enchant=enchant;
	}

	public int getId()
	{
		return id;
	}

	public int getCount()
	{
		return count;
	}

	public int getPrice()
	{
		return price;
	}

	public int getEnchant()
	{
		return enchant;
	}

}
