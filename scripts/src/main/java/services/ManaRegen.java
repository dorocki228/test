package services;

import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.ItemFunctions;

public class ManaRegen
{
	private static final int ADENA = 57;
	private static final long PRICE = 5; //5 аден за 1 МП

	@Bypass("services.ManaRegen:DoManaRegen")
	public void DoManaRegen(Player player, NpcInstance npc, String[] param)
	{
		long mp = (long) Math.floor(player.getMaxMp() - player.getCurrentMp());
		long fullCost = mp * PRICE;
		if(fullCost <= 0)
		{
			player.sendPacket(SystemMsg.NOTHING_HAPPENED);
			return;
		}
		if(ItemFunctions.getItemCount(player, ADENA) >= fullCost)
		{
			ItemFunctions.deleteItem(player, ADENA, fullCost);
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addNumber(mp));
			player.setCurrentMp(player.getMaxMp());
		}
		else
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
	}
}