package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.c2s.RequestBuySeed;
import l2s.gameserver.network.l2.c2s.RequestProcureCropList;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.templates.manor.SeedProduction;
import l2s.gameserver.templates.npc.BuyListTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class ManorManagerInstance extends MerchantInstance
{
	private static final long serialVersionUID = 1L;

	public ManorManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		//		if(this != player.getTarget())
		//		{
		//			player.setTarget(this);
		//			player.sendPacket(new ValidateLocationPacket(this));
		//		}
		//		else
		//		{
		//			player.sendPacket(new MyTargetSelectedPacket(player, this));
		//			if(!checkInteractionDistance(player))
		//			{
		//				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		//				player.sendActionFailed();
		//			}
		//			else
		//			{
		//				if(CastleManorManager.getInstance().isDisabled())
		//				{
		//					final HtmlMessage html = new HtmlMessage(this);
		//					html.setFile("npcdefault.htm");
		//					player.sendPacket(html);
		//				}
		//				else if(!player.isGM() && player.isClanLeader() && this.getCastle() != null && this.getCastle().getOwnerId() == player.getClanId())
		//					showMessageWindow(player, "manager-lord.htm");
		//				else
		//					showMessageWindow(player, "manager.htm");
		//				player.sendActionFailed();
		//			}
		//		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("manor_menu_select"))
		{
			//			if(CastleManorManager.getInstance().isUnderMaintenance())
			//			{
			//				player.sendPacket(ActionFailPacket.STATIC, SystemMsg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
			//				return;
			//			}
			String params = command.substring(command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			int state = Integer.parseInt(st.nextToken().split("=")[1]);
			int time = Integer.parseInt(st.nextToken().split("=")[1]);
			Castle castle = getCastle();
			int castleId;
			if(state == -1)
				castleId = castle.getId();
			else
				castleId = state;
			switch(ask)
			{
				case 1:
				{
					if(castleId != castle.getId())
					{
						player.sendPacket(SystemMsg._HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
						break;
					}
					BuyListTemplate tradeList = new BuyListTemplate(0, 0, 1);
					List<SeedProduction> seeds = castle.getSeedProduction(0);
					for(SeedProduction s : seeds)
					{
						TradeItem item = new TradeItem();
						item.setItemId(s.getId());
						item.setOwnersPrice(s.getPrice());
						item.setCount(s.getCanProduce());
						if(item.getCount() > 0L && item.getOwnersPrice() > 0L)
							tradeList.addItem(item);
					}
					BuyListSeedPacket bl = new BuyListSeedPacket(tradeList, castleId, player.getAdena());
					player.sendPacket(bl);
					break;
				}
				case 2:
				{
					player.sendPacket(new ExShowSellCropListPacket(player, castleId, castle.getCropProcure(0)));
					break;
				}
				case 3:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowSeedInfoPacket(castleId, Collections.emptyList()));
						break;
					}
					player.sendPacket(new ExShowSeedInfoPacket(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getSeedProduction(time)));
					break;
				}
				case 4:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowCropInfoPacket(castleId, Collections.emptyList()));
						break;
					}
					player.sendPacket(new ExShowCropInfoPacket(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getCropProcure(time)));
					break;
				}
				case 5:
				{
					player.sendPacket(new ExShowManorDefaultInfoPacket());
					break;
				}
				case 6:
				{
                    showShopWindow(player, 1, false);
					break;
				}
				case 9:
				{
					player.sendPacket(new ExShowProcureCropDetail(state));
					break;
				}
			}
		}
		else if(command.startsWith("help"))
		{
			StringTokenizer st2 = new StringTokenizer(command, " ");
			st2.nextToken();
			String filename = "manor_client_help00" + st2.nextToken() + ".htm";
			showMessageWindow(player, filename);
		}
		else
			super.onBypassFeedback(player, command);
	}

	public String getHtmlPath()
	{
		return "manormanager/";
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return getHtmlPath();
	}

	@Override
	public String getHtmlFilename(int val, Player player)
	{
		return "manager.htm";
	}

	private void showMessageWindow(Player player, String filename)
	{
		HtmlMessage html = new HtmlMessage(this);
		html.setFile(getHtmlPath() + filename);
		player.sendPacket(html);
	}

	@Override
	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestBuySeed.class || packet == RequestProcureCropList.class || super.canPassPacket(player, packet, arg);
	}
}
