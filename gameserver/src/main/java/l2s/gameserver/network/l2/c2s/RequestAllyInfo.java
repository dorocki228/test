package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.List;

public class RequestAllyInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		Alliance ally = player.getAlliance();
		if(ally == null)
			return;
        Clan leaderclan = player.getAlliance().getLeader();
        int clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
        int[] online = new int[clancount + 1];
		int[] count = new int[clancount + 1];
		Clan[] clans = player.getAlliance().getMembers();
		for(int i = 0; i < clancount; ++i)
		{
			online[i + 1] = clans[i].getOnlineMembers(0).size();
			count[i + 1] = clans[i].getAllSize();
			int[] array = online;
			int n = 0;
			array[n] += online[i + 1];
			int[] array2 = count;
			int n2 = 0;
			array2[n2] += count[i + 1];
		}
		List<IBroadcastPacket> packets = new ArrayList<>(7 + 5 * clancount);
		packets.add(SystemMsg.ALLIANCE_INFORMATION);
		packets.add(new SystemMessage(492).addString(player.getClan().getAlliance().getAllyName()));
		packets.add(new SystemMessage(493).addNumber(online[0]).addNumber(count[0]));
		packets.add(new SystemMessage(494).addString(leaderclan.getName()).addString(leaderclan.getLeaderName()));
		packets.add(new SystemMessage(495).addNumber(clancount));
		packets.add(SystemMsg.CLAN_INFORMATION);
		for(int j = 0; j < clancount; ++j)
		{
			packets.add(new SystemMessage(497).addString(clans[j].getName()));
			packets.add(new SystemMessage(498).addString(clans[j].getLeaderName()));
			packets.add(new SystemMessage(499).addNumber(clans[j].getLevel()));
			packets.add(new SystemMessage(493).addNumber(online[j + 1]).addNumber(count[j + 1]));
			packets.add(SystemMsg.LINE_500);
		}
		packets.add(SystemMsg.LINE_490);
		player.sendPacket(packets);
	}
}
