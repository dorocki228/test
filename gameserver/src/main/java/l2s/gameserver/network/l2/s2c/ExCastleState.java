package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;

public class ExCastleState extends L2GameServerPacket
{
	private final int _id;
	private final ResidenceSide _side;

	public ExCastleState(Castle castle)
	{
		_id = castle.getId();
		_side = castle.getResidenceSide();
	}

	@Override
	protected void writeImpl()
	{
        writeD(_id);
        writeD(_side.ordinal());
	}
}
