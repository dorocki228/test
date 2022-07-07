package l2s.gameserver.network.l2.s2c;

public class StartPledgeWar extends L2GameServerPacket
{
	private final String _pledgeName;
	private final String _char;

	public StartPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_char);
		writeS(_pledgeName);
	}
}
