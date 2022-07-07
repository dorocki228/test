package l2s.authserver.network.l2.s2c;

import l2s.authserver.network.l2.SessionKey;

public final class LoginOk extends L2LoginServerPacket
{
	private final int _loginOk1;
	private final int _loginOk2;

	public LoginOk(SessionKey sessionKey)
	{
		_loginOk1 = sessionKey.loginOkID1;
		_loginOk2 = sessionKey.loginOkID2;
	}

	@Override
	protected void writeImpl()
	{
		writeC(3);
		writeD(_loginOk1);
		writeD(_loginOk2);
		writeB(new byte[8]);
		writeD(1002);
		writeH(60872);
		writeC(35);
		writeC(6);
		writeB(new byte[28]);
	}
}
