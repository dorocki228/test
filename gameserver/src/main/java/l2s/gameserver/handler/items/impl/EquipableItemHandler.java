package l2s.gameserver.handler.items.impl;

import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.ItemFunctions;

public class EquipableItemHandler extends DefaultItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable.isPet())
		{
			PetInstance pet = (PetInstance) playable;
			IBroadcastPacket sm = ItemFunctions.checkIfCanEquip(pet, item);
			if(sm == null)
			{
				if(item.isEquipped())
					pet.getInventory().unEquipItem(item);
				else
					pet.getInventory().equipItem(item);
				pet.broadcastCharInfo();
				return true;
			}
			if(pet.getPlayer() != null)
				pet.getPlayer().sendPacket(sm);
			return false;
		}
		else
		{
			if(!playable.isPlayer())
				return false;
			Player player = playable.getPlayer();
			if(player.isStunned() || player.isSleeping() || player.isDecontrolled() || player.isAlikeDead() || player.isWeaponEquipBlocked())
			{
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
				return false;
			}
			int bodyPart = item.getBodyPart();
			if((bodyPart == 16384 || bodyPart == 256 || bodyPart == 128) && (player.isMounted() || player.getActiveWeaponFlagAttachment() != null))
			{
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
				return false;
			}
			if(player.isAttackingNow() || player.isCastingNow())
			{
				player.getAI().setNextAction(PlayableAI.AINextAction.EQUIP, item, null, ctrl, false);
				player.sendActionFailed();
				return false;
			}
			if(item.isEquipped())
			{
				ItemInstance weapon = player.getActiveWeaponInstance();
				if(item == weapon)
				{
					player.abortAttack(true, true);
					player.abortCast(true, true);
				}
				player.sendDisarmMessage(item);
				player.getInventory().unEquipItem(item);
				return false;
			}
			IBroadcastPacket p = ItemFunctions.checkIfCanEquip(player, item);
			if(p != null)
			{
				player.sendPacket(p);
				return false;
			}
			player.getInventory().equipItem(item);
			if(!item.isEquipped())
			{
				player.sendActionFailed();
				return false;
			}
			SystemMessagePacket sm2;
			if(item.getFixedEnchantLevel(player) > 0)
			{
				sm2 = new SystemMessagePacket(SystemMsg.EQUIPPED_S1_S2);
				sm2.addNumber(item.getFixedEnchantLevel(player));
				sm2.addItemName(item.getItemId());
			}
			else
				sm2 = new SystemMessagePacket(SystemMsg.YOU_HAVE_EQUIPPED_YOUR_S1).addItemName(item.getItemId());
			player.sendPacket(sm2);
			return true;
		}
	}
}
