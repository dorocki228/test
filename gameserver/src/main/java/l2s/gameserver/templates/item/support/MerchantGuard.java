package l2s.gameserver.templates.item.support;

public class MerchantGuard
{
	private final int _itemId;
	private final int _npcId;
	private final int _max;

	public MerchantGuard(int itemId, int npcId, int max)
	{
		_itemId = itemId;
		_npcId = npcId;
		_max = max;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public int getMax()
	{
		return _max;
	}
}
