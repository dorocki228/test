package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.updatetype.PartySmallWindowUpdateType;

public class PartySmallWindowUpdatePacket extends L2GameServerPacket
{
	private final int obj_id;
	private final int class_id;
	private final int level;
	private final int curCp;
	private final int maxCp;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private int vitality;
	private final String obj_name;
	private int _flags;

	public PartySmallWindowUpdatePacket(Player member, boolean addAllFlags)
	{
		_flags = 0;
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
			for(PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values())
				addUpdateType(type);
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
	protected final void writeImpl()
	{
        writeD(obj_id);
        writeH(_flags);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.CURRENT_CP))
            writeD(curCp);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.MAX_CP))
            writeD(maxCp);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.CURRENT_HP))
            writeD(curHp);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.MAX_HP))
            writeD(maxHp);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.CURRENT_MP))
            writeD(curMp);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.MAX_MP))
            writeD(maxMp);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.LEVEL))
            writeC(level);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.CLASS_ID))
            writeH(class_id);
		if(L2GameServerPacket.containsMask(_flags, PartySmallWindowUpdateType.VITALITY_POINTS))
            writeD(0);
	}
}
