package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.NpcString;

public class ExSendUIEventPacket extends NpcStringContainer
{
	private final int _objectId;
	private final int _isHide;
	private final int _isIncrease;
	private final int _startTime;
	private final int _endTime;

	public ExSendUIEventPacket(Player player, int isHide, int isIncrease, int startTime, int endTime, String... params)
	{
		this(player, isHide, isIncrease, startTime, endTime, NpcString.NONE, params);
	}

	public ExSendUIEventPacket(Player player, int isHide, int isIncrease, int startTime, int endTime, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objectId = player.getObjectId();
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
	}

	@Override
	protected void writeImpl()
	{
		if(_isHide == 5)
		{
            writeD(_objectId);
            writeD(_isHide);
            writeD(0);
            writeD(0);
			writeS(String.valueOf(_isIncrease));
			writeS(String.valueOf(_startTime));
			writeS(String.valueOf(_endTime));
			writeS(String.valueOf(0));
			writeS(String.valueOf(0));
			writeElements();
		}
		else if(_isHide == 2)
		{
            writeD(_objectId);
            writeD(_isHide);
            writeD(1);
            writeD(0);
			writeS(String.valueOf(_isIncrease));
			writeS(_startTime + "%");
			writeS(String.valueOf(0));
			writeS(String.valueOf(_endTime));
			writeS(String.valueOf(0));
			writeElements();
		}
		else
		{
            writeD(_objectId);
            writeD(_isHide);
            writeD(0);
            writeD(0);
			writeS(String.valueOf(_isIncrease));
			writeS(String.valueOf(_startTime / 60));
			writeS(String.valueOf(_startTime % 60));
			writeS(String.valueOf(_endTime / 60));
			writeS(String.valueOf(_endTime % 60));
			writeElements();
		}
	}
}
