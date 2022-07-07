package  l2s.Phantoms.taskmanager;

import  l2s.commons.threading.RunnableImpl;
import  l2s.gameserver.handler.items.IItemHandler;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.items.ItemInstance;

public class BSoeTask extends RunnableImpl
	{
		public Player member;
		
		public BSoeTask(Player ph)
		{
			member = ph;
		}
		
		@Override
		public void runImpl()
		{
			if (!member.isDead())
			{
				ItemInstance item = member.getInventory().getItemByItemId(1538);
				if (item != null)
				{
					IItemHandler handler = item.getTemplate().getHandler();
					handler.useItem(member, item, false);
					//GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", "bsoe cast:" + member));
				}
			}
			else
			{
					member.teleToClosestTown();
					member.doRevive(); // воскрешаем
			}
		}
		
	}