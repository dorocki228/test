package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.utils.Location;

public class PetStatusUpdatePacket extends L2GameServerPacket
{
	private final int type;
	private final int obj_id;
	private final int level;
	private final int maxFed;
	private final int curFed;
	private final int maxHp;
	private final int curHp;
	private final int maxMp;
	private final int curMp;
	private final long exp;
	private final long exp_this_lvl;
	private final long exp_next_lvl;
	private final Location _loc;
	private String title;

	public PetStatusUpdatePacket(Servitor summon)
	{
		type = summon.getServitorType();
		obj_id = summon.getObjectId();
		_loc = summon.getLoc();
		title = summon.getTitle();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxFed();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(type);
        writeD(obj_id);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
		writeS(title);
        writeD(curFed);
        writeD(maxFed);
        writeD(curHp);
        writeD(maxHp);
        writeD(curMp);
        writeD(maxMp);
        writeD(level);
		writeQ(exp);
		writeQ(exp_this_lvl);
		writeQ(exp_next_lvl);
        writeD(0);
	}
}
