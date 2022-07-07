package l2s.authserver.network.l2.s2c;

import l2s.authserver.network.l2.L2LoginClient;

public final class Init extends L2LoginServerPacket
{
	private final int _sessionId;
	private final byte[] _publicKey;
	private final byte[] _blowfishKey;
	private final int _protocol;

	public Init(L2LoginClient client)
	{
		this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId(), client.getProtocol());
	}

	public Init(byte[] publickey, byte[] blowfishkey, int sessionId, int protocol)
	{
		_sessionId = sessionId;
		_publicKey = publickey;
		_blowfishKey = blowfishkey;
		_protocol = protocol;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_sessionId);
		writeD(_protocol);
		writeB(_publicKey);
		writeB(new byte[16]);
		writeB(_blowfishKey);
		writeD(0);
	}
}
