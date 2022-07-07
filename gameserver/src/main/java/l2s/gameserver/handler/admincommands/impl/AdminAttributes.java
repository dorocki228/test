package l2s.gameserver.handler.admincommands.impl;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;

public class AdminAttributes implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;
		int armorType = -1;
		switch(command)
		{
			case admin_attribute:
			{
				showMainPage(activeChar);
				return true;
			}
			case admin_setatteh:
			{
				armorType = 6;
				break;
			}
			case admin_setattec:
			{
				armorType = 10;
				break;
			}
			case admin_setatteg:
			{
				armorType = 9;
				break;
			}
			case admin_setstteb:
			{
				armorType = 12;
				break;
			}
			case admin_setattel:
			{
				armorType = 11;
				break;
			}
			case admin_setattew:
			{
				armorType = 7;
				break;
			}
			case admin_setattes:
			{
				armorType = 8;
				break;
			}
			case admin_setattle:
			{
				armorType = 2;
				break;
			}
			case admin_setattre:
			{
				armorType = 1;
				break;
			}
			case admin_setattlf:
			{
				armorType = 5;
				break;
			}
			case admin_setattrf:
			{
				armorType = 4;
				break;
			}
			case admin_setatten:
			{
				armorType = 3;
				break;
			}
			case admin_setattha:
			{
				armorType = 15;
				break;
			}
			case admin_setattdha:
			{
				armorType = 15;
				break;
			}
			case admin_setattlbr:
			{
				armorType = 18;
				break;
			}
		}
		if(armorType == -1 || wordList.length < 7)
		{
			showMainPage(activeChar);
			return true;
		}
		try
		{
			int fire = Integer.parseInt(wordList[1]);
			int water = Integer.parseInt(wordList[2]);
			int earth = Integer.parseInt(wordList[3]);
			int wind = Integer.parseInt(wordList[4]);
			int holy = Integer.parseInt(wordList[5]);
			int dark = Integer.parseInt(wordList[6]);
			if(fire < 0 || water < 0 || earth < 0 || wind < 0 || holy < 0 || dark < 0 || fire > 65535 || water > 65535 || earth > 65535 || wind > 65535 || holy > 65535 || dark > 65535)
				activeChar.sendMessage("You must set the attribute level to be between 0-65535.");
			else
				setAttribute(activeChar, fire, water, earth, wind, holy, dark, armorType);
		}
		catch(StringIndexOutOfBoundsException e)
		{
			activeChar.sendMessage("Please specify a new attribute value.");
		}
		catch(NumberFormatException e2)
		{
			activeChar.sendMessage("Please specify a valid new attribute value.");
		}
		showMainPage(activeChar);
		return true;
	}

	private void setAttribute(Player activeChar, int fire, int water, int earth, int wind, int holy, int dark, int armorType)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		if(!target.isPlayer())
		{
			activeChar.sendMessage("Wrong target type.");
			return;
		}
		Player player = (Player) target;
        ItemInstance itemInstance = player.getInventory().getPaperdollItem(armorType);
		if(itemInstance != null)
		{
			if(itemInstance.isWeapon())
			{
				if(fire > 0 && (water > 0 || earth > 0 || wind > 0 || holy > 0 || dark > 0))
				{
					activeChar.sendMessage("Error! Cannot attribute weapon item on two or more attributes! Please, select one attribute.");
					return;
				}
				if(water > 0 && (fire > 0 || earth > 0 || wind > 0 || holy > 0 || dark > 0))
				{
					activeChar.sendMessage("Error! Cannot attribute weapon item on two or more attributes! Please, select one attribute.");
					return;
				}
				if(earth > 0 && (fire > 0 || water > 0 || wind > 0 || holy > 0 || dark > 0))
				{
					activeChar.sendMessage("Error! Cannot attribute weapon item on two or more attributes! Please, select one attribute.");
					return;
				}
				if(wind > 0 && (fire > 0 || water > 0 || earth > 0 || holy > 0 || dark > 0))
				{
					activeChar.sendMessage("Error! Cannot attribute weapon item on two or more attributes! Please, select one attribute.");
					return;
				}
				if(holy > 0 && (fire > 0 || water > 0 || earth > 0 || wind > 0 || dark > 0))
				{
					activeChar.sendMessage("Error! Cannot attribute weapon item on two or more attributes! Please, select one attribute.");
					return;
				}
				if(dark > 0 && (fire > 0 || water > 0 || earth > 0 || wind > 0 || holy > 0))
				{
					activeChar.sendMessage("Error! Cannot attribute weapon item on two or more attributes! Please, select one attribute.");
					return;
				}
			}
            int curFire = itemInstance.getAttributes().getValue(Element.FIRE);
            int curWater = itemInstance.getAttributes().getValue(Element.WATER);
            int curEarth = itemInstance.getAttributes().getValue(Element.EARTH);
            int curWind = itemInstance.getAttributes().getValue(Element.WIND);
            int curHoly = itemInstance.getAttributes().getValue(Element.HOLY);
            int curDark = itemInstance.getAttributes().getValue(Element.UNHOLY);
            boolean equipped = false;
			if(equipped = itemInstance.isEquipped())
			{
				player.getInventory().isRefresh = true;
				player.getInventory().unEquipItem(itemInstance);
			}
			itemInstance.setAttributeElement(Element.FIRE, fire);
			itemInstance.setAttributeElement(Element.WATER, water);
			itemInstance.setAttributeElement(Element.EARTH, earth);
			itemInstance.setAttributeElement(Element.WIND, wind);
			itemInstance.setAttributeElement(Element.HOLY, holy);
			itemInstance.setAttributeElement(Element.UNHOLY, dark);
			itemInstance.setJdbcState(JdbcEntityState.UPDATED);
			itemInstance.update();
			if(equipped)
			{
				player.getInventory().equipItem(itemInstance);
				player.getInventory().isRefresh = false;
			}
			player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, itemInstance));
			player.broadcastCharInfo();
			activeChar.sendMessage("Changed attributes of " + player.getName() + "'s " + itemInstance.getName() + " from Fire[" + curFire + "] Water[" + curWater + "] Earth[" + curEarth + "] Wind[" + curWind + "] Holy[" + curHoly + "] Dark[" + curDark + "] to Fire[" + fire + "] Water[" + water + "] Earth[" + earth + "] Wind[" + wind + "] Holy[" + holy + "] Dark[" + dark + "].");
			player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getName() + " from Fire[" + curFire + "] Water[" + curWater + "] Earth[" + curEarth + "] Wind[" + curWind + "] Holy[" + curHoly + "] Dark[" + curDark + "] to Fire[" + fire + "] Water[" + water + "] Earth[" + earth + "] Wind[" + wind + "] Holy[" + holy + "] Dark[" + dark + "].");
		}
	}

	public void showMainPage(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		Player player = activeChar;
		if(target.isPlayer())
			player = (Player) target;
		HtmlMessage adminReply = new HtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><table width=720><tr><td width=40>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</td><td width=50>&nbsp;</td><td width=50>&nbsp;</td><td width=50>&nbsp;</td><td width=50>&nbsp;</td></tr></table></center><br>");
		replyMSG.append("<center>Attribute equip for player: " + player.getName() + "</center><br>");
		replyMSG.append("<center><table width=270><tr><td>");
		replyMSG.append("<button value=\"Helmet\" action=\"bypass -h admin_setatteh $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Mask\" action=\"bypass -h admin_setattha $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Hair\" action=\"bypass -h admin_setattdha $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"L-Bracelet\" action=\"bypass -h admin_setattlbr $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Necklace\" action=\"bypass -h admin_setatten $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
		replyMSG.append("</center><center><table width=270><tr><td>");
		replyMSG.append("<button value=\"Weapon\" action=\"bypass -h admin_setattew $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Chest\" action=\"bypass -h admin_setattec $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Shield\" action=\"bypass -h admin_setattes $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Earring\" action=\"bypass -h admin_setattre $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Earring\" action=\"bypass -h admin_setattle $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
		replyMSG.append("</center><center><table width=270><tr><td>");
		replyMSG.append("<button value=\"Gloves\" action=\"bypass -h admin_setatteg $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Leggings\" action=\"bypass -h admin_setattel $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Boots\" action=\"bypass -h admin_setstteb $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Ring\" action=\"bypass -h admin_setattrf $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Ring\" action=\"bypass -h admin_setattlf $menu_command_0 $menu_command_1 $menu_command_2 $menu_command_3 $menu_command_4 $menu_command_5\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
		replyMSG.append("</center><br>");
		replyMSG.append("<center>[0 - 65535]<br></center>");
		replyMSG.append("<center><table width=270>");
		replyMSG.append("<tr><td>Fire:</td><td><edit var=\"menu_command_0\" width=50 height=12></td><td>&nbsp;</td><td>Water:</td><td><edit var=\"menu_command_1\" width=50 height=12></td></tr>");
		replyMSG.append("<tr><td>Earth:</td><td><edit var=\"menu_command_2\" width=50 height=12></td><td>&nbsp;</td><td>Wind:</td><td><edit var=\"menu_command_3\" width=50 height=12></td></tr>");
		replyMSG.append("<tr><td>Holy:</td><td><edit var=\"menu_command_4\" width=50 height=12></td><td>&nbsp;</td><td>Dark:</td><td><edit var=\"menu_command_5\" width=50 height=12></td></tr>");
		replyMSG.append("</center></table>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private enum Commands
	{
		admin_setatteh,
		admin_setattec,
		admin_setatteg,
		admin_setattel,
		admin_setstteb,
		admin_setattew,
		admin_setattes,
		admin_setattle,
		admin_setattre,
		admin_setattlf,
		admin_setattrf,
		admin_setatten,
		admin_setattha,
		admin_setattdha,
		admin_setattlbr,
		admin_setattbelt,
		admin_attribute
    }
}
