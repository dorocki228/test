package l2s.gameserver.model.entity.events.impl;

import com.google.common.collect.Iterators;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExDuelStart;
import l2s.gameserver.network.l2.s2c.ExDuelUpdateUserInfo;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

import java.util.Iterator;
import java.util.List;

public abstract class DuelEvent extends AbstractDuelEvent implements Iterable<DuelSnapshotObject>
{
	protected OnPlayerExitListener _playerExitListener;
	protected TeamType _winner;
	protected boolean _aborted;
	protected boolean _isInProgress;

	public DuelEvent(MultiValueSet<String> set)
	{
		super(set);
		_playerExitListener = new OnPlayerExitListenerImpl();
		_winner = TeamType.NONE;
	}

	protected DuelEvent(int id, String name)
	{
		super(id, name);
		_playerExitListener = new OnPlayerExitListenerImpl();
		_winner = TeamType.NONE;
	}

	@Override
	public void initEvent()
	{}

	public abstract void packetSurrender(Player var1);

	@Override
	public abstract void onDie(Player var1, Creature killer);

	public abstract int getDuelType();

	@Override
	public void startEvent()
	{
		_isInProgress = true;
		for(DuelSnapshotObject snapshot : this)
		{
			if(canDuel0(snapshot.getPlayer(), snapshot.getPlayer(), true) == null)
				continue;
			abortDuel(snapshot.getPlayer());
			return;
		}
		updatePlayers(true, false);
		sendPackets(new ExDuelStart(this), PlaySoundPacket.B04_S01, SystemMsg.LET_THE_DUEL_BEGIN);
		for(DuelSnapshotObject $snapshot : this)
			sendPacket(new ExDuelUpdateUserInfo($snapshot.getPlayer()), $snapshot.getTeam().revert());
	}

	public void sendPacket(IBroadcastPacket packet, TeamType... ar)
	{
		for(TeamType a : ar)
		{
			List<DuelSnapshotObject> objs = getObjects(a);
			for(DuelSnapshotObject obj : objs)
				obj.getPlayer().sendPacket(packet);

		}
	}

	@Override
	public void sendPacket(IBroadcastPacket packet)
	{
		sendPackets(packet);
	}

	@Override
	public void sendPackets(IBroadcastPacket... packet)
	{
		for(DuelSnapshotObject d : this)
			d.getPlayer().sendPacket(packet);
	}

	public void abortDuel(Player player)
	{
		_aborted = true;
		_winner = TeamType.NONE;
		stopEvent(true);
	}

	protected IBroadcastPacket canDuel0(Player requestor, Player target, boolean secondCheck)
	{
		IBroadcastPacket packet = null;
		if(target.isInCombat())
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE).addName(target);
		else if(target.isDead() || target.isAlikeDead() || target.getCurrentHpPercents() < 50.0 || target.getCurrentMpPercents() < 50.0 || target.getCurrentCpPercents() < 50.0)
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1S_HP_OR_MP_IS_BELOW_50).addName(target);
		else if(!secondCheck && target.containsEvent(DuelEvent.class))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL).addName(target);
		else if(secondCheck && !target.containsEvent(this))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL).addName(target);
		else if(target.isInZone(Zone.ZoneType.SIEGE))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR).addName(target);
		else if(target.isInOlympiadMode() || Olympiad.isRegisteredInComp(target) || requestor.isInOlympiadMode() || Olympiad.isRegisteredInComp(requestor))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD).addName(target);
		else if(target.isPK() || target.getPvpFlag() > 0)
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE).addName(target);
		else if(target.isInStoreMode())
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE).addName(target);
		else if(target.isMounted() || target.isInBoat())
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER).addName(target);
		else if(target.isFishing())
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING).addName(target);
		else if(target.isInZoneBattle() || target.isInPeaceZone() || target.isInWater() || target.isInZone(Zone.ZoneType.no_restart))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA).addName(target);
		else if(!requestor.isInRangeZ(target, secondCheck ? 1200 : 250))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY).addName(target);
		else if(target.isTransformed())
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED).addName(target);
		else if(!secondCheck && target.containsEvent(SingleMatchEvent.class))
			packet = new SystemMessagePacket(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE).addName(target);
		return packet;
	}

	protected void updatePlayers(boolean start, boolean teleport)
	{
		for(DuelSnapshotObject snapshot : this)
		{
			if(teleport)
			{
				snapshot.teleportBack();
				continue;
			}

			Player player = snapshot.getPlayer();
			if(start)
			{
				snapshot.store();
				player.setUndying(SpecialEffectState.TRUE);
				player.setTeam(snapshot.getTeam());
				continue;
			}

			if(player.isUndying())
				player.setUndying(SpecialEffectState.FALSE);

			player.removeEvent(this);
			if(!_aborted)
				snapshot.restore();

			player.setTeam(TeamType.NONE);
		}
	}

	@Override
	public void onStatusUpdate(Player player)
	{
		sendPacket(new ExDuelUpdateUserInfo(player), player.getTeam().revert());
	}

	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
			return SystemMsg.INVALID_TARGET;

		if(!target.containsEvent(this))
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	@Override
	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force, boolean nextAttackCheck)
	{
		if(target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
			return false;

		return target.containsEvent(this);
	}

	@Override
	public void onAddEvent(GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().addListener(_playerExitListener);
	}

	@Override
	public void onRemoveEvent(GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().removeListener(_playerExitListener);
	}

	@Override
	public Iterator<DuelSnapshotObject> iterator()
	{
		List<DuelSnapshotObject> blue = getObjects(TeamType.BLUE);
		List<DuelSnapshotObject> red = getObjects(TeamType.RED);

		return Iterators.concat(blue.iterator(), red.iterator());
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		registerActions();
	}

	@Override
	public EventType getType()
	{
		return EventType.PVP_EVENT;
	}

	@Override
	public void announce(int i, SystemMsg msgId)
	{
		sendPacket(new SystemMessagePacket(SystemMsg.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(i));
	}

	public void playerLost(Player player)
	{
		player.setTeam(TeamType.NONE);

		for(DuelSnapshotObject snapshot : this)
		{
			if(snapshot.getPlayer() != player)
				continue;

			snapshot.setDead();
			break;
		}
		checkForWinner();
	}

	protected synchronized void checkForWinner()
	{
		TeamType winnerTeam = null;
		for(TeamType team : TeamType.VALUES)
		{
			List<DuelSnapshotObject> objects = getObjects(team);
			boolean allDead = true;

			for(DuelSnapshotObject d : objects)
			{
				if(d.isDead())
					continue;

				allDead = false;
			}
			if(!allDead)
				continue;

			winnerTeam = team.revert();
			break;
		}
		if(winnerTeam != null)
		{
			_winner = winnerTeam;
			stopEvent(true);
		}
	}

	@Override
	public boolean isInProgress()
	{
		return _isInProgress;
	}

	private class OnPlayerExitListenerImpl extends SingleMatchEvent.OnDeathFromUndyingListenerImpl implements OnPlayerExitListener
	{
		private OnPlayerExitListenerImpl()
		{
		}

		@Override
		public void onPlayerExit(Player player)
		{
            playerLost(player);
		}
	}

}
