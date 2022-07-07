package l2s.gameserver.network.l2.s2c;

public class AskJoinAlliancePacket extends L2GameServerPacket
{
	private final String _requestorName;
	private final String _requestorAllyName;
	private final int _requestorId;

	public AskJoinAlliancePacket(int requestorId, String requestorName, String requestorAllyName)
	{
		_requestorName = requestorName;
		_requestorAllyName = requestorAllyName;
		_requestorId = requestorId;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_requestorId);
		writeS(_requestorName);
		writeS("");
		writeS(_requestorAllyName);
	}
}
