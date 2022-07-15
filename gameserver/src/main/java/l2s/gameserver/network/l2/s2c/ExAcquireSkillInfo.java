package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.item.data.ItemData;

public class ExAcquireSkillInfo implements IClientOutgoingPacket
{
	private Skill _skill;
	private List<ItemData> _requiredItems;
	private SkillLearn _learn;

	public ExAcquireSkillInfo(Player player, AcquireType type, SkillLearn learn)
	{
		_learn = learn;
		_requiredItems = _learn.getRequiredItemsForLearn(type);
		_skill = SkillHolder.getInstance().getSkill(_learn.getId(), _learn.getLevel());
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ACQUIRE_SKILL_INFO.writeId(packetWriter);
		packetWriter.writeD(_learn.getId());
		packetWriter.writeD(_learn.getLevel());
		packetWriter.writeQ(_learn.getCost());
		packetWriter.writeH(_learn.getMinLevel());
		packetWriter.writeH(0x00); // Dual-class min level.

		packetWriter.writeD(_requiredItems.size());
		for(ItemData item : _requiredItems)
		{
			packetWriter.writeD(item.getId());
			packetWriter.writeQ(item.getCount());
		}

		packetWriter.writeD(0x00); // Analog skills count

		return true;
	}
}