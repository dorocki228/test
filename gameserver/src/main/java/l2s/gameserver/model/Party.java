package l2s.gameserver.model;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.Phantoms.templates.PhantomItem;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.PartyListener;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.actor.listener.PartyListenerList;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.service.PartyService;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class Party implements PlayerGroup
{
	public static final int MAX_SIZE;
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	private final List<Player> _members;
	private int _partyLvl;
	private int _itemDistribution;
	private int _itemOrder;
	private int _dimentionalRift;
	private Reflection _reflection;
	private CommandChannel _commandChannel;
	public double _rateExp;
	public double _rateSp;
	public double _rateDrop;
	public double _rateAdena;
	public double _rateSpoil;
	public double _dropChanceMod;
	public double _spoilChanceMod;
	private ScheduledFuture<?> positionTask;
	private int _requestChangeLoot;
	private long _requestChangeLootTimer;
	private Set<Integer> _changeLootAnswers;
	private static final int[] LOOT_SYSSTRINGS;
	private static final int[] TACTICAL_SYSSTRINGS;
	private Future<?> _checkTask;
	private final TIntObjectHashMap<Creature> _tacticalTargets;
	private long lastBuff;
	private final PartyListenerList listenerList = new PartyListenerList(this);

	public Party(Player leader, int itemDistribution)
	{
		_members = new CopyOnWriteArrayList<>();
		_partyLvl = 0;
		_itemDistribution = 0;
		_itemOrder = 0;
		_requestChangeLoot = -1;
		_requestChangeLootTimer = 0L;
		_changeLootAnswers = null;
		_checkTask = null;
		_tacticalTargets = new TIntObjectHashMap<>(4);
		_itemDistribution = itemDistribution;
		_members.add(leader);
		_partyLvl = leader.getLevel();
		_rateExp = leader.getRateExp(true);
		_rateSp = leader.getRateSp(true);
		_rateAdena = leader.getRateAdena(true);
		_rateDrop = leader.getRateItems(true);
		_rateSpoil = leader.getRateSpoil(true);
		_dropChanceMod = leader.getDropChanceMod(true);
		_spoilChanceMod = leader.getSpoilChanceMod(true);
	}

	@Override
	public int getMemberCount()
	{
		return _members.size();
	}

	public int getMemberCountInRange(Player player, int range)
	{
		int count = 0;
		for(Player member : _members)
			if(member == player || member.isInRangeZ(player, range))
				++count;
		return count;
	}

	public List<Player> getPartyMembers()
	{
		return _members;
	}

	public List<Integer> getPartyMembersObjIds()
	{
		List<Integer> result = new ArrayList<>(_members.size());
		for(Player member : _members)
			result.add(member.getObjectId());
		return result;
	}

	public List<Playable> getPartyMembersWithPets()
	{
		List<Playable> result = new ArrayList<>();
		for(Player member : _members)
		{
			result.add(member);
            result.addAll(member.getServitors());
		}
		return result;
	}

	public Player getRandomPartyMember()
	{
		return Rnd.get(_members);
	}

	private Player getNextLooterInRange(Player player, ItemInstance item, int range)
	{
		synchronized (_members)
		{
			int antiloop = _members.size();
			while(--antiloop > 0)
			{
				int looter = _itemOrder;
				++_itemOrder;
				if(_itemOrder > _members.size() - 1)
					_itemOrder = 0;
				Player ret = looter < _members.size() ? _members.get(looter) : player;
				if(ret != null && !ret.isDead() && ret.isInRangeZ(player, range) && ret.getInventory().validateCapacity(item) && ret.getInventory().validateWeight(item))
					return ret;
			}
		}
		return player;
	}

	public boolean isLeader(Player player)
	{
		return getPartyLeader() == player;
	}

	public Player getPartyLeader()
	{
		synchronized (_members)
		{
			if(_members.isEmpty())
				return null;
			return _members.get(0);
		}
	}

	@Override
	public void broadCast(IBroadcastPacket... msg)
	{
		for(Player member : _members)
			member.sendPacket(msg);
	}

	public void broadcastMessageToPartyMembers(String msg)
	{
		broadCast(new SystemMessage(msg));
	}

	public void broadcastCustomMessageToPartyMembers(String address, String... replacements)
	{
		for(Player member : _members)
		{
			CustomMessage cm = new CustomMessage(address);
			for(String s : replacements)
				cm.addString(s);
			member.sendMessage(cm);
		}
	}

	public void broadcastToPartyMembers(Player exclude, IBroadcastPacket msg)
	{
		for(Player member : _members)
			if(exclude != member)
				member.sendPacket(msg);
	}

	public void broadcastToPartyMembersInRange(Player player, IBroadcastPacket msg, int range)
	{
		for(Player member : _members)
			if(player.isInRangeZ(member, range))
				member.sendPacket(msg);
	}

	public boolean containsMember(Player player)
	{
		return _members.contains(player);
	}

	public int indexOf(Player player)
	{
		return _members.indexOf(player);
	}

	public boolean addPartyMember(Player player)
	{
		Player leader = getPartyLeader();
		if(leader == null)
			return false;
		synchronized (_members)
		{
			if(_members.isEmpty())
				return false;
			if(_members.contains(player))
				return false;
			if(_members.size() == MAX_SIZE)
				return false;
			_members.add(player);
		}

		PartyService.getInstance().addParty(this);

		if(_requestChangeLoot != -1)
			finishLootRequest(false);
		player.setParty(this);
		player.getListeners().onPartyInvite();
		List<IBroadcastPacket> addInfo = new ArrayList<>(4 + _members.size() * 4);
		List<IBroadcastPacket> pplayer = new ArrayList<>(20);
		pplayer.add(new PartySmallWindowAllPacket(this, player));
		pplayer.add(new SystemMessage(106).addName(leader));
		addInfo.add(new SystemMessage(107).addName(player));
		addInfo.add(new PartySpelledPacket(player, true));
		for(Servitor servitor : player.getServitors())
		{
			addInfo.add(new ExPartyPetWindowAdd(servitor));
			addInfo.add(new PartySpelledPacket(servitor, true));
		}
		RelationChangedPacket rcp = new RelationChangedPacket();
		PartyMemberPositionPacket pmp = new PartyMemberPositionPacket();
		for(Player member : _members)
			if(member != player)
			{
				List<IBroadcastPacket> pmember = new ArrayList<>(addInfo.size() + 4);
				pmember.addAll(addInfo);
				pmember.add(new PartySmallWindowAddPacket(member, player));
				pmember.add(new PartyMemberPositionPacket().add(player));
				RelationChangedPacket memberrcp = new RelationChangedPacket(player, member);
				for(Servitor servitor2 : player.getServitors())
					memberrcp.add(servitor2, member);
				pmember.add(memberrcp);
				member.sendPacket(pmember);
				pplayer.add(new PartySpelledPacket(member, true));
				for(Servitor servitor2 : player.getServitors())
				{
					pplayer.add(new PartySpelledPacket(servitor2, true));
					servitor2.broadcastCharInfoImpl(member, NpcInfoType.VALUES);
				}
				rcp.add(member, player);
				for(Servitor servitor2 : member.getServitors())
					rcp.add(servitor2, player);
				pmp.add(member);
			}
		pplayer.add(rcp);
		pplayer.add(pmp);
		if(isInCommandChannel())
			pplayer.add(ExOpenMPCCPacket.STATIC);
		player.sendPacket(pplayer);
		startUpdatePositionTask();
		recalculatePartyData();
		sendTacticalSign(player);
		MatchingRoom currentRoom = player.getMatchingRoom();
		MatchingRoom room = leader.getMatchingRoom();
		if(currentRoom != null && currentRoom != room)
			currentRoom.removeMember(player, false);
		if(room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
			room.addMemberForce(player);
		else
			MatchingRoomManager.getInstance().removeFromWaitingList(player);
		return true;
	}

	public void dissolveParty()
	{
		listenerList.onDissolve();
		for(Player p : _members)
		{
			p.sendPacket(PartySmallWindowDeleteAllPacket.STATIC);
			p.setParty(null);
		}

		PartyService.getInstance().removeParty(this);

		synchronized (_members)
		{
			_members.clear();
		}

		setCommandChannel(null);
		stopUpdatePositionTask();
	}

	public boolean removePartyMember(Player player, boolean kick)
	{
		boolean isLeader = isLeader(player);
		boolean dissolve = false;
		synchronized (_members)
		{
			if(!_members.remove(player))
				return false;
			dissolve = _members.size() == 1;
		}
		player.stopSubstituteTask();
		player.getListeners().onPartyLeave();
		player.setParty(null);

		listenerList.onLeaveMember(player, kick);

		recalculatePartyData();
		List<IBroadcastPacket> pplayer = new ArrayList<>(4 + _members.size() * 2);
		if(isInCommandChannel())
			pplayer.add(ExCloseMPCCPacket.STATIC);
		if(kick)
			pplayer.add(SystemMsg.YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY);
		else
			pplayer.add(SystemMsg.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY);
		pplayer.add(PartySmallWindowDeleteAllPacket.STATIC);
		List<IBroadcastPacket> outsInfo = new ArrayList<>(3);
		for(Servitor servitor : player.getServitors())
			outsInfo.add(new ExPartyPetWindowDelete(servitor));
		outsInfo.add(new PartySmallWindowDeletePacket(player));
		if(kick)
			outsInfo.add(new SystemMessage(201).addName(player));
		else
			outsInfo.add(new SystemMessage(108).addName(player));
		RelationChangedPacket rcp = new RelationChangedPacket();
		for(Player member : _members)
		{
			List<IBroadcastPacket> pmember = new ArrayList<>(2 + outsInfo.size());
			pmember.addAll(outsInfo);
			RelationChangedPacket memberrcp = new RelationChangedPacket(player, member);
			for(Servitor servitor2 : player.getServitors())
				memberrcp.add(servitor2, member);
			pmember.add(memberrcp);
			member.sendPacket(pmember);
			rcp.add(member, player);
			for(Servitor servitor2 : member.getServitors())
				rcp.add(servitor2, player);
		}
		pplayer.add(rcp);
		player.sendPacket(pplayer);
		clearTacticalTargets(player);
		Reflection reflection = getReflection();
		if(reflection != null && player.getReflection() == reflection && reflection.getReturnLoc() != null)
			player.teleToLocation(reflection.getReturnLoc(), ReflectionManager.MAIN);
		Player leader = getPartyLeader();
		MatchingRoom room = leader != null ? leader.getMatchingRoom() : null;
		if(dissolve)
		{
			if(isInCommandChannel())
				_commandChannel.removeParty(this);
			else if(reflection != null && reflection.getInstancedZone() != null && reflection.getInstancedZone().isCollapseOnPartyDismiss() && reflection.getParty() == this)
			{
				reflection.startCollapseTimer(reflection.getInstancedZone().getTimerOnCollapse() * 1000);
				if(leader != null && leader.getReflection() == reflection)
					leader.broadcastPacket(new SystemMessage(2106).addNumber(1));
			}
			if(room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
				if(isLeader)
					room.disband();
				else
					room.removeMember(player, kick);
			dissolveParty();
		}
		else
		{
			if(isInCommandChannel() && _commandChannel.getChannelLeader() == player)
				_commandChannel.setChannelLeader(leader);
			if(room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
				room.removeMember(player, kick);
			if(isLeader)
				updateLeaderInfo();
		}
		if(_checkTask != null)
		{
			_checkTask.cancel(true);
			_checkTask = null;
		}
		return true;
	}

	public boolean changePartyLeader(Player player)
	{
		Player leader = getPartyLeader();
		PartyService.getInstance().removeParty(this);

		synchronized (_members)
		{
			int index = _members.indexOf(player);
			if(index == -1)
				return false;
			_members.set(0, player);
			_members.set(index, leader);
		}

		PartyService.getInstance().addParty(this);

		leader.sendPacket(ExReplyHandOverPartyMaster.FALSE);
		player.sendPacket(ExReplyHandOverPartyMaster.TRUE);
		updateLeaderInfo();
		if(isInCommandChannel() && _commandChannel.getChannelLeader() == leader)
			_commandChannel.setChannelLeader(player);
		return true;
	}

	private void updateLeaderInfo()
	{
		Player leader = getPartyLeader();
		if(leader == null)
			return;
		SystemMessagePacket msg = new SystemMessagePacket(SystemMsg.C1_HAS_BECOME_THE_PARTY_LEADER).addName(leader);
		for(Player member : _members)
			member.sendPacket(PartySmallWindowDeleteAllPacket.STATIC, new PartySmallWindowAllPacket(this, member), msg);
		for(Player member : _members)
		{
			broadcastToPartyMembers(member, new PartySpelledPacket(member, true));
			for(Servitor servitor : member.getServitors())
				broadCast(new ExPartyPetWindowAdd(servitor));
		}
		MatchingRoom room = leader.getMatchingRoom();
		if(room != null && room.getType() == MatchingRoom.PARTY_MATCHING)
			room.setLeader(leader);
	}

	public Player getPlayerByName(String name)
	{
		for(Player member : _members)
			if(name.equalsIgnoreCase(member.getName()))
				return member;
		return null;
	}

	public void distributeItem(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		switch(item.getItemId())
		{
			case ItemTemplate.ITEM_ID_ADENA:
			{
				distributeAdena(player, item, fromNpc);
				break;
			}
			default:
			{
				distributeItem0(player, item, fromNpc);
				break;
			}
		}
	}

	private void distributeItem0(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		Player target = null;
		List<Player> ret = null;
		switch(_itemDistribution)
		{
			case 1:
			case 2:
			{
				ret = new ArrayList<>(_members.size());
				for(Player member : _members)
					if(member.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !member.isDead() && member.getInventory().validateCapacity(item) && member.getInventory().validateWeight(item))
						ret.add(member);
				target = ret.isEmpty() ? null : ret.get(Rnd.get(ret.size()));
				break;
			}
			case 3:
			case 4:
			{
				synchronized (_members)
				{
					ret = new CopyOnWriteArrayList<>(_members);
					while(target == null && !ret.isEmpty())
					{
						int looter = _itemOrder;
						++_itemOrder;
						if(_itemOrder > ret.size() - 1)
							_itemOrder = 0;
						Player looterPlayer = looter < ret.size() ? ret.get(looter) : null;
						if(looterPlayer != null)
							if(!looterPlayer.isDead() && looterPlayer.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && ItemFunctions.canAddItem(looterPlayer, item))
								target = looterPlayer;
							else
								ret.remove(looterPlayer);
					}
				}
				if(target == null)
					return;
				break;
			}
			default:
			{
				target = player;
				break;
			}
		}
		if(target == null)
			target = player;
		if(target.pickupItem(item, ItemLogProcess.PartyPickup))
		{
			if(fromNpc == null)
				player.broadcastPacket(new GetItemPacket(item, player.getObjectId()));
			player.broadcastPickUpMsg(item);
			item.pickupMe();
			broadcastToPartyMembers(target, SystemMessagePacket.obtainItemsBy(item, target));
		}
		else
			item.dropToTheGround(player, fromNpc);
	}

	private void distributeAdena(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		if(player == null)
			return;

		List<Player> membersInRange = _members.stream()
				.filter(member -> canReceiveDistributedAdena(player, member))
				.collect(Collectors.toList());
		long totalAdena = item.getCount();

		if(membersInRange.isEmpty()) {
			player.getInventory().addAdena(totalAdena);
			player.sendPacket(SystemMessagePacket.obtainItems(ItemTemplate.ITEM_ID_ADENA, totalAdena, 0));
		}
		else {
			long amount = totalAdena / membersInRange.size();
			if(amount == 0) {
				Collections.shuffle(membersInRange);
				for(int i = 0; i < totalAdena; i++) {
					Player pl = membersInRange.get(i);
					pl.getInventory().addAdena(1);
					pl.sendPacket(SystemMessagePacket.obtainItems(ItemTemplate.ITEM_ID_ADENA, 1, 0));
				}
			}
			else {
				for(Player member : membersInRange) {
					member.getInventory().addAdena(amount);
					member.sendPacket(SystemMessagePacket.obtainItems(ItemTemplate.ITEM_ID_ADENA, amount, 0));
				}
			}
		}
		if(fromNpc == null) {
			player.broadcastPacket(new GetItemPacket(item, player.getObjectId()));
		}

		item.pickupMe();
	}

	private boolean canReceiveDistributedAdena(Player player, Player member) {
		return member.equals(player) || player.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE);
	}

	public void distributeXpAndSp(Player killer, Player victim, double xpReward, double spReward)
	{
		recalculatePartyData();

        int partyLevel = victim.getLevel();

        for(Player member : getPartyMembers())
		{
			if(!victim.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;

			partyLevel = Math.max(partyLevel, member.getLevel());
		}

        int partyLvlSum = 0;
        List<Player> mtr = new ArrayList<>();
        for(Player member : getPartyMembers())
		{
			if(!victim.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;

			if(member.getLevel() <= partyLevel - 15)
				continue;

			partyLvlSum += member.getLevel();
			mtr.add(member);
		}

		if(mtr.isEmpty())
			return;

		TIntIntMap clansInParty = new TIntIntHashMap();

		for(Player member : mtr)
		{
			Clan clan = member.getClan();
			if(clan == null)
				continue;

			clansInParty.put(clan.getClanId(), clansInParty.get(clan.getClanId()) + 1);
		}

		double bonus = Config.ALT_PARTY_BONUS[Math.min(Config.ALT_PARTY_BONUS.length, mtr.size()) - 1];
		double XP = xpReward * bonus;
		double SP = spReward * bonus;
		for(Player member : mtr)
		{
			double clanBonus = Config.ALT_PARTY_CLAN_BONUS[Math.min(Config.ALT_PARTY_CLAN_BONUS.length - 1, clansInParty.get(member.getClanId()))];
			long memberXp = (long) (XP * clanBonus * member.getLevel() / partyLvlSum);
			long memberSp = (long) (SP * clanBonus * member.getLevel() / partyLvlSum);
			memberXp = (long) Math.min(memberXp, xpReward);
			memberSp = (long) Math.min(memberSp, spReward);
			member.addExpAndSp(memberXp, memberSp);
		}

		recalculatePartyData();
	}

	public void distributeXpAndSp(double xpReward, double spReward, List<Player> rewardedMembers, Creature lastAttacker, MonsterInstance monster)
	{
		recalculatePartyData();

        int partyLevel = lastAttacker.getLevel();

        for(Player member : rewardedMembers)
		{
			if(!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;

			partyLevel = Math.max(partyLevel, member.getLevel());
		}

        int partyLvlSum = 0;
        List<Player> mtr = new ArrayList<>();
        for(Player member : rewardedMembers)
		{
			if(!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;

			if(member.getLevel() <= partyLevel - 15)
				continue;

			partyLvlSum += member.getLevel();
			mtr.add(member);
		}

		if(mtr.isEmpty())
			return;

		TIntIntMap clansInParty = new TIntIntHashMap();

		for(Player member : mtr)
		{
			Clan clan = member.getClan();
			if(clan == null)
				continue;

			clansInParty.put(clan.getClanId(), clansInParty.get(clan.getClanId()) + 1);
		}

		double bonus = Config.ALT_PARTY_BONUS[Math.min(Config.ALT_PARTY_BONUS.length, mtr.size()) - 1];
		double XP = xpReward * bonus;
		double SP = spReward * bonus;
		for(Player member : mtr)
		{
			double lvlPenalty = Experience.penaltyModifier(monster.calculateLevelDiffForDrop(member.getLevel()));
			int lvlDiff = partyLevel - member.getLevel();
			lvlDiff = Math.max(0, Math.min(lvlDiff, Config.ALT_PARTY_LVL_DIFF_PENALTY.length - 1));
			lvlPenalty *= Config.ALT_PARTY_LVL_DIFF_PENALTY[lvlDiff] / 100.0;
			double clanBonus = Config.ALT_PARTY_CLAN_BONUS[Math.min(Config.ALT_PARTY_CLAN_BONUS.length - 1, clansInParty.get(member.getClanId()))];
			double memberXp = XP * clanBonus * lvlPenalty * member.getLevel() / partyLvlSum;
			double memberSp = SP * clanBonus * lvlPenalty * member.getLevel() / partyLvlSum;
			memberXp = Math.min(memberXp, xpReward);
			memberSp = Math.min(memberSp, spReward);
			member.addExpAndCheckBonus(monster, (long) memberXp, (long) memberSp);
		}

		recalculatePartyData();
	}

	public void recalculatePartyData()
	{
		_partyLvl = 0;
		double rateExp = 0.0;
		double rateSp = 0.0;
		double rateDrop = 0.0;
		double rateAdena = 0.0;
		double rateSpoil = 0.0;
		double dropChanceMod = 0.0;
		double spoilChanceMod = 0.0;
		double minRateExp = Double.MAX_VALUE;
		double minRateSp = Double.MAX_VALUE;
		double minRateDrop = Double.MAX_VALUE;
		double minRateAdena = Double.MAX_VALUE;
		double minRateSpoil = Double.MAX_VALUE;
		double minDropChanceMod = Double.MAX_VALUE;
		double minSpoilChanceMod = Double.MAX_VALUE;
		int count = 0;
		for(Player member : _members)
		{
			int level = member.getLevel();
			_partyLvl = Math.max(_partyLvl, level);
			++count;
			rateExp += member.getRateExp(true);
			rateSp += member.getRateSp(true);
			rateDrop += member.getRateItems(true);
			rateAdena += member.getRateAdena(true);
			rateSpoil += member.getRateSpoil(true);
			dropChanceMod += member.getDropChanceMod(true);
			spoilChanceMod += member.getSpoilChanceMod(true);
			minRateExp = Math.min(minRateExp, member.getRateExp(true));
			minRateSp = Math.min(minRateSp, member.getRateSp(true));
			minRateDrop = Math.min(minRateDrop, member.getRateItems(true));
			minRateAdena = Math.min(minRateAdena, member.getRateAdena(true));
			minRateSpoil = Math.min(minRateSpoil, member.getRateSpoil(true));
			minDropChanceMod = Math.min(minDropChanceMod, member.getDropChanceMod(true));
			minSpoilChanceMod = Math.min(minSpoilChanceMod, member.getSpoilChanceMod(true));
		}

		_rateExp = Config.RATE_PARTY_MIN ? minRateExp : rateExp / count;
		_rateSp = Config.RATE_PARTY_MIN ? minRateSp : rateSp / count;
		_rateDrop = Config.RATE_PARTY_MIN ? minRateDrop : rateDrop / count;
		_rateAdena = Config.RATE_PARTY_MIN ? minRateAdena : rateAdena / count;
		_rateSpoil = Config.RATE_PARTY_MIN ? minRateSpoil : rateSpoil / count;
		_dropChanceMod = Config.RATE_PARTY_MIN ? minDropChanceMod : dropChanceMod / count;
		_spoilChanceMod = Config.RATE_PARTY_MIN ? minSpoilChanceMod : spoilChanceMod / count;

		updateFractionBonus();
	}

    private void updateFractionBonus()
    {
		if(getMemberCount() < 2)
		{
			return;
		}

		var modifier = FractionService.getInstance().getFractionAdenaModifier(getGroupLeader().getFraction());

		_rateAdena += modifier.getValue();
	}

	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isDistributeSpoilLoot()
	{
		boolean rv = false;
		if(_itemDistribution == 2 || _itemDistribution == 4)
			rv = true;
		return rv;
	}

	public boolean isInReflection()
	{
		return _reflection != null;
	}

	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}

	public Reflection getReflection()
	{
		if(_reflection != null)
			return _reflection;
		return null;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(CommandChannel channel)
	{
		_commandChannel = channel;
	}

	public void Teleport(int x, int y, int z)
	{
		TeleportParty(getPartyMembers(), new Location(x, y, z));
	}

	public void Teleport(Location dest)
	{
		TeleportParty(getPartyMembers(), dest);
	}

	public void Teleport(Territory territory)
	{
		RandomTeleportParty(getPartyMembers(), territory);
	}

	public void Teleport(Territory territory, Location dest)
	{
		TeleportParty(getPartyMembers(), territory, dest);
	}

	public static void TeleportParty(List<Player> members, Location dest)
	{
		for(Player _member : members)
		{
			if(_member == null)
				continue;
			_member.teleToLocation(dest);
		}
	}

	public static void TeleportParty(List<Player> members, Territory territory, Location dest)
	{
		if(!territory.isInside(dest.x, dest.y))
		{
			SimpleMessage message = new SimpleMessage("TeleportParty: dest is out of territory");
			LogService.getInstance().log(LoggerType.DEBUG, message);
			Thread.dumpStack();
			return;
		}
		int base_x = members.get(0).getX();
		int base_y = members.get(0).getY();
		for(Player _member : members)
		{
			if(_member == null)
				continue;
			int diff_x = _member.getX() - base_x;
			int diff_y = _member.getY() - base_y;
			Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
			while(!territory.isInside(loc.x, loc.y))
			{
				diff_x = loc.x - dest.x;
				diff_y = loc.y - dest.y;
				if(diff_x != 0)
				{
					Location location = loc;
					location.x -= diff_x / Math.abs(diff_x);
				}
				if(diff_y != 0)
				{
					Location location2 = loc;
					location2.y -= diff_y / Math.abs(diff_y);
				}
			}
			_member.teleToLocation(loc);
		}
	}

	public static void RandomTeleportParty(List<Player> members, Territory territory)
	{
		for(Player member : members)
			member.teleToLocation(Territory.getRandomLoc(territory, member.getGeoIndex()));
	}

	private void startUpdatePositionTask()
	{
		if(positionTask == null)
			positionTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(new UpdatePositionTask(), 1000L, 1000L);
	}

	private void stopUpdatePositionTask()
	{
		if(positionTask != null)
			positionTask.cancel(false);
	}

	public void requestLootChange(byte type)
	{
		if(_requestChangeLoot != -1)
		{
			if(System.currentTimeMillis() <= _requestChangeLootTimer)
				return;
			finishLootRequest(false);
		}
		_requestChangeLoot = type;
		int additionalTime = 45000;
		_requestChangeLootTimer = System.currentTimeMillis() + additionalTime;
		_changeLootAnswers = new CopyOnWriteArraySet<>();
		_checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ChangeLootCheck(), additionalTime + 1000, 5000L);
		broadcastToPartyMembers(getPartyLeader(), new ExAskModifyPartyLooting(getPartyLeader().getName(), type));
		SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.REQUESTING_APPROVAL_FOR_CHANGING_PARTY_LOOT_TO_S1);
		sm.addSysString(LOOT_SYSSTRINGS[type]);
		getPartyLeader().sendPacket(sm);
		
		// XXX затычка фантомам. Прием запроса на смену лута
		Party party = getPartyLeader().getParty();
		if (party != null)
		{
			for(Player member : party.getPartyMembers())
			{
				if (member.isPhantom())
				{
					party.answerLootChangeRequest(member, true);
				}
			}
		}
	}

	public synchronized void answerLootChangeRequest(Player member, boolean answer)
	{
		if(_requestChangeLoot == -1)
			return;
		if(_changeLootAnswers.contains(member.getObjectId()))
			return;
		if(!answer)
		{
			finishLootRequest(false);
			return;
		}
		_changeLootAnswers.add(member.getObjectId());
		if(_changeLootAnswers.size() >= getMemberCount() - 1)
			finishLootRequest(true);
	}

	private synchronized void finishLootRequest(boolean success)
	{
		if(_requestChangeLoot == -1)
			return;
		if(_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if(success)
		{
			broadCast(new ExSetPartyLooting(1, _requestChangeLoot));
			_itemDistribution = _requestChangeLoot;
			SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.PARTY_LOOT_WAS_CHANGED_TO_S1_);
			sm.addSysString(LOOT_SYSSTRINGS[_requestChangeLoot]);
			broadCast(sm);
		}
		else
		{
			broadCast(new ExSetPartyLooting(0, 0));
			broadCast(new SystemMessagePacket(SystemMsg.PARTY_LOOT_CHANGE_WAS_CANCELLED));
		}
		_changeLootAnswers = null;
		_requestChangeLoot = -1;
		_requestChangeLootTimer = 0L;
	}

	@Override
	public Player getGroupLeader()
	{
		return getPartyLeader();
	}

	@Override
	public Iterator<Player> iterator()
	{
		return _members.iterator();
	}

	public void changeTacticalSign(Player player, int sign, Creature target)
	{
		if(target == null)
			return;
		if(_tacticalTargets.containsKey(sign))
		{
			Creature oldTarget = _tacticalTargets.get(sign);
			if(oldTarget != null)
				broadCast(new ExTacticalSign(oldTarget.getObjectId(), 0));
		}
		_tacticalTargets.put(sign, target);
		broadCast(new ExTacticalSign(target.getObjectId(), sign));
		SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.C1_USED_S3_ON_C2);
		sm.addName(player);
		sm.addName(target);
		sm.addSysString(TACTICAL_SYSSTRINGS[sign]);
		broadCast(sm);
	}

	public Creature findTacticalTarget(Player player, int sign)
	{
		if(player == null)
			return null;
		if(!_tacticalTargets.containsKey(sign))
			return null;
		Creature target = _tacticalTargets.get(sign);
		if(player.getDistance3D(target) > 1000.0)
			return null;
		return target;
	}

	private void clearTacticalTargets(Player player)
	{
		for(Creature target : _tacticalTargets.valueCollection())
			player.sendPacket(new ExTacticalSign(target.getObjectId(), 0));
	}

	private void sendTacticalSign(Player member)
	{
		TIntObjectIterator<Creature> iterator = _tacticalTargets.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			Creature target = iterator.value();
			if(target == null)
				continue;
			member.sendPacket(new ExTacticalSign(target.getObjectId(), iterator.key()));
		}
	}

	public void removeTacticalSign(Creature target)
	{
		TIntObjectIterator<Creature> iterator = _tacticalTargets.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			if(iterator.value() == target)
			{
				broadCast(new ExTacticalSign(target.getObjectId(), 0));
				_tacticalTargets.remove(iterator.key());
				break;
			}
		}
	}

	public void substituteMember(Player member, Player member2)
	{
		Location defLoc = member.getLoc();
		Location defLoc2 = member2.getLoc();
		member.teleToLocation(defLoc2, member2.getReflection());
		member2.teleToLocation(defLoc, member.getReflection());
		removePartyMember(member, false);
		addPartyMember(member2);
	}

	static
	{
		MAX_SIZE = Config.MAXIMUM_MEMBERS_IN_PARTY;
		LOOT_SYSSTRINGS = new int[] { 487, 488, 798, 799, 800 };
		TACTICAL_SYSSTRINGS = new int[] { 0, 2664, 2665, 2666, 2667 };
	}

	public void setLastBuff(long currentTimeMillis) {
		this.lastBuff = currentTimeMillis;
	}

	public long getLastBuff() {
		return lastBuff;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(!(o instanceof Party))
			return false;
		Party players = (Party) o;
		return Objects.equals(getGroupLeader(), players.getGroupLeader());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getGroupLeader());
	}

	public void removeListener(PartyListener partyListener) {
		listenerList.remove(partyListener);
	}

	public void addListener(PartyListener listener) {
		listenerList.add(listener);
	}

	private class UpdatePositionTask implements Runnable
	{
		@Override
		public void run()
		{
			List<Player> update = new ArrayList<>();
			for(Player member : _members)
			{
				Location loc = member.getLastPartyPosition();
				if(loc == null || member.getDistance(loc) > 256.0)
				{
					member.setLastPartyPosition(member.getLoc());
					update.add(member);
				}
			}
			if(!update.isEmpty())
				for(Player member : _members)
				{
					PartyMemberPositionPacket pmp = new PartyMemberPositionPacket();
					for(Player m : update)
						if(m != member)
							pmp.add(m);
					if(pmp.size() > 0)
						member.sendPacket(pmp);
				}
		}
	}

	private class ChangeLootCheck implements Runnable
	{
		@Override
		public void run()
		{
			if(System.currentTimeMillis() > _requestChangeLootTimer)
				finishLootRequest(false);
		}
	}

}
