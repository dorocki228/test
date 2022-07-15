package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExCleftList implements IClientOutgoingPacket
{
	public static final int CleftType_Close = -1;
	public static final int CleftType_Total = 0;
	public static final int CleftType_Add = 1;
	public static final int CleftType_Remove = 2;
	public static final int CleftType_TeamChange = 3;

	private int CleftType = 0; //TODO

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CLEFT_LIST.writeId(packetWriter);
		packetWriter.writeD(CleftType);
		switch(CleftType)
		{
			case CleftType_Total:
				//dd (MinMemberCount:%d bBalancedMatch:%d)
				// BlueTeam: d[dS] (PlayerID:%d PlayerName:%s)
				// RedTeam: d[dS] (PlayerID:%d PlayerName:%s)
				break;
			case CleftType_Add:
				//ddS - TeamID:%d PlayerID:%d PlayerName:%s
				break;
			case CleftType_Remove:
				//dd - TeamID:%d PlayerID:%d
				break;
			case CleftType_TeamChange:
				//ddd - PlayerID:%d From:%d To:%d
				break;
			case CleftType_Close:
				break;
		}

		return true;
	}
}