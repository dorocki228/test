package l2s.authserver.network.l2;

import l2s.authserver.network.l2.L2LoginClient.LoginClientState;
import l2s.authserver.network.l2.c2s.AuthGameGuard;
import l2s.authserver.network.l2.c2s.RequestAuthLogin;
import l2s.authserver.network.l2.c2s.RequestServerList;
import l2s.authserver.network.l2.c2s.RequestServerLogin;
import l2s.commons.net.nio.impl.IPacketHandler;
import l2s.commons.net.nio.impl.ReceivablePacket;

import java.nio.ByteBuffer;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	@SuppressWarnings("incomplete-switch")
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;
		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();
		switch(state)
		{
			case CONNECTED:
			{
				if(opcode == 7)
				{
					packet = new AuthGameGuard();
					break;
				}
				break;
			}
			case AUTHED_GG:
			{
				if(opcode == 0)
				{
					packet = new RequestAuthLogin();
					break;
				}
				break;
			}
			case AUTHED:
			{
				if(opcode == 5)
				{
					packet = new RequestServerList();
					break;
				}
				if(opcode == 2)
				{
					packet = new RequestServerLogin();
					break;
				}
				break;
			}
		}
		return packet;
	}
}
