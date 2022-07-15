package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Format: ch d[Sdd]
 * @author SYS
 */
public class ExMpccShowPartyMemberInfo implements IClientOutgoingPacket
{
	private List<PartyMemberInfo> members;

	public ExMpccShowPartyMemberInfo(Party party)
	{
		members = new ArrayList<PartyMemberInfo>();
		for(Player _member : party.getPartyMembers())
			members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_MPCC_SHOW_PARTY_MEMBERINFO.writeId(packetWriter);
		packetWriter.writeD(members.size()); // Количество членов в пати

		for(PartyMemberInfo member : members)
		{
			packetWriter.writeS(member.name); // Имя члена пати
			packetWriter.writeD(member.object_id); // object Id члена пати
			packetWriter.writeD(member.class_id); // id класса члена пати
		}

		members.clear();

		return true;
	}

	static class PartyMemberInfo
	{
		public String name;
		public int object_id, class_id;

		public PartyMemberInfo(String _name, int _object_id, int _class_id)
		{
			name = _name;
			object_id = _object_id;
			class_id = _class_id;
		}
	}
}