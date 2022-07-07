package l2s.gameserver.network.l2.s2c;

public class AskJoinPartyPacket extends L2GameServerPacket
{
	private final String _requestorName;
	private final int _itemDistribution;

	public AskJoinPartyPacket(String requestorName, int itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_requestorName);
        writeD(_itemDistribution);
	}
}
