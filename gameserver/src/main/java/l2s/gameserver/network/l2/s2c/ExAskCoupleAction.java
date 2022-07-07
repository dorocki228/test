package l2s.gameserver.network.l2.s2c;

public class ExAskCoupleAction extends L2GameServerPacket
{
	private final int _objectId;
	private final int _socialId;

	public ExAskCoupleAction(int objectId, int socialId)
	{
		_objectId = objectId;
		_socialId = socialId;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_socialId);
        writeD(_objectId);
	}
}
