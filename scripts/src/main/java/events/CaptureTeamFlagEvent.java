package events;

import com.google.common.collect.ImmutableList;
import events.impl.ctf.CtfBaseObject;
import events.impl.ctf.CtfFlagObject;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.skillclasses.Resurrect;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * @author VISTALL
 * @date 15:08/03.04.2012
 */
public class CaptureTeamFlagEvent extends CustomInstantTeamEvent
{
	private class RessurectTask implements Runnable
	{
		private Player _player;
		private int _seconds = 11;

		public RessurectTask(Player player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			_seconds -= 1;
			if(_seconds == 0)
			{
				_deadList.remove(_player.getObjectId());

				if (_player.getTeam() == TeamType.NONE) // Он уже не на эвенте.
					return;

				_player.teleToLocation(getTeleportLoc(_player.getTeam()));
				_player.doRevive();

                onRevive(_player);
			}
			else
			{
				_player.sendPacket(new SystemMessagePacket(SystemMsg.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addNumber(_seconds));
				ScheduledFuture<?> f = ThreadPoolManager.getInstance().schedule(this, 1000L);

				_deadList.put(_player.getObjectId(), f);
			}
		}
	}

	private class OnDeathListenerImpl implements OnDeathListener
	{
		@Override
		public void onDeath(Creature victim, Creature killer)
		{
			_deadList.put(victim.getObjectId(), ThreadPoolManager.getInstance().schedule(new RessurectTask(victim.getPlayer()), 1000L));
		}
	}

	public static final String FLAGS = "flags";
	public static final String BASES = "bases";
	public static final String UPDATE_ARROW = "update_arrow";
	private OnDeathListener _deathListener = new OnDeathListenerImpl();
	private IntObjectMap<ScheduledFuture<?>> _deadList = new CHashIntObjectMap<ScheduledFuture<?>>();

	public CaptureTeamFlagEvent(MultiValueSet<String> set)
	{
		super(set);

		Resurrect.GLOBAL.add(this);

		addObject(BASES, new CtfBaseObject(35426, new Location(225512, 77544, 1104), TeamType.BLUE));
		addObject(BASES, new CtfBaseObject(35423, new Location(216008, 73048, 928), TeamType.RED));
		addObject(FLAGS, new CtfFlagObject(new Location(225784, 77160, 1080), TeamType.BLUE));
		addObject(FLAGS, new CtfFlagObject(new Location(216248, 72680, 960), TeamType.RED));
	}

	private void updateArrowInPlayers()
	{
		List<CtfFlagObject> flagObjects = getObjects(FLAGS);

		for(int i = 0; i < TeamType.VALUES.length; i++)
		{
			TeamType teamType = TeamType.VALUES[i];

			CtfFlagObject selfFlag = flagObjects.get(teamType.ordinalWithoutNone());
			CtfFlagObject enemyFlag = flagObjects.get(teamType.revert().ordinalWithoutNone());

			List<EventPlayerObject> objects = getObjects(teamType);

			for(EventPlayerObject object : objects)
			{
				Player player = object.getPlayer();
				if(player == null)
					continue;

				Location location = null;
				// у тя чужой флаг в руках, посылаем к базе
				if(enemyFlag.getOwner() == player)
				{
					List<CtfBaseObject> bases = getObjects(BASES);

					location = bases.get(i).getLoc();
				}
				// свой флаг потерян, посылаем к овнеру
				else if(selfFlag.getOwner() != null)
					location = selfFlag.getOwner().getLoc();
				// иначе посылаем к чужом флагу
				else
					location = enemyFlag.getLocation();

				player.addRadar(location.getX(), location.getY(), location.getZ());
			}
		}
	}

	public void setWinner(TeamType teamType)
	{
		if(_winner != TeamType.NONE)
			return;

		_winner = teamType;
	}

	//region Implementation & Override
	@Override
	public void stopEvent(boolean force)
	{
		for(IntObjectPair<ScheduledFuture<?>> pair : _deadList.entrySet())
			pair.getValue().cancel(true);

		_deadList.clear();

		super.stopEvent(force);

        Announcements.announceToAll(getName() + " event is over. " + _winner + " team won.");
	}

	@Override
	protected void actionUpdate(boolean start, Player player)
	{
		if(!start)
			player.removeRadar();
	}

	@Override
	public void action(String name, boolean start)
	{
		if(Objects.equals(name, UPDATE_ARROW))
			updateArrowInPlayers();
		else
			super.action(name, start);
	}

	@Override
	public int getInstantId()
	{
		return 600;
	}

	@Override
	protected Location getTeleportLoc(TeamType team)
	{
		List<CtfBaseObject> objects = getObjects(BASES);

		return Location.findAroundPosition(objects.get(team.ordinalWithoutNone()).getLoc(), 100, 200, _reflection.getGeoIndex());
	}

    @Override
    protected void reward(List<EventPlayerObject> winners, List<EventPlayerObject> losers)
    {
        super.reward(winners, losers);

        ImmutableList<EventPlayerObject> list = ImmutableList.<EventPlayerObject>builder()
                .addAll(getObjects(TeamType.BLUE))
                .addAll(getObjects(TeamType.RED))
                .build();
        Optional<Player> optionalPlayer = list.stream()
				.filter(eventPlayerObject -> eventPlayerObject.getPoints("EVENT_POINTS") > 0)
                .max(Comparator.comparingLong(playerObject -> playerObject.getPoints("EVENT_POINTS")))
                .map(EventPlayerObject::getPlayer);
        optionalPlayer.ifPresent(player ->
        {
            Arrays.stream(topKillerRewards)
                    .forEach(reward -> ItemFunctions.addItem(player, reward.getId(), reward.getCount()));
        });
    }

	@Override
	protected boolean canWalkInWaitTime()
	{
		return false;
	}

	@Override
	protected void onRevive(Player player)
	{
        player.dispelDebuffs();

        player.setCurrentMp(player.getMaxMp());
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp(), false);

        resetSkills(player);
	}

	@Override
	protected void onTeleportOutsideOrExit(List<EventPlayerObject> objects, EventPlayerObject eventPlayerObject, boolean exit)
	{
		objects.remove(eventPlayerObject);

        if(exit)
            eventPlayerObject.clear();
	}

	@Override
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		r.clear();
	}

	@Override
	public boolean canResurrect(Creature active, Creature target, boolean force, boolean quiet)
	{
		CaptureTeamFlagEvent cubeEvent = target.getEvent(CaptureTeamFlagEvent.class);
		if(cubeEvent == this)
		{
			if(!quiet)
				active.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		else
			return true;
	}

	@Override
	public void onAddEvent(GameObject o)
	{
		super.onAddEvent(o);
		if(o.isPlayer())
			o.getPlayer().addListener(_deathListener);
	}

	@Override
	public void onRemoveEvent(GameObject o)
	{
		super.onRemoveEvent(o);
		if(o.isPlayer())
			o.getPlayer().removeListener(_deathListener);
	}

	@Override
	public void onDie(Player player, Creature killer)
	{
	}

    @Override
    public synchronized void checkForWinner()
    {
        if(_state == State.NONE)
            return;

        TeamType winnerTeam = TeamType.NONE;
        for(TeamType team : TeamType.VALUES)
        {
            List<EventPlayerObject> objects = getObjects(team);

            long teamFlagCount = objects.stream()
                    .mapToLong(playerObject -> playerObject.getPoints("EVENT_POINTS"))
                    .sum();
            if(teamFlagCount == 1)
            {
                winnerTeam = team;
            }
        }

        if(winnerTeam != TeamType.NONE)
        {
            setWinner(winnerTeam);
            stopEvent(false);
        }
    }
	//endregion
}
