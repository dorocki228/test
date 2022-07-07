package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.clanhall.InstantClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InstantClanHallAuctionEvent extends SiegeEvent<InstantClanHall, SiegeClanObject>
{
	private final Calendar _startAuctionDate;

	public InstantClanHallAuctionEvent(MultiValueSet<String> set)
	{
		super(set);
		_startAuctionDate = Calendar.getInstance();
	}

	@Override
	public void reCalcNextTime(boolean onStart)
	{
		clearActions();
		_onTimeActions.clear();
		Calendar siegeDate = getResidence().getSiegeDate();
		if(siegeDate.getTimeInMillis() <= System.currentTimeMillis())
		{
			if(onStart)
				checkWinners();
			siegeDate.setTimeInMillis(getResidence().getFirstLotteryDate().getTimeInMillis());
			siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			siegeDate.set(Calendar.HOUR_OF_DAY, 20);
			siegeDate.set(Calendar.MINUTE, 55);
			siegeDate.set(Calendar.SECOND, 0);
			siegeDate.set(Calendar.MILLISECOND, 0);
			while(siegeDate.getTimeInMillis() <= System.currentTimeMillis())
				siegeDate.add(5, getResidence().getRentalPeriod());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
		}
		long auctionPeriod = getResidence().getApplyPeriod() * 60 * 60 * 1000L;
		_startAuctionDate.setTimeInMillis(siegeDate.getTimeInMillis() - auctionPeriod);
		_startAuctionDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		_startAuctionDate.set(Calendar.HOUR_OF_DAY, 0);
		_startAuctionDate.set(Calendar.MINUTE, 1);
		_startAuctionDate.set(Calendar.SECOND, 0);
		_startAuctionDate.set(Calendar.MILLISECOND, 0);
		while(siegeDate.getTimeInMillis() <= _startAuctionDate.getTimeInMillis())
			_startAuctionDate.add(5, -7);
		addOnTimeAction((int) ((_startAuctionDate.getTimeInMillis() - System.currentTimeMillis()) / 1000L), new StartStopAction("event", true));
		addOnTimeAction((int) ((siegeDate.getTimeInMillis() - _startAuctionDate.getTimeInMillis()) / 1000L), new StartStopAction("event", false));
		registerActions();
	}

	@Override
	public void startEvent()
	{
		for(Clan clan : getResidence().getOwners())
		{
			getResidence().removeOwner(clan, true);
			clan.setHasHideout(0);
			clan.broadcastClanStatus(true, false, false);
		}
		super.startEvent();
	}

	@Override
	public void stopEvent(boolean force)
	{
		checkWinners();
		reCalcNextTime(false);
		super.stopEvent(force);
	}

	private void checkWinners()
	{
		List<SiegeClanObject> siegeClanObjects = removeObjects("attackers");
		if(!siegeClanObjects.isEmpty())
		{
			siegeClanObjects.removeIf(siegeClan -> siegeClan.getClan().getHasHideout() != 0);
			List<SiegeClanObject> winnersSiegeClans = new ArrayList<>();
			if(siegeClanObjects.size() <= getResidence().getMaxCount())
				winnersSiegeClans.addAll(siegeClanObjects);
			else
			{
				while(winnersSiegeClans.size() < getResidence().getMaxCount())
				{
					SiegeClanObject winnerSiegeClan = Rnd.get(siegeClanObjects);
					winnersSiegeClans.add(winnerSiegeClan);
					siegeClanObjects.remove(winnerSiegeClan);
				}
				for(SiegeClanObject siegeClan2 : siegeClanObjects)
				{
					siegeClan2.getClan().broadcastToOnlineMembers(SystemMsg.YOUR_BID_FOR_THE_PROVISIONAL_CLAN_HALL_LOST);
					siegeClan2.getClan().getWarehouse().addItem(57, getResidence().getRentalFee() * (100 - getResidence().getCommissionPercent()) / 100L);
				}
			}
			if(!winnersSiegeClans.isEmpty())
				for(SiegeClanObject siegeClan2 : winnersSiegeClans)
				{
					Clan clan = siegeClan2.getClan();
					getResidence().addOwner(clan, true);
					clan.setHasHideout(getResidence().getId());
					clan.broadcastClanStatus(true, false, false);
					clan.broadcastToOnlineMembers(SystemMsg.YOUR_BID_FOR_THE_PROVISIONAL_CLAN_HALL_WON);
				}
		}
		SiegeClanDAO.getInstance().delete(getResidence());
	}

	@Override
	public void findEvent(Player player)
	{}

	@Override
	public Calendar getSiegeDate()
	{
		return getStartAuctionDate();
	}

	public Calendar getStartAuctionDate()
	{
		return _startAuctionDate;
	}

	public Calendar getEndAuctionDate()
	{
		return getResidence().getSiegeDate();
	}

	public int getParticipantsCount()
	{
		return getObjects("attackers").size();
	}
}
