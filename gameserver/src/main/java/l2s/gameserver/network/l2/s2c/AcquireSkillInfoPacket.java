package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AcquireSkillInfoPacket extends L2GameServerPacket
{
	private final SkillLearn _learn;
	private final AcquireType _type;
	private List<Require> _reqs;

	public AcquireSkillInfoPacket(AcquireType type, SkillLearn learn)
	{
		_reqs = Collections.emptyList();
		_type = type;
		_learn = learn;
		if(_learn.getItemId() != 0)
			(_reqs = new ArrayList<>(1)).add(new Require(99, _learn.getItemId(), _learn.getItemCount(), 50));
	}

	@Override
	public void writeImpl()
	{
        writeD(_learn.getId());
        writeD(_learn.getLevel());
		writeQ(_learn.getCost());
        writeD(_type.getId());
        writeD(_reqs.size());
		for(Require temp : _reqs)
		{
            writeD(temp.type);
            writeD(temp.itemId);
			writeQ(temp.count);
            writeD(temp.unk);
		}
	}

	private static class Require
	{
		public int itemId;
		public long count;
		public int type;
		public int unk;

		public Require(int pType, int pItemId, long pCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}
}
