package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.dao.SiegePlayerDAO;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.EventRestartLoc;
import l2s.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.CTBTeamObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.clanhall.SiegeableClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.Location;

import java.util.List;

public class ClanHallTeamBattleEvent extends SiegeEvent<SiegeableClanHall, CTBSiegeClanObject>
{
	public static final String TRYOUT_PART = "tryout_part";
	public static final String CHALLENGER_RESTART_POINTS = "challenger_restart_points";
	public static final String FIRST_DOORS = "first_doors";
	public static final String SECOND_DOORS = "second_doors";
	public static final String NEXT_STEP = "next_step";

	public ClanHallTeamBattleEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void startEvent()
	{
		List<CTBSiegeClanObject> attackers = getObjects("attackers");
		if(attackers.isEmpty())
		{
			if(getResidence().getOwner() == null)
				broadcastInZone2(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			else
				broadcastInZone2(new SystemMessagePacket(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));
			reCalcNextTime(false);
			return;
		}
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
			addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
		SiegeClanDAO.getInstance().delete(getResidence());
		SiegePlayerDAO.getInstance().delete(getResidence());
		List<CTBTeamObject> teams = getObjects("tryout_part");
		for(int i = 0; i < 5; ++i)
		{
			CTBTeamObject team = teams.get(i);
			CTBSiegeClanObject siegeClan = attackers.size() > i ? attackers.get(i) : null;
			team.setSiegeClan(siegeClan);
		}
		broadcastTo(new SystemMessagePacket(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), "attackers", "defenders");
		broadcastTo(SystemMsg.THE_TRYOUTS_ARE_ABOUT_TO_BEGIN, "attackers");
		super.startEvent();
	}

	public void nextStep()
	{
		broadcastTo(SystemMsg.THE_TRYOUTS_HAVE_BEGUN, "attackers", "defenders");
		updateParticles(true, "attackers", "defenders");
	}

	public void processStep(CTBTeamObject team)
	{
		if(team.getSiegeClan() != null)
		{
			CTBSiegeClanObject object = team.getSiegeClan();
			object.setEvent(false, this);
			teleportPlayers("spectators");
		}
		team.despawnObject(this);
		List<CTBTeamObject> teams = getObjects("tryout_part");
		boolean hasWinner = false;
		CTBTeamObject winnerTeam = null;
		for(CTBTeamObject t : teams)
			if(t.isParticle())
			{
				hasWinner = winnerTeam == null;
				winnerTeam = t;
			}
		if(!hasWinner)
			return;
		SiegeClanObject clan = winnerTeam.getSiegeClan();
		if(clan != null)
			getResidence().changeOwner(clan.getClan());
		stopEvent(true);
	}

	@Override
	public void announce(int val, SystemMsg msgId)
	{
		int minute = val / 60;
		if(minute > 0)
			broadcastTo(new SystemMessagePacket(SystemMsg.THE_CONTEST_WILL_BEGIN_IN_S1_MINUTES).addNumber(minute), "attackers", "defenders");
		else
			broadcastTo(new SystemMessagePacket(SystemMsg.THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS).addNumber(val), "attackers", "defenders");
	}

	@Override
	public void stopEvent(boolean force)
	{
		Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			if(_oldOwner != newOwner)
			{
				newOwner.broadcastToOnlineMembers(PlaySoundPacket.SIEGE_VICTORY);
				newOwner.incReputation(1700, false, toString());
			}
			broadcastTo(new SystemMessagePacket(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName()).addResidenceName(getResidence()), "attackers", "defenders");
			broadcastTo(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), "attackers", "defenders");
		}
		else
			broadcastTo(new SystemMessagePacket(SystemMsg.THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), "attackers");
		updateParticles(false, "attackers", "defenders");
		removeObjects("defenders");
		removeObjects("attackers");
		super.stopEvent(force);
		_oldOwner = null;
	}

	@Override
	public void loadSiegeClans()
	{
		List<SiegeClanObject> siegeClanObjectList = SiegeClanDAO.getInstance().load(getResidence(), "attackers");
		addObjects("attackers", siegeClanObjectList);
		List<CTBSiegeClanObject> objects = getObjects("attackers");
		for(CTBSiegeClanObject clan : objects)
			clan.select(getResidence());
	}

	@Override
	public CTBSiegeClanObject newSiegeClan(String type, int clanId, long i, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new CTBSiegeClanObject(type, clan, i, date);
	}

	@Override
	public void findEvent(Player player)
	{
		if(!isInProgress() || player.getClan() == null)
			return;
		CTBSiegeClanObject object = getSiegeClan("attackers", player.getClan());
		if(object != null && object.getPlayers().contains(player.getObjectId()))
			player.addEvent(this);
	}

	@Override
	public EventRestartLoc getRestartLoc(Player player, RestartType type)
	{
		if(!checkIfInZone(player))
			return null;
		SiegeClanObject attackerClan = getSiegeClan("attackers", player.getClan());
		Location loc = null;
		switch(type)
		{
			case TO_VILLAGE:
			{
				if(attackerClan != null && checkIfInZone(player))
				{
					List<SiegeClanObject> objectList = getObjects("attackers");
					List<Location> teleportList = getObjects("challenger_restart_points");
					int index = objectList.indexOf(attackerClan);
					loc = teleportList.get(index);
					break;
				}
				break;
			}
		}
		return new EventRestartLoc(loc);
	}

	@Override
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		return true;
	}

	@Override
	public void action(String name, boolean start)
	{
		if("next_step".equalsIgnoreCase(name))
			nextStep();
		else
			super.action(name, start);
	}

	@Override
	public int getUserRelation(Player thisPlayer, int result)
	{
		return result;
	}

	@Override
	public int getRelation(Player thisPlayer, Player targetPlayer, int result)
	{
		return result;
	}
}
