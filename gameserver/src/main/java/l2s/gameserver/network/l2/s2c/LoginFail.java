package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class LoginFail implements IClientOutgoingPacket
{
	public static IClientOutgoingPacket SUCCESS = new LoginFail(0xFFFFFFFF, 0);
	public static IClientOutgoingPacket SYSTEM_ERROR_LOGIN_LATER = new LoginFail(0, 1);
	public static IClientOutgoingPacket PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT = new LoginFail(0, 2);
	public static IClientOutgoingPacket PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT2 = new LoginFail(0, 3);
	public static IClientOutgoingPacket ACCESS_FAILED_TRY_LATER = new LoginFail(0, 4);
	public static IClientOutgoingPacket INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT = new LoginFail(0, 5);
	public static IClientOutgoingPacket ACCESS_FAILED_TRY_LATER2 = new LoginFail(0, 6);
	public static IClientOutgoingPacket ACOUNT_ALREADY_IN_USE = new LoginFail(0, 7);
	public static IClientOutgoingPacket ACCESS_FAILED_TRY_LATER3 = new LoginFail(0, 8);
	public static IClientOutgoingPacket ACCESS_FAILED_TRY_LATER4 = new LoginFail(0, 9);
	public static IClientOutgoingPacket ACCESS_FAILED_TRY_LATER5 = new LoginFail(0, 10);

	private final int _reason1;
	private final int _reason2;

	public LoginFail(int reason1, int reason2)
	{
		_reason1 = reason1;
		_reason2 = reason2;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.LOGIN_FAIL.writeId(packetWriter);
		packetWriter.writeD(_reason1);
		packetWriter.writeD(_reason2);

		return true;
	}
}