package l2s.gameserver.network.l2.c2s;

public class RequestExCleftEnter extends L2GameClientPacket
{
	private int unk;

	@Override
	protected void readImpl()
	{
		unk = readD();
	}

	@Override
	protected void runImpl()
	{}
}