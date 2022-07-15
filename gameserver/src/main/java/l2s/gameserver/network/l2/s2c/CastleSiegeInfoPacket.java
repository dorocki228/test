package l2s.gameserver.network.l2.s2c;

import java.util.Calendar;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

/**
 * Shows the Siege Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = UnitID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = Siege time (seconds) (0 for selectable)<BR>
 * d = Size of Siege Time Select Related
 *   d - next siege time
 *
 * @reworked VISTALL
 */
public class CastleSiegeInfoPacket implements IClientOutgoingPacket
{
	private int _startTime;
	private int _id, _ownerObjectId, _allyId;
	private boolean _isLeader;
	private String _ownerName = "NPC";
	private String _leaderName = StringUtils.EMPTY;
	private String _allyName = StringUtils.EMPTY;

	public CastleSiegeInfoPacket(Residence residence, Player player)
	{
		_id = residence.getId();
		_ownerObjectId = residence.getOwnerId();
		Clan owner = residence.getOwner();
		if(owner != null)
		{
			_isLeader = player.isGM() || owner.getLeaderId(Clan.SUBUNIT_MAIN_CLAN) == player.getObjectId();
			_ownerName = owner.getName();
			_leaderName = owner.getLeaderName(Clan.SUBUNIT_MAIN_CLAN);
			Alliance ally = owner.getAlliance();
			if(ally != null)
			{
				_allyId = ally.getAllyId();
				_allyName = ally.getAllyName();
			}
		}
		_startTime = residence.getSiegeEvent() != null ? (int) (residence.getSiegeDate().getTimeInMillis() / 1000) : 0;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CASTLE_SIEGE_INFO.writeId(packetWriter);
		packetWriter.writeD(_id);
		packetWriter.writeD(_isLeader ? 0x01 : 0x00);
		packetWriter.writeD(_ownerObjectId);
		packetWriter.writeS(_ownerName); // Clan Name
		packetWriter.writeS(_leaderName); // Clan Leader Name
		packetWriter.writeD(_allyId); // Ally ID
		packetWriter.writeS(_allyName); // Ally Name
		packetWriter.writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		packetWriter.writeD(_startTime);
		/*if(_startTime == 0) //если ноль то идет цыкл
			packet.writeDD(_nextTimeMillis, true);*/
		return true;
	}
}