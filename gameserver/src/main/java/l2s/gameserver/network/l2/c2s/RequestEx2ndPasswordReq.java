package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordAckPacket;
import l2s.gameserver.security.SecondaryPasswordAuth;

public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private int _changePass;
	private String _password;
	private String _newPassword;

	@Override
	protected void readImpl()
	{
		_changePass = readC();
		_password = readS();
		if(_changePass == 2)
			_newPassword = readS();
	}

	@Override
	protected void runImpl()
	{
		if(!Config.EX_SECOND_AUTH_ENABLED)
			return;

        GameClient client = getClient();

		SecondaryPasswordAuth spa = client.getSecondaryAuth();
		boolean exVal = false;
		if(_changePass == 0 && !spa.passwordExist())
			exVal = spa.savePassword(_password);
		else if(_changePass == 2 && spa.passwordExist())
			exVal = spa.changePassword(_password, _newPassword);
		if(exVal)
			client.sendPacket(new Ex2NDPasswordAckPacket(0));
	}
}
