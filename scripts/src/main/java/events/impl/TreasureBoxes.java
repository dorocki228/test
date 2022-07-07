package events.impl;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author KanuToIIIKa
 */

public class TreasureBoxes implements OnInitScriptListener
{

	@Override
	public void onInit()
	{
		//		CharListenerList.addGlobal(new KillMonster());
	}

	private class KillMonster implements OnKillListener
	{

		private final int GLOBAL_CHANCE = 2;

		@Override
		public void onKill(Creature killer, Creature victim)
		{
			if(killer.isPlayable() && victim.isMonster())
			{
				if(victim.getLevel() >= 20 && victim.getLevel() <= 35 && Rnd.chance(GLOBAL_CHANCE))
				{
					ItemInstance i = ItemFunctions.createItem(5202);
					i.dropToTheGround(killer.getPlayer(), (NpcInstance) victim);
				}
				else if(victim.getLevel() >= 36 && victim.getLevel() <= 50 && Rnd.chance(GLOBAL_CHANCE))
				{
					ItemInstance i = ItemFunctions.createItem(5203);
					i.dropToTheGround(killer.getPlayer(), (NpcInstance) victim);
				}
				else if(victim.getLevel() >= 51 && victim.getLevel() <= 75 && Rnd.chance(GLOBAL_CHANCE))
				{
					ItemInstance i = ItemFunctions.createItem(5204);
					i.dropToTheGround(killer.getPlayer(), (NpcInstance) victim);
				}
			}
		}

		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}

	}
}
