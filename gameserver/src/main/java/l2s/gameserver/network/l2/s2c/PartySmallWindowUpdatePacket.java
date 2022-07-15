package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.s2c.updatetype.PartySmallWindowUpdateType;

public class PartySmallWindowUpdatePacket implements IClientOutgoingPacket
{
	private int obj_id, class_id, level;
	private int curCp, maxCp, curHp, maxHp, curMp, maxMp, vitality;
	private String obj_name;
	private int _flags = 0;

	public PartySmallWindowUpdatePacket(Player member, boolean addAllFlags)
	{
		obj_id = member.getObjectId();
		obj_name = member.getName();
		curCp = (int) member.getCurrentCp();
		maxCp = member.getMaxCp();
		curHp = (int) member.getCurrentHp();
		maxHp = member.getMaxHp();
		curMp = (int) member.getCurrentMp();
		maxMp = member.getMaxMp();
		level = member.getLevel();
		class_id = member.getClassId().getId();

		if(addAllFlags)
		{
			for(PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values())
				addUpdateType(type);
		}
	}

	public PartySmallWindowUpdatePacket(Player member)
	{
		this(member, true);
	}
	
	public void addUpdateType(PartySmallWindowUpdateType type)
	{
		_flags |= type.getMask();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PARTY_SMALL_WINDOW_UPDATE.writeId(packetWriter);
		packetWriter.writeD(obj_id);
		packetWriter.writeH(_flags);
		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_CP))
			packetWriter.writeD(curCp); // c4

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_CP))
			packetWriter.writeD(maxCp); // c4

		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_HP))
			packetWriter.writeD(curHp);

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_HP))
			packetWriter.writeD(maxHp);

		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_MP))
			packetWriter.writeD(curMp);

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_MP))
			packetWriter.writeD(maxMp);

		if(containsMask(_flags, PartySmallWindowUpdateType.LEVEL))
			packetWriter.writeC(level);

		if(containsMask(_flags, PartySmallWindowUpdateType.CLASS_ID))
			packetWriter.writeH(class_id);

		if(containsMask(_flags, PartySmallWindowUpdateType.VITALITY_POINTS))
			packetWriter.writeD(0x00);

		return true;
	}
}