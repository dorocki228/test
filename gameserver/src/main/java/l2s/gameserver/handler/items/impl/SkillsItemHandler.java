package l2s.gameserver.handler.items.impl;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

public class SkillsItemHandler extends DefaultItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer() && !playable.isPet())
			return false;
		if(playable.isPet())
		{
			PetInstance pet = (PetInstance) playable;
			if(!pet.isMyFeed(item.getItemId()) && !ArrayUtils.contains(Config.ALT_ALLOWED_PET_POTIONS, item.getItemId()))
			{
				if(pet.getPlayer() != null)
					pet.getPlayer().sendPacket(SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
				return false;
			}
		}
		boolean sendMessage = false;
		boolean canReduce = reduceAfterUse();
		SkillEntry[] skills = item.getTemplate().getAttachedSkills();
		for(int i = 0; i < skills.length; ++i)
		{
			SkillEntry skillEntry = skills[i];
			Skill skill = skillEntry.getTemplate();
			Creature aimingTarget = skill.getAimingTarget(playable, playable.getTarget());
			if(skill.checkCondition(playable, aimingTarget, ctrl, false, true))
			{
				//TODO: FIXME!!
				if(canReduce && skill.getCastRange() > 0 && playable != aimingTarget) {
					canReduce = false;
				}
				if(!playable.getAI().Cast(skill, aimingTarget, ctrl, false))
					return false;
				if(!skill.altUse() && !sendMessage)
					sendMessage = true;
			}
			else if(i == 0)
				return false;

		}
		if(canReduce)
			ItemFunctions.deleteItem(playable, item, 1L, sendMessage);
		return true;
	}

	public boolean reduceAfterUse()
	{
		return false;
	}
}
