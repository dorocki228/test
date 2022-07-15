package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeInfoPacket implements IClientOutgoingPacket
{
	private int clan_id;
	private String clan_name, ally_name;

	public PledgeInfoPacket(Clan clan)
	{
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		ally_name = clan.getAlliance() == null ? "" : clan.getAlliance().getAllyName();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_INFO.writeId(packetWriter);
		packetWriter.writeD(Config.REQUEST_ID);
		packetWriter.writeD(clan_id);
		packetWriter.writeS(clan_name);
		packetWriter.writeS(ally_name);

		return true;
	}
}