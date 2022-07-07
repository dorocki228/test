package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

import java.util.Calendar;

public class CastleSiegeInfoPacket extends L2GameServerPacket
{
	private final int _startTime;
	private final int _id;
	private final int _ownerObjectId;
	private int _allyId;
	private boolean _isLeader;
	private String _ownerName;
	private String _leaderName;
	private String _allyName;

	public CastleSiegeInfoPacket(Residence residence, Player player)
	{
		_ownerName = "NPC";
		_leaderName = "";
		_allyName = "";
		_id = residence.getId();
		_ownerObjectId = residence.getOwnerId();
		Clan owner = residence.getOwner();
		if(owner != null)
		{
			_isLeader = player.isGM() || owner.getLeaderId(0) == player.getObjectId();
			_ownerName = owner.getName();
			_leaderName = owner.getLeaderName(0);
			Alliance ally = owner.getAlliance();
			if(ally != null)
			{
				_allyId = ally.getAllyId();
				_allyName = ally.getAllyName();
			}
		}
		_startTime = residence.getSiegeEvent() != null ? (int) (residence.getSiegeDate().getTimeInMillis() / 1000L) : 0;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_id);
        writeD(_isLeader ? 1 : 0);
        writeD(_ownerObjectId);
		writeS(_ownerName);
		writeS(_leaderName);
        writeD(_allyId);
		writeS(_allyName);
        writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000L));
        writeD(_startTime);
	}
}
