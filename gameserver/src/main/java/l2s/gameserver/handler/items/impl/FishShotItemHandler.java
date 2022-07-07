/*
 * Decompiled with CFR 0_122.
 */
package l2s.gameserver.handler.items.impl;

import l2s.gameserver.data.xml.holder.FishDataHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.SoulShotType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.fish.RodTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

public class FishShotItemHandler extends DefaultItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
        if(playable == null || !playable.isPlayer())
			return false;

		Player player = (Player) playable;
		if(player.getChargedFishshotPower() > 0.0)
			return false;

		int shotId = item.getItemId();
		boolean isAutoSoulShot = false;

		if(player.isAutoShot(shotId))
			isAutoSoulShot = true;

		WeaponTemplate weaponItem = player.getActiveWeaponTemplate();
		if(player.getActiveWeaponInstance() == null || weaponItem.getItemType() != WeaponTemplate.WeaponType.ROD)
		{
			if(!isAutoSoulShot)
				player.sendPacket(SystemMsg.CANNOT_USE_SOULSHOTS);

			return false;
		}

		RodTemplate rod = FishDataHolder.getInstance().getRod(weaponItem.getItemId());
		if(rod == null)
		{
			if(!isAutoSoulShot)
				player.sendPacket(SystemMsg.CANNOT_USE_SOULSHOTS);

			return false;
		}

        SkillEntry skillEntry;
        if(player.getInventory().destroyItem(item, rod.getShotConsumeCount()))
		{
			skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null && isAutoSoulShot)
			{
				player.removeAutoShot(shotId, true, SoulShotType.SOULSHOT);
				return false;
			}
		}
		else
		{
			if(isAutoSoulShot)
			{
				player.removeAutoShot(shotId, true, SoulShotType.SOULSHOT);
				return false;
			}
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOT_FOR_THAT);
			return false;
		}

		player.forceUseSkill(skillEntry.getTemplate(), player);
		return true;
	}

	@Override
	public boolean isAutoUse()
	{
		return true;
	}
}
