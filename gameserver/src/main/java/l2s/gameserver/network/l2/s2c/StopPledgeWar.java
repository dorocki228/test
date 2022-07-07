package l2s.gameserver.network.l2.s2c;

public class StopPledgeWar extends L2GameServerPacket
{
	private final String _pledgeName;
	private final String _char;

	public StopPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_pledgeName);
		writeS(_char);
	}
}
