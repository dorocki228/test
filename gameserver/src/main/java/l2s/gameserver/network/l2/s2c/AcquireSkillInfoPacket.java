package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * Reworked: VISTALL
 */
public class AcquireSkillInfoPacket implements IClientOutgoingPacket
{
	private SkillLearn _learn;
	private AcquireType _type;
	private List<Require> _reqs = Collections.emptyList();

	public AcquireSkillInfoPacket(AcquireType type, SkillLearn learn)
	{
		_type = type;
		_learn = learn;
		_reqs = new ArrayList<Require>();
		for(ItemData item : _learn.getRequiredItemsForLearn(type))
			_reqs.add(new Require(99, item.getId(), item.getCount(), 50));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ACQUIRE_SKILL_INFO.writeId(packetWriter);

		packetWriter.writeD(_learn.getId());
		packetWriter.writeD(_learn.getLevel());
		packetWriter.writeQ(_learn.getCost()); // sp/rep
		packetWriter.writeD(_type.getId());

		packetWriter.writeD(_reqs.size()); //requires size

		for(Require temp : _reqs)
		{
			packetWriter.writeD(temp.type);
			packetWriter.writeD(temp.itemId);
			packetWriter.writeQ(temp.count);
			packetWriter.writeD(temp.unk);
		}
		return true;
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