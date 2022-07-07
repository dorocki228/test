package l2s.commons.net.utils;

public class NetUtils
{
	private static final NetList PRIVATE;

	public static final boolean isInternalIP(String address)
	{
		return PRIVATE.isInRange(address);
	}

	static
	{
		(PRIVATE = new NetList()).add(Net.valueOf("127.0.0.0/8"));
		PRIVATE.add(Net.valueOf("10.0.0.0/8"));
		PRIVATE.add(Net.valueOf("172.16.0.0/12"));
		PRIVATE.add(Net.valueOf("192.168.0.0/16"));
		PRIVATE.add(Net.valueOf("169.254.0.0/16"));
	}
}
