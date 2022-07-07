package l2s.gameserver.model.entity.residence.clanhall;

import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.residence.ClanHallType;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.StatsSet;

public class AuctionClanHall extends NormalClanHall
{
	private static final long serialVersionUID = 1L;
	private static final int REWARD_CYCLE = 168;
	private int _auctionLength;
	private long _auctionMinBid;
	private String _auctionDescription;
	private final int _grade;
	private final int _feeItemId;
	private final long _rentalFee;
	private final long _minBid;
	private final long _deposit;

	public AuctionClanHall(StatsSet set)
	{
		super(set);
		_auctionDescription = "";
		_grade = set.getInteger("grade");
		_feeItemId = set.getInteger("fee_item_id");
		_rentalFee = set.getInteger("rental_fee");
		_minBid = set.getInteger("min_bid");
		_deposit = set.getInteger("deposit");
	}

	@Override
	public void init()
	{
		super.init();
		if(getSiegeEvent() != null && getSiegeEvent().getClass() == ClanHallAuctionEvent.class && _owner != null && getAuctionLength() == 0)
			startCycleTask();
	}

	@Override
	public int getGrade()
	{
		return _grade;
	}

	@Override
	public void changeOwner(Clan clan)
	{
		super.changeOwner(clan);
		if(clan == null && getSiegeEvent().getClass() == ClanHallAuctionEvent.class)
            getSiegeEvent().reCalcNextTime(false);
	}

	@Override
	public int getAuctionLength()
	{
		return _auctionLength;
	}

	@Override
	public void setAuctionLength(int auctionLength)
	{
		_auctionLength = auctionLength;
	}

	@Override
	public String getAuctionDescription()
	{
		return _auctionDescription;
	}

	@Override
	public void setAuctionDescription(String auctionDescription)
	{
		_auctionDescription = auctionDescription == null ? "" : auctionDescription;
	}

	@Override
	public long getAuctionMinBid()
	{
		return _auctionMinBid;
	}

	@Override
	public void setAuctionMinBid(long auctionMinBid)
	{
		_auctionMinBid = auctionMinBid;
	}

	@Override
	public int getFeeItemId()
	{
		return _feeItemId;
	}

	@Override
	public long getRentalFee()
	{
		return _rentalFee;
	}

	@Override
	public long getBaseMinBid()
	{
		return _minBid;
	}

	@Override
	public long getDeposit()
	{
		return _deposit;
	}

	@Override
	public void chanceCycle()
	{
		super.chanceCycle();
		if(getPaidCycle() >= REWARD_CYCLE)
			if(_owner.getWarehouse().getCountOf(57) > _rentalFee)
			{
				_owner.getWarehouse().destroyItemByItemId(57, _rentalFee);
				setPaidCycle(0);
			}
			else
			{
				UnitMember member = _owner.getLeader();
				if(member.isOnline())
					member.getPlayer().sendPacket(SystemMsg.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED);
				else
					PlayerMessageStack.getInstance().mailto(member.getObjectId(), SystemMsg.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED.packet(null));
				changeOwner(null);
			}
	}

	@Override
	public int getVisibleFunctionLevel(int level)
	{
		return level;
	}

	@Override
	public ClanHallType getClanHallType()
	{
		return ClanHallType.AUCTIONABLE;
	}
}
