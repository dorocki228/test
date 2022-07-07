package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExOlympiadUserInfoPacket extends L2GameServerPacket
{
	private final int _side;
	private final int class_id;
	private final int curHp;
	private final int maxHp;
	private final int curCp;
	private final int maxCp;
	private int obj_id = 0;
	private final String _name;

	public ExOlympiadUserInfoPacket(Player player, int side)
	{
		_side = side;
		obj_id = player.getObjectId();
		class_id = player.getClassId().getId();
		_name = player.getName();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curCp = (int) player.getCurrentCp();
		maxCp = player.getMaxCp();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_side);
		writeD(obj_id);
		writeS(_name);
		writeD(class_id);
		writeD(curHp);
		writeD(maxHp);
		writeD(curCp);
		writeD(maxCp);
	}
}
