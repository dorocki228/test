package l2s.gameserver.ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.templates.npc.RandomActions;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Location;

public class NpcAI extends CharacterAI
{
	public static final String WALKER_ROUTE_PARAM = "walker_route_id";
	private static final int WALKER_ROUTE_TIMER_ID = -1000;
	private static final int RANDOM_ACTION_TIMER_ID = -2000;
	private final RandomActions _randomActions;
	private final boolean _haveRandomActions;
	private int _currentActionId;
	private final WalkerRoute _walkerRoute;
	private boolean _haveWalkerRoute;
	private boolean _toBackWay;
	private int _currentWalkerPoint;
	private boolean _delete;
	private int moveFailedAttemps;

	private boolean _isActive;

	public NpcAI(NpcInstance actor)
	{
		super(actor);
		_randomActions = actor.getTemplate().getRandomActions();
		_haveRandomActions = _randomActions != null && _randomActions.getActionsCount() > 0;
		_currentActionId = 0;
		int walkerRouteId = actor.getParameter("walker_route_id", -1);
		_walkerRoute = actor.getTemplate().getWalkerRoute(walkerRouteId);
		_haveWalkerRoute = _walkerRoute != null && _walkerRoute.isValid();
		_toBackWay = false;
		_currentWalkerPoint = -1;
		_delete = false;
		_isActive = false;
	}

	@Override
	protected void onEvtArrived()
	{
		continueWalkerRoute();
	}

	@Override
	protected void onEvtTeleported()
	{
		continueWalkerRoute();
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == WALKER_ROUTE_TIMER_ID)
		{
			if(_haveWalkerRoute)
			{
				if(!(arg1 instanceof Location))
					return;
				if((Boolean) arg2)
				{
					NpcInstance actor = getActor();
					if(actor == null)
						return;
					actor.teleToLocation((Location) arg1);
					continueWalkerRoute();
				}
				else
					moveToLocation((Location) arg1);
			}
		}
		else if(timerId == RANDOM_ACTION_TIMER_ID && _haveRandomActions)
			makeRandomAction();
	}

	protected boolean thinkActive()
	{
		if(_haveWalkerRoute)
		{
			NpcInstance actor = getActor();
			if(!actor.isMoving() && !haveTask(WALKER_ROUTE_TIMER_ID))
			{
				moveToLocation(actor.getSpawnedLoc());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isActive()
	{
		return _isActive;
	}

	@Override
	public void stopAITask()
	{
		_isActive = false;
	}

	@Override
	public void startAITask()
	{
		_isActive = true;
		if(_haveWalkerRoute)
			moveToNextPoint(0);
		if(_haveRandomActions)
		{
			RandomActions.Action action = _randomActions.getAction(1);
			if(action != null)
                addTask(RANDOM_ACTION_TIMER_ID, Rnd.get(0, action.getDelay()) * 1000L);
		}
	}

	private void continueWalkerRoute()
	{
		if(!isActive() || getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			return;
		if(_haveWalkerRoute)
		{
			if(_currentWalkerPoint == -1)
			{
				moveToNextPoint(0);
				return;
			}

			WalkerRoutePoint route;
			try
			{
				route = _walkerRoute.getPoint(_currentWalkerPoint);
			}
			catch(IndexOutOfBoundsException e)
			{
				_log.error("Can't find {} point in walker route {} for npc {}.",
						_currentWalkerPoint, _walkerRoute.getId(), getActor().getNpcId(), e);
				return;
			}
			if(route == null)
				return;
			NpcInstance actor = getActor();
			int socialActionId = route.getSocialActionId();
			if(socialActionId >= 0)
				actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), socialActionId));
			NpcString phrase = (NpcString) Rnd.get((Object[]) route.getPhrases());
			if(phrase != null)
				Functions.npcSay(actor, phrase);
			moveToNextPoint(route.getDelay());
		}
	}

	private void moveToNextPoint(int delay)
	{
		if(!isActive() || getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			return;
		if(!_haveWalkerRoute)
			return;
		NpcInstance actor = getActor();
		if(actor == null)
			return;
		switch(_walkerRoute.getType())
		{
			case LENGTH:
			{
				if(_toBackWay)
					--_currentWalkerPoint;
				else
					++_currentWalkerPoint;
				if(_currentWalkerPoint >= _walkerRoute.size() - 1)
					_toBackWay = true;
				if(_currentWalkerPoint == 0)
				{
					_toBackWay = false;
					break;
				}
				break;
			}
			case ROUND:
			{
				++_currentWalkerPoint;
				if(_currentWalkerPoint >= _walkerRoute.size())
				{
					_currentWalkerPoint = 0;
					break;
				}
				break;
			}
			case RANDOM:
			{
				if(_walkerRoute.size() > 1)
				{
					int oldPoint = _currentWalkerPoint;
					while(oldPoint == _currentWalkerPoint)
						_currentWalkerPoint = Rnd.get(_walkerRoute.size() - 1);
					break;
				}
				break;
			}
			case DELETE:
			{
				if(_delete)
				{
					actor.deleteMe();
					return;
				}
				++_currentWalkerPoint;
				if(_currentWalkerPoint >= _walkerRoute.size())
				{
					_delete = true;
					break;
				}
				break;
			}
			case FINISH:
			{
				++_currentWalkerPoint;
				if(_currentWalkerPoint >= _walkerRoute.size())
				{
					actor.stopMove();
					break;
				}
				break;
			}
		}
		if(_currentWalkerPoint == -1)
			return;
		WalkerRoutePoint route = _walkerRoute.getPoint(_currentWalkerPoint);
		if(route == null)
			return;
		if(route.isRunning())
			actor.setRunning();
		else
			actor.setWalking();
		if(delay > 0)
            addTask(WALKER_ROUTE_TIMER_ID, route.getLocation(), route.isTeleport(), delay * 1000L);
		else if(route.isTeleport())
		{
			actor.teleToLocation(route.getLocation());
			continueWalkerRoute();
		}
		else
			moveToLocation(route.getLocation());
	}

	private void makeRandomAction()
	{
		if(!isActive())
			return;
		if(!_haveRandomActions)
			return;
		NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
		{
			++_currentActionId;
			if(_currentActionId > _randomActions.getActionsCount())
				_currentActionId = 1;
			RandomActions.Action action = _randomActions.getAction(_currentActionId);
			if(action == null)
				return;
			int socialActionId = action.getSocialActionId();
			if(socialActionId >= 0)
				actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), socialActionId));
			NpcString phrase = action.getPhrase();
			if(phrase != null)
				Functions.npcSay(actor, phrase);
            addTask(RANDOM_ACTION_TIMER_ID, action.getDelay() * 1000L);
		}
		else
            addTask(RANDOM_ACTION_TIMER_ID, 1000L);
	}

	private void moveToLocation(Location loc)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
		{
			actor.setSpawnedLoc(loc);

			if(actor.isMoving())
				return;

            Location currentLoc = actor.getLoc();
            // TODO разобраться почему нпц иногда не доходит пару координат
			if(currentLoc.distance(loc) <= 2)
			{
				if(_currentWalkerPoint != -1)
					moveToNextPoint(0);
				return;
			}

			if(!actor.moveToLocation(loc, 0, false))
			{
			    if(moveFailedAttemps >= 1)
                {
                    _haveWalkerRoute = false;
					_log.error("Npc {} can't move from {} to {} " +
                            "current point {} in walker route {}", actor, currentLoc, loc,
                            _currentWalkerPoint, _walkerRoute.getId());
                    return;
                }

                clientStopMoving();
				actor.teleToLocation(loc);

                moveFailedAttemps++;
			}
		}
	}

	@Override
	public NpcInstance getActor()
	{
		return (NpcInstance) super.getActor();
	}

	protected boolean isHaveRandomActions()
	{
		return _haveRandomActions;
	}

	protected boolean isHaveWalkerRoute()
	{
		return _haveWalkerRoute;
	}
}
