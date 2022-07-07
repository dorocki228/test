package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;

import java.util.ArrayList;
import java.util.List;

public class ExPledgeRecruitInfo extends L2GameServerPacket
{
	private final String _clanName;
	private final String _leaderName;
	private final int _clanLevel;
	private final int _clanMemberCount;
	private final List<SubUnit> _subUnits;

	public ExPledgeRecruitInfo(Clan clan)
	{
		_subUnits = new ArrayList<>();
		_clanName = clan.getName();
		_leaderName = clan.getLeader().getName();
		_clanLevel = clan.getLevel();
		_clanMemberCount = clan.getAllSize();
		for(SubUnit su : clan.getAllSubUnits())
		{
			if(su.getType() == 0)
				continue;
			_subUnits.add(su);
		}
	}

	@Override
	protected void writeImpl()
	{
		writeS(_clanName);
		writeS(_leaderName);
		writeD(_clanLevel);
		writeD(_clanMemberCount);
		writeD(_subUnits.size());
		for(SubUnit su : _subUnits)
		{
			writeD(su.getType());
			writeS(su.getName());
		}
	}
}
