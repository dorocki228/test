package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;

public class ExAcquireSkillInfo extends L2GameServerPacket
{
	private final Skill _skill;
	private final SkillLearn _learn;

	public ExAcquireSkillInfo(Player player, SkillLearn learn)
	{
		_learn = learn;
		_skill = SkillHolder.getInstance().getSkill(_learn.getId(), _learn.getLevel());
	}

	@Override
	public void writeImpl()
	{
        writeD(_learn.getId());
        writeD(_learn.getLevel());
		writeQ(_learn.getCost());
        writeH(_learn.getMinLevel());
        writeH(0);
		boolean haveItem = _learn.getItemId() > 0;
        writeD(haveItem ? 1 : 0);
		if(haveItem)
		{
            writeD(_learn.getItemId());
			writeQ(_learn.getItemCount());
		}
        writeD(0);
	}
}
