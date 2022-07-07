package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;

public final class AccessoryListener implements OnEquipListener
{
	private static final AccessoryListener _instance;

	public static AccessoryListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		Player player = (Player) actor;
		if(item.getBodyPart() == 2097152 && item.getTemplate().getAttachedSkills().length > 0)
		{
			int agathionId = player.getAgathionId();
			int transformNpcId = player.getTransformId();
			for(SkillEntry skillEntry : item.getTemplate().getAttachedSkills())
			{
				Skill skill = skillEntry.getTemplate();
				if(agathionId > 0 && skill.getNpcId() == agathionId)
					player.setAgathion(0);
				if(skill.getNpcId() == transformNpcId && skill.hasEffect(EffectUseType.NORMAL, EffectType.Transformation))
					player.setTransform(null);
			}
		}
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{}

	static
	{
		_instance = new AccessoryListener();
	}
}
