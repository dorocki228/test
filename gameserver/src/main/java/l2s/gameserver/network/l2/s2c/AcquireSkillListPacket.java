package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.List;

import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author VISTALL
 * @date 22:22/25.05.2011
 */
public class AcquireSkillListPacket implements IClientOutgoingPacket
{
	private Player _player;
	private Collection<SkillLearn> _skills;

	public AcquireSkillListPacket(Player player)
	{
		_player = player;
		_skills = SkillAcquireHolder.getInstance().getAcquirableSkillListByClass(player);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ACQUIRE_SKILL_LIST.writeId(packetWriter);

		packetWriter.writeH(_skills.size());
		for(SkillLearn sk : _skills)
		{
			Skill skill = SkillHolder.getInstance().getSkill(sk.getId(), sk.getLevel());
			if(skill == null)
				continue;

			packetWriter.writeD(sk.getId());
			packetWriter.writeH(sk.getLevel());
			packetWriter.writeQ(sk.getCost());
			packetWriter.writeC(sk.getMinLevel());
			packetWriter.writeC(0x00); // Dual-class min level.
			packetWriter.writeC(true); // TODO: NEW???

			List<ItemData> requiredItems = sk.getRequiredItemsForLearn(AcquireType.NORMAL);
			packetWriter.writeC(requiredItems.size());
			for(ItemData item : requiredItems)
			{
				packetWriter.writeD(item.getId());
				packetWriter.writeQ(item.getCount());
			}

			packetWriter.writeC(0x00); // Analog Skills Count
		}

		return true;
	}
}