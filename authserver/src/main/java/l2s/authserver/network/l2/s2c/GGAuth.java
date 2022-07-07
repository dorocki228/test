package l2s.authserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GGAuth extends L2LoginServerPacket
{
	static Logger _log = LoggerFactory.getLogger(GGAuth.class);
	public static int SKIP_GG_AUTH_REQUEST = 11;
	private final int _response;

	public GGAuth(int response)
	{
		_response = response;
	}

	@Override
	protected void writeImpl()
	{
		writeC(SKIP_GG_AUTH_REQUEST);
		writeD(_response);
		writeB(new byte[16]);
	}
}
