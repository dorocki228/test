package l2s.gameserver.utils;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.skills.SkillEntry;

public final class SkillUtils
{
	public static int generateSkillHashCode(int id, int level)
	{
		return id * 1000 + level;
	}

	public static int getSkillIdFromPTSHash(int hash)
	{
		int mask = 65535;
		return 0xFFFF & hash >>> 16;
	}

	public static int getSkillLevelFromPTSHash(int hash)
	{
		int mask = 65535;
		return 0xFFFF & hash;
	}

	public static boolean checkSkill(Player player, SkillEntry skillEntry)
	{
		if(!Config.ALT_REMOVE_SKILLS_ON_DELEVEL)
			return false;
		SkillLearn learn = SkillAcquireHolder.getInstance().getSkillLearn(player, skillEntry.getId(), skillEntry.getLevel(), AcquireType.NORMAL);
		if(learn == null)
			return false;
		boolean update = false;
		int lvlDiff = learn.isFreeAutoGet() ? 1 : 4;
		if(learn.getMinLevel() >= player.getLevel() + lvlDiff)
		{
			player.removeSkill(skillEntry, true);
			for(int i = skillEntry.getLevel() - 1; i != 0; --i)
			{
				SkillLearn learn2 = SkillAcquireHolder.getInstance().getSkillLearn(player, skillEntry.getId(), i, AcquireType.NORMAL);
				if(learn2 != null)
				{
					int lvlDiff2 = learn2.isFreeAutoGet() ? 1 : 4;
					if(learn2.getMinLevel() < player.getLevel() + lvlDiff2)
					{
						SkillEntry newSkillEntry = SkillHolder.getInstance().getSkillEntry(skillEntry.getId(), i);
						if(newSkillEntry != null)
						{
							player.addSkill(newSkillEntry, true);
							break;
						}
					}
				}
			}
			update = true;
		}
		if(player.isTransformed())
		{
			learn = player.getTransform().getAdditionalSkill(skillEntry.getId(), skillEntry.getLevel());
			if(learn == null)
				return false;
			if(learn.getMinLevel() >= player.getLevel() + 1)
			{
				player.removeTransformSkill(skillEntry);
				player.removeSkill(skillEntry, false);
				for(int i = skillEntry.getLevel() - 1; i != 0; --i)
				{
					SkillLearn learn2 = player.getTransform().getAdditionalSkill(skillEntry.getId(), i);
					if(learn2 != null)
						if(learn2.getMinLevel() < player.getLevel() + 1)
						{
							SkillEntry newSkillEntry2 = SkillHolder.getInstance().getSkillEntry(skillEntry.getId(), i);
							if(newSkillEntry2 != null)
							{
								player.addTransformSkill(newSkillEntry2);
								player.addSkill(newSkillEntry2, false);
								break;
							}
						}
				}
				update = true;
			}
		}
		return update;
	}
}
