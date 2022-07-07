package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.utils.Location;

import java.util.*;
import java.util.function.Consumer;

public class DuelSnapshotObject
{
	private final TeamType _team;
	private Player _player;
	protected int _classIndex;
	private Location _returnLoc;
	protected double _currentHp;
	protected double _currentMp;
	protected double _currentCp;
	protected List<Abnormal> _effects;
	protected Collection<ShortCut> shortCuts;

	private boolean _isDead;

	public DuelSnapshotObject(Player player, TeamType team, boolean store)
	{
		this(player, team, store, false);
	}

	public DuelSnapshotObject(Player player, TeamType team, boolean store, boolean storeShortCuts)
	{
		_effects = Collections.emptyList();
		_player = player;
		_team = team;
		if(store)
			store(storeShortCuts);
	}

	public void store()
	{
		store(false);
	}

	public void store(boolean storeShortCuts)
	{
		_classIndex = _player.getActiveSubClass().getIndex();
		_returnLoc = _player.getStablePoint() == null ? _player.getReflection().getReturnLoc() == null ? _player.getLoc() : _player.getReflection().getReturnLoc() : _player.getStablePoint();
		_currentCp = _player.getCurrentCp();
		_currentHp = _player.getCurrentHp();
		_currentMp = _player.getCurrentMp();
		Collection<Abnormal> effectList = _player.getAbnormalList().getEffects();
		if(!effectList.isEmpty())
		{
			_effects = new ArrayList<>(effectList.size());
			for(Abnormal e : effectList)
			{
				if(e.getSkill().isToggle())
					continue;
				Abnormal effect = e.getTemplate().getEffect(e.getEffector(), e.getEffected(), e.getSkill());
				effect.setDuration(e.getDuration());
				effect.setTimeLeft(e.getTimeLeft());
				_effects.add(effect);
			}
		}

		if(storeShortCuts)
			shortCuts = _player.getAllShortCuts();
	}

	public void restore()
	{
		if(_player == null)
			return;
		for(Abnormal e : _player.getAbnormalList().getEffects())
			if(!e.getSkill().isToggle())
				e.exit();
		if(_classIndex == _player.getActiveSubClass().getIndex())
		{
			for(Abnormal e : _effects)
				e.schedule();
			_player.setCurrentCp(_currentCp);
			_player.setCurrentHpMp(_currentHp, _currentMp);

			if(shortCuts != null)
				_player.restoreShortCuts(shortCuts);
		}
		else
		{
			_player.setCurrentCp(_player.getMaxCp());
			_player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
		}
	}

	public void teleportBack()
	{
		if(_player == null)
			return;
		_player.setStablePoint(null);
		ThreadPoolManager.getInstance().schedule(() -> {
			if(_player.isMoveBlocked())
				_player.stopMoveBlock();
			_player.stopFrozen();
			_player.teleToLocation(_returnLoc, ReflectionManager.MAIN);
		}, 5000L);
	}

	public void blockUnblock()
	{
		if(_player == null)
			return;
		_player.block();
		List<Servitor> servitors = _player.getServitors();
		for(Servitor servitor : servitors)
			servitor.block();

		ThreadPoolManager.getInstance().schedule(() -> {
			_player.unblock();
			for(Servitor servitor : servitors)
				servitor.unblock();
		}, 3000L);
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isDead()
	{
		return _isDead;
	}

	public void setDead()
	{
		_isDead = true;
	}

	public Location getLoc()
	{
		return _returnLoc;
	}

	public TeamType getTeam()
	{
		return _team;
	}

	public Location getReturnLoc()
	{
		return _returnLoc;
	}

	public void clear()
	{
		_player = null;
	}

	public void ifPlayerExist(Consumer<Player> consumer)
	{
		if(_player != null)
			consumer.accept(_player);
	}

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(!(o instanceof DuelSnapshotObject))
            return false;
        DuelSnapshotObject that = (DuelSnapshotObject) o;
        return Objects.equals(_player, that._player);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(_player);
    }
}
