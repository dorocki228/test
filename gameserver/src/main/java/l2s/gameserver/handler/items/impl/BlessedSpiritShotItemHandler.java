package l2s.gameserver.handler.items.impl;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.SoulShotType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.WeaponTemplate;

public class BlessedSpiritShotItemHandler extends DefaultItemHandler
{
	private static final TIntIntMap SHOT_SKILLS = new TIntIntHashMap();

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;

		Player player = (Player) playable;
		if(player.getChargedSpiritshotPower() > 0.0)
			return false;

		int shotId = item.getItemId();
		boolean isAutoSoulShot = false;
		if(player.isAutoShot(shotId))
			isAutoSoulShot = true;

		if(player.getActiveWeaponInstance() == null)
		{
			if(!isAutoSoulShot)
				player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_SPIRITSHOTS);

			return false;
		}

		WeaponTemplate weaponItem = player.getActiveWeaponTemplate();
		int bspsConsumption = weaponItem.getSpiritShotCount();
		if(bspsConsumption <= 0)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoShot(shotId, true, SoulShotType.SPIRITSHOT);
				return false;
			}
			player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_SPIRITSHOTS);
			return false;
		}

		int[] reducedSpiritshot = weaponItem.getReducedSpiritshot();
		if(reducedSpiritshot[0] > 0 && Rnd.chance(reducedSpiritshot[0]))
			bspsConsumption = reducedSpiritshot[1];

		if(bspsConsumption <= 0)
			return false;

		ItemGrade grade = weaponItem.getGrade().extGrade();
		if(grade != item.getGrade())
		{
			if(isAutoSoulShot)
				return false;
			player.sendPacket(SystemMsg.YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPONS_GRADE);
			return false;
		}

		if(!player.isPhantom())// отключим фантомам потребление итемов
		if(!player.getInventory().destroyItem(item, bspsConsumption))
		{
			if(isAutoSoulShot)
			{
				player.removeAutoShot(shotId, true, SoulShotType.SPIRITSHOT);
				return false;
			}
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOT_FOR_THAT);
			return false;
		}

		SkillEntry skillEntry = item.getTemplate().getFirstSkill();
		if(skillEntry == null)
			skillEntry = SkillHolder.getInstance().getSkillEntry(SHOT_SKILLS.get(grade.ordinal()), 1);

		player.forceUseSkill(skillEntry.getTemplate(), player);
		return true;
	}

	@Override
	public boolean isAutoUse()
	{
		return true;
	}

	static
	{
		SHOT_SKILLS.put(ItemGrade.NONE.ordinal(), 2061);
		SHOT_SKILLS.put(ItemGrade.D.ordinal(), 2160);
		SHOT_SKILLS.put(ItemGrade.C.ordinal(), 2161);
		SHOT_SKILLS.put(ItemGrade.B.ordinal(), 2162);
		SHOT_SKILLS.put(ItemGrade.A.ordinal(), 2163);
		SHOT_SKILLS.put(ItemGrade.S.ordinal(), 2164);
		SHOT_SKILLS.put(ItemGrade.R.ordinal(), 9195);
	}
}
