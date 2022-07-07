package npc.model.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.Privilege;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public class CastleMessengerInstance extends NpcInstance
{
	public CastleMessengerInstance(int objectID, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectID, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{

        StringBuilder sb = new StringBuilder();

		sb.append("<font color=LEVEL>Fortress:</font><br>");

		Fortress fort = ResidenceHolder.getInstance().getResidence(Fortress.class, 400);
		Fraction fraction = fort.getFraction();
		long lastSiegeDateMillis = fort.getLastSiegeDate().getTimeInMillis();
		String owner = fort.getOwner() != null ? fort.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		String nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(fort.getName()).append("</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(fort.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (lastSiegeDateMillis + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(fort.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

		fort = ResidenceHolder.getInstance().getResidence(Fortress.class, 401);
		fraction = fort.getFraction();
		lastSiegeDateMillis = fort.getLastSiegeDate().getTimeInMillis();
		owner = fort.getOwner() != null ? fort.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(fort.getName()).append("</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(fort.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (lastSiegeDateMillis + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(fort.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

		fort = ResidenceHolder.getInstance().getResidence(Fortress.class, 402);
		fraction = fort.getFraction();
		lastSiegeDateMillis = fort.getLastSiegeDate().getTimeInMillis();
		owner = fort.getOwner() != null ? fort.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(fort.getName()).append("</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(fort.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (lastSiegeDateMillis + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(fort.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

		sb.append("<font color=\"LEVEL\">Castles:</font><br>");

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, 4);
		fraction = castle.getFraction();
		owner = castle.getOwner() != null ? castle.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(castle.getName()).append(" Castle</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(castle.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (castle.getOwnDate().getTimeInMillis() + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(castle.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

		castle = ResidenceHolder.getInstance().getResidence(Castle.class, 10);
		fraction = castle.getFraction();
		owner = castle.getOwner() != null ? castle.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(castle.getName()).append(" Castle</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(castle.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (castle.getOwnDate().getTimeInMillis() + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(castle.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

		castle = ResidenceHolder.getInstance().getResidence(Castle.class, 8);
		fraction = castle.getFraction();
		owner = castle.getOwner() != null ? castle.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(castle.getName()).append(" Castle</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(castle.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (castle.getOwnDate().getTimeInMillis() + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(castle.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

		castle = ResidenceHolder.getInstance().getResidence(Castle.class, 9);
		fraction = castle.getFraction();
		owner = castle.getOwner() != null ? castle.getOwner().getName() : fraction == Fraction.FIRE ? "Fire" : fraction == Fraction.WATER ? "Water" : "None";
		nameColor = fraction == Fraction.FIRE ? "FF901E" : fraction == Fraction.WATER ? "0076EE" : "FFFFFF";
		sb.append("<table><tr>");
		sb.append("<td width=100>").append(castle.getName()).append(" Castle</td><td width=100><font color=\"").append(nameColor).append("\">").append(owner).append("</font></td><td width=70>");
		if(castle.getOwner() == null && fraction != Fraction.NONE && !fraction.canAttack(player.getFraction()) && (castle.getOwnDate().getTimeInMillis() + SiegeEvent.AUCTION_TIME) >= System.currentTimeMillis())
			sb.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_bid ").append(castle.getId()).append("\">Bet</a></font>");
		sb.append("</td></tr></table><br>");

        String html = "gve/residence_auction/bid.htm";
        showChatWindow(player, html, firstTalk, "%residences%", sb.toString());
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("bid"))
		{
			StringTokenizer st = new StringTokenizer(command);

			st.nextToken();

			if(st.hasMoreTokens())
			{
				try
				{
					int id = Integer.parseInt(st.nextToken());
					Residence r = ResidenceHolder.getInstance().getResidence(id);
					Fraction f = r.getFraction();

					if(!st.hasMoreTokens())
					{

						String name = r.getName() + (r.isCastle() ? " Castle" : "");

						SiegeClanObject bidder = r.getSiegeEvent().getFirstObject(SiegeEvent.ATTACKERS);
						StringBuilder sb = new StringBuilder();

						if(bidder != null)
						{
							String nameColor = f == Fraction.FIRE ? "FF901E" : f == Fraction.WATER ? "0076EE" : "FFFFFF";

							sb.append("<table><tr>");
							sb.append("<td><font color=").append(nameColor).append(">").append(bidder.getClan().getName()).append(" ").append(bidder.getParam()).append(" ").append(bidder.getClan().getLeaderName()).append("</font></td>");
							sb.append("</tr></table>");
						}
						showChatWindow(player, "gve/residence_auction/info.htm", false, "%name%", name, "%bid_info%", sb.toString(), "%id%", r.getId());
					}
					else
					{
						int count = Integer.parseInt(st.nextToken());
						Residence residence = ResidenceHolder.getInstance().getResidence(id);
						long ownDateInMillis = residence.getOwnDate().getTimeInMillis();
						long lastSiegeDateMillis = residence.getLastSiegeDate().getTimeInMillis();
						long endTime = (residence.isCastle() ? ownDateInMillis : lastSiegeDateMillis) + SiegeEvent.AUCTION_TIME;
						if(residence == null || residence.getSiegeEvent() == null || endTime < System.currentTimeMillis())
						{
							// Срок ставки за наградной холл не наступил.
							player.sendPacket(SystemMsg.IT_IS_NOT_THE_BIDDING_PERIOD_FOR_THE_PROVISIONAL_CLAN_HALL);
							return;
						}

						Clan clan = player.getClan();
						if(clan == null)
						{
							// Клан-владелец отсутствует. Ставки невозможны.
							player.sendPacket(SystemMsg.YOU_CANNOT_MAKE_A_BID_BECAUSE_YOU_DONT_BELONG_TO_A_CLAN);
							return;
						}

						if(clan.getHasHideout() != 0 || residence.getSiegeEvent().getSiegeClan(SiegeEvent.ATTACKERS, clan) != null)
						{
							// Вы уже внесли ставку за наградной холл.
							player.sendPacket(SystemMsg.YOU_ALREADY_MADE_A_BID_FOR_THE_PROVISIONAL_CLAN_HALL);
							return;
						}

						if(player.getClan().isPlacedForDisband())
						{
							// Вы уже подали заявку на расформирование клана.
							player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
							return;
						}

						int minBid = 100;
						AuctionSiegeClanObject bidder = r.getSiegeEvent().getFirstObject(SiegeEvent.ATTACKERS);
						if(bidder != null)
							minBid += bidder.getParam();

						if(count <= minBid)
						{
							player.sendMessage("Min bid is more than " + minBid + " adena.");
							return;
						}

						if(bidder != null && player.getClan().getClanId() == bidder.getClan().getClanId())
						{
							//
							return;
						}

						if(!player.hasPrivilege(Privilege.CH_AUCTION) || clan.getWarehouse().getCountOf(ItemTemplate.ITEM_ID_ADENA) < count)
						{
							// Для внесения ставки  необходимо соответствовать условиям участия в аукционе холлов.
							return;
						}

						if(bidder != null)
						{
							long returnBid = bidder.getParam() - (long) (bidder.getParam() * 0.15);
							bidder.getClan().getWarehouse().addItem(ItemTemplate.ITEM_ID_ADENA, returnBid);
							residence.getSiegeEvent().removeObject(SiegeEvent.ATTACKERS, bidder);
							SiegeClanDAO.getInstance().delete(residence);
						}

						AuctionSiegeClanObject siegeClan = new AuctionSiegeClanObject(SiegeEvent.ATTACKERS, clan, count);
						residence.getSiegeEvent().addObject(SiegeEvent.ATTACKERS, siegeClan);
						SiegeClanDAO.getInstance().insert(residence, siegeClan);
						clan.getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, count);
						player.sendPacket(new SystemMessagePacket(SystemMsg.YOU_MADE_A_BID_AT_S1).addString(clan.getName()));
					}

				}
				catch(Exception e)
				{
					return;
				}
			}

		}
		else
			super.onBypassFeedback(player, command);
	}
}
