package l2s.gameserver.network.l2.s2c;

public class StopAllianceWar extends L2GameServerPacket
{
	private final String _allianceName;
	private final String _char;

	public StopAllianceWar(String alliance, String charName)
	{
		_allianceName = alliance;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_allianceName);
		writeS(_char);
	}
}
