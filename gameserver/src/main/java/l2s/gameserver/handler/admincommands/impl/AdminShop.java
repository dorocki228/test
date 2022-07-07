package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.templates.npc.BuyListTemplate;

public class AdminShop implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().UseGMShop)
			return false;
		switch(command)
		{
			case admin_buy:
			{
				try
				{
					handleBuyRequest(activeChar, fullString.substring(10));
				}
				catch(IndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify buylist.");
				}
				break;
			}
			case admin_gmshop:
			{
				activeChar.sendPacket(new HtmlMessage(5).setFile("admin/gmshops.htm"));
				break;
			}
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleBuyRequest(Player activeChar, String command)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch(Exception ex)
		{}
		BuyListTemplate list = BuyListHolder.getInstance().getBuyList(-1, val);
		if(list != null)
			activeChar.sendPacket(new ExBuySellListPacket.BuyList(list, activeChar, 0.0), new ExBuySellListPacket.SellRefundList(activeChar, false, 0.0));
		activeChar.sendActionFailed();
	}

	private enum Commands
	{
		admin_buy,
		admin_gmshop
    }
}
