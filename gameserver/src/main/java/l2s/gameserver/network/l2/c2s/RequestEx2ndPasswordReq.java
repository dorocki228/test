package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordAckPacket;
import l2s.gameserver.security.SecondaryPasswordAuth;

/**
 * (ch)cS{S}
 * c: change pass?
 * S: current password
 * S: new password
 */
public class RequestEx2ndPasswordReq implements IClientIncomingPacket
{
	private int _changePass;
	private String _password, _newPassword;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_changePass = packet.readC();
		_password = packet.readS();
		if(_changePass == 2)
			_newPassword = packet.readS();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(!Config.EX_SECOND_AUTH_ENABLED)
			return;

		SecondaryPasswordAuth spa = client.getSecondaryAuth();
		boolean exVal = false;

		if(_changePass == 0 && !spa.passwordExist())
			exVal = spa.savePassword(_password);
		else if(_changePass == 2 && spa.passwordExist())
			exVal = spa.changePassword(_password, _newPassword);

		if(exVal)
			client.sendPacket(new Ex2NDPasswordAckPacket(Ex2NDPasswordAckPacket.SUCCESS));
	}
}
