package handler.items;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SetupGaugePacket;

public class SoulCrystals extends ScriptItemHandler
{

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = playable.getPlayer();

		if(player.getTarget() == null || !player.getTarget().isMonster())
		{
			player.sendPacket(SystemMsg.INVALID_TARGET, ActionFailPacket.STATIC);
			return false;
		}

		if(player.isImmobilized() || player.isCastingNow())
		{
			player.sendActionFailed();
			return false;
		}

		MonsterInstance target = (MonsterInstance) player.getTarget();

		// u can use soul crystal only when target hp goes to <50%
		if(target.getCurrentHpPercents() >= 50)
		{
			player.sendPacket(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL, ActionFailPacket.STATIC);
			return false;
		}

		// Soul Crystal Casting section
		int skillHitTime = SkillHolder.getInstance().getSkillEntry(2096, 1).getTemplate().getHitTime();
		player.broadcastPacket(new MagicSkillUse(player, 2096, 1, skillHitTime, 0));
		player.sendPacket(new SetupGaugePacket(player, SetupGaugePacket.Colors.BLUE, skillHitTime));
		// End Soul Crystal Casting section

		// Continue execution later
		player._skillTask = ThreadPoolManager.getInstance().schedule(new CrystalFinalizer(player, target, item.getItemId()), skillHitTime);
		return true;
	}

	static class CrystalFinalizer implements Runnable
	{
		private final Player _activeChar;
		private final MonsterInstance _target;
		private final int _itemId;

		CrystalFinalizer(Player activeChar, MonsterInstance target, int id)
		{
			_activeChar = activeChar;
			_target = target;
			_itemId = id;
		}

		@Override
		public void run()
		{
			_activeChar.sendActionFailed();
			_activeChar.clearCastVars();
			if(_activeChar.isDead() || _target.isDead())
				return;
			_target.addAbsorber(_activeChar, _itemId);
		}
	}

}