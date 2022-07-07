package l2s.gameserver.network.l2.s2c;

public class PetDeletePacket extends L2GameServerPacket
{
	private final int _petId;
	private final int _petnum;

	public PetDeletePacket(int petId, int petnum)
	{
		_petId = petId;
		_petnum = petnum;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_petnum);
        writeD(_petId);
	}
}
