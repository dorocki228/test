package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.GameClient;

public class NetPing extends L2GameClientPacket
{
	public static final int MIN_CLIP_RANGE = 1433;
	public static final int MAX_CLIP_RANGE = 6144;
	private int _timestamp;
	private int _clippingRange;
	private int _fps;

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		client.onPing(_timestamp, _fps, Math.max(MIN_CLIP_RANGE, Math.min(_clippingRange, MAX_CLIP_RANGE)));
	}

	@Override
	protected void readImpl()
	{
		_timestamp = readD();
		_fps = readD();
		_clippingRange = readD();
	}
}
