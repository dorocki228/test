package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private final int _objectId;
	private final int _effect3;
	private final int _lectureMark;

	public ExBrExtraUserInfo(Player cha)
	{
		_objectId = cha.getObjectId();
		_effect3 = 0;
		_lectureMark = cha.getLectureMark();
	}

	@Override
	protected void writeImpl()
	{
        writeD(_objectId);
        writeD(_effect3);
        writeC(_lectureMark);
	}
}
