package l2s.gameserver.templates.item.data;

public class ItemData
{
	private final int _id;
	private final long _count;

	public ItemData(int id, long count)
	{
		_id = id;
		_count = count;
	}

	public ItemData(String string)
	{
		var split = string.split(",");
		_id = Integer.parseInt(split[0]);
		_count = Long.parseLong(split[1]);
	}

	public int getId()
	{
		return _id;
	}

	public long getCount()
	{
		return _count;
	}
}
