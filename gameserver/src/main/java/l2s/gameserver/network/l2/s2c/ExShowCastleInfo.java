package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExShowCastleInfo extends L2GameServerPacket
{
	private List<CastleInfo> _infos;

	public ExShowCastleInfo()
	{
		_infos = Collections.emptyList();
		List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		_infos = new ArrayList<>(castles.size());
		for(Castle castle : castles)
		{
			String ownerName = ClanTable.getInstance().getClanName(castle.getOwnerId());
			int id = castle.getId();
			int tax = castle.getSellTaxPercent();
			int nextSiege = castle.getSiegeEvent() != null ? (int) (castle.getSiegeDate().getTimeInMillis() / 1000L) : 0;
			_infos.add(new CastleInfo(ownerName, id, tax, nextSiege));
		}
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_infos.size());
		for(CastleInfo info : _infos)
		{
            writeD(info._id);
			writeS(info._ownerName);
            writeD(info._tax);
            writeD(info._nextSiege);
		}
		_infos.clear();
	}

	private static class CastleInfo
	{
		public String _ownerName;
		public int _id;
		public int _tax;
		public int _nextSiege;

		public CastleInfo(String ownerName, int id, int tax, int nextSiege)
		{
			_ownerName = ownerName;
			_id = id;
			_tax = tax;
			_nextSiege = nextSiege;
		}
	}
}
