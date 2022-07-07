package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
	private final int owner_obj_id;
	private final int npc_id;
	private final int _type;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private final int level;
	private int obj_id;
	private final String _name;

	public ExPartyPetWindowUpdate(Servitor summon)
	{
		obj_id = 0;
		obj_id = summon.getObjectId();
		owner_obj_id = summon.getPlayer().getObjectId();
		npc_id = summon.getNpcId() + 1000000;
		_type = summon.getServitorType();
		_name = summon.getName();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		level = summon.getLevel();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(obj_id);
        writeD(npc_id);
        writeD(_type);
        writeD(owner_obj_id);
		writeS(_name);
        writeD(curHp);
        writeD(maxHp);
        writeD(curMp);
        writeD(maxMp);
        writeD(level);
	}
}
