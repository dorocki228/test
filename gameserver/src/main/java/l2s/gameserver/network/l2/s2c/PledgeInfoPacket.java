package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.pledge.Clan;

public class PledgeInfoPacket extends L2GameServerPacket
{
	private final int clan_id;
	private final String clan_name;
	private final String ally_name;

	public PledgeInfoPacket(Clan clan)
	{
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		ally_name = clan.getAlliance() == null ? "" : clan.getAlliance().getAllyName();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(clan_id);
		writeS(clan_name);
		writeS(ally_name);
	}
}
