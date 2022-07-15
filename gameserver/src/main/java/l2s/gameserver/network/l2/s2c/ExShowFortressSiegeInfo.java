package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.Collections;
import java.util.List;

public class ExShowFortressSiegeInfo implements IClientOutgoingPacket
{
	public static final ExShowFortressSiegeInfo STATIC_PACKET = new ExShowFortressSiegeInfo();

	private List<FortressInfo> _infos = Collections.emptyList();

	public ExShowFortressSiegeInfo()
	{
		/*
		fortresses don't exist in classic
		List<Fortress> forts = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
		_infos = new ArrayList<FortressInfo>(forts.size());
		for(Fortress fortress : forts)
		{
			Clan owner = fortress.getOwner();
			_infos.add(new FortressInfo(owner == null ? StringUtils.EMPTY : owner.getName(), fortress.getId(), fortress.getSiegeEvent().isInProgress(), owner == null ? 0 : (int) ((System.currentTimeMillis()- fortress.getOwnDate().getTimeInMillis()) / 1000L)));
		}*/
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_FORTRESSIEGE_INFO.writeId(packetWriter);
		packetWriter.writeD(_infos.size());
		for(FortressInfo _info : _infos)
		{
			packetWriter.writeD(_info._id);
			packetWriter.writeS(_info._owner);
			packetWriter.writeD(_info._status);
			packetWriter.writeD(_info._siege);
		}

		return true;
	}

	static class FortressInfo
	{
		public int _id, _siege;
		public String _owner;
		public boolean _status;

		public FortressInfo(String owner, int id, boolean status, int siege)
		{
			_owner = owner;
			_id = id;
			_status = status;
			_siege = siege;
		}
	}
}