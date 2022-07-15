package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.network.PacketWriter;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;


public class RelationChangedPacket implements IClientOutgoingPacket
{
	private static class RelationChangedData
	{
		public int objectId;
		public boolean isAutoAttackable;
		public int relation, karma, pvpFlag;
	}

	public static final int RELATION_INSIDE_BATTLEFIELD = 0x00001;
	public static final int RELATION_IN_PVP = 0x00002;
	public static final int RELATION_CHAOTIC = 0x00004;
	public static final int RELATION_IN_PARTY = 0x00008;
	public static final int RELATION_PARTY_LEADER = 0x00010;
	public static final int RELATION_SAME_PARTY = 0x00020;
	public static final int RELATION_IN_PLEDGE = 0x00040;
	public static final int RELATION_PLEDGE_LEADER = 0x00080;
	public static final int RELATION_SAME_PLEDGE = 0x00100;
	public static final int RELATION_SIEGE_PARTICIPANT = 0x00200;
	public static final int RELATION_SIEGE_ATTACKER = 0x00400;
	public static final int RELATION_SIEGE_ALLY = 0x00800;
	public static final int RELATION_SIEGE_ENEMY = 0x01000;
	public static final int RELATION_CLAN_WAR_ATTACKER = 0x04000;
	public static final int RELATION_CLAN_WAR_ATTACKED = 0x08000;
	public static final int RELATION_IN_ALLIANCE = 0x10000;
	public static final int RELATION_ALLIANCE_LEADER = 0x20000;
	public static final int RELATION_SAME_ALLIANCE = 0x40000;
	
	// Masks
	private static final byte SEND_DEFAULT = (byte) 0x01;
	private static final byte SEND_ONE = (byte) 0x02;
	private static final byte SEND_MULTI = (byte) 0x04;

	private byte _mask = (byte) 0x00;

	private final List<RelationChangedData> _datas = new ArrayList<RelationChangedData>(1);

	public RelationChangedPacket()
	{
		//
	}

	public RelationChangedPacket(Playable about, Player target)
	{
		add(about, target);
	}

	public void add(Playable about, Player target)
	{
		RelationChangedData data = new RelationChangedData();
		data.objectId = about.getObjectId();
		data.karma = about.getKarma();
		data.pvpFlag = about.getPvpFlag();
		data.isAutoAttackable = about.isAutoAttackable(target);
		data.relation = about.getRelation(target);

		_datas.add(data);

		if(_datas.size() > 1)
			_mask |= SEND_MULTI;
		else if(_datas.size() == 1)
			_mask |= SEND_ONE;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.RELATION_CHANGED.writeId(packetWriter);
		packetWriter.writeC(_mask);
		if((_mask & SEND_MULTI) == SEND_MULTI)
		{
			packetWriter.writeH(_datas.size());
			for(RelationChangedData data : _datas)
				writeRelation(packetWriter, data);
		}
		else if((_mask & SEND_ONE) == SEND_ONE)
			writeRelation(packetWriter, _datas.get(0));
		else if((_mask & SEND_DEFAULT) == SEND_DEFAULT)
			packetWriter.writeD(_datas.get(0).objectId);

		return true;
	}
	
	private void writeRelation(PacketWriter packet, RelationChangedData data)
	{
		packet.writeD(data.objectId);
		packet.writeD(data.relation);
		packet.writeC(data.isAutoAttackable);
		packet.writeD(data.karma);
		packet.writeC(data.pvpFlag);
	}
}