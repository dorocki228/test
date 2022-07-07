package  l2s.Phantoms.Utils;

public class TradeItemP
{
	private int item_id;
	private int count;
	private int price;
	
	public TradeItemP(int item_id,int count,int price)
	{
		setItemId(item_id);
		setCount(count);
		setPrice(price);
	}

	public int getItemId()
	{
		return item_id;
	}

	public void setItemId(int item_id)
	{
		this.item_id = item_id;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public int getPrice()
	{
		return price;
	}

	public void setPrice(int price)
	{
		this.price = price;
	}
}
