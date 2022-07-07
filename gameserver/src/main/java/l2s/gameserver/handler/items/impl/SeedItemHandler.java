package l2s.gameserver.handler.items.impl;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.mapregion.DomainArea;

public class SeedItemHandler extends DefaultItemHandler
{
	private static final int SEED_SKILL_ID = 2097;

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;
		if(playable.getTarget() == null)
		{
			player.sendActionFailed();
			return false;
		}
		if(!player.getTarget().isMonster() || player.getTarget().isRaid() || ((MonsterInstance) player.getTarget()).getLeader().isRaid() || ((MonsterInstance) playable.getTarget()).getChampion() > 0 && !item.isAltSeed())
		{
			player.sendPacket(SystemMsg.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}
		MonsterInstance target = (MonsterInstance) playable.getTarget();
		if(target == null)
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		if(target.isUnsowing())
		{
			player.sendPacket(SystemMsg.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}
		if(target.isDead())
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		if(target.isSeeded())
		{
			player.sendPacket(SystemMsg.THE_SEED_HAS_BEEN_SOWN);
			return false;
		}
		int seedId = item.getItemId();
		if(seedId == 0 || player.getInventory().getItemByItemId(item.getItemId()) == null)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, player);
		int castleId = domain == null ? 0 : domain.getId();
		if(Manor.getInstance().getCastleIdForSeed(seedId) != castleId)
		{
			player.sendPacket(SystemMsg.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}
		Skill skill = SkillHolder.getInstance().getSkill(2097, 1);
		if(skill == null)
		{
			player.sendActionFailed();
			return false;
		}
		if(skill.checkCondition(player, target, false, false, true))
		{
			player.setUseSeed(seedId);
			player.getAI().Cast(skill, target);
		}
		return true;
	}
}
