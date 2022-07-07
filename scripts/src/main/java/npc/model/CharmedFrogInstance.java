package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CharmedFrogInstance extends NpcInstance
{

	public CharmedFrogInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if("kiss_frog".equalsIgnoreCase(command))
		{
			SkillEntry s = SkillHolder.getInstance().getSkillEntry(1068, 3);
			s.getEffects(this, player, 60 * 60 * 1000, 0);

			s = SkillHolder.getInstance().getSkillEntry(1040, 3);
			s.getEffects(this, player, 60 * 60 * 1000, 0);

			s = SkillHolder.getInstance().getSkillEntry(1077, 3);
			s.getEffects(this, player, 60 * 60 * 1000, 0);

			s = SkillHolder.getInstance().getSkillEntry(1086, 2);
			s.getEffects(this, player, 60 * 60 * 1000, 0);

			s = SkillHolder.getInstance().getSkillEntry(1085, 3);
			s.getEffects(this, player, 60 * 60 * 1000, 0);

			s = SkillHolder.getInstance().getSkillEntry(1059, 3);
			s.getEffects(this, player, 60 * 60 * 1000, 0);

			s = SkillHolder.getInstance().getSkillEntry(1204, 2);
			s.getEffects(this, player, 60 * 60 * 1000, 0);
			broadcastPacket(new MagicSkillUse(this, player, 1204, 2, 333, 0));

		}
	}
}
