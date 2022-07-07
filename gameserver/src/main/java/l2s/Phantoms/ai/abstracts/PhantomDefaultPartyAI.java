package l2s.Phantoms.ai.abstracts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.Phantoms.Utils.ClassesDictionary;
import l2s.Phantoms.ai.tasks.party.CastPartyRecallTask;
import l2s.Phantoms.ai.tasks.party.ChangePartyStateTask;
import l2s.Phantoms.ai.tasks.party.PartyTask;
import l2s.Phantoms.enums.PartyState;
import l2s.Phantoms.enums.PartyType;
import l2s.Phantoms.enums.PhantomType;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.Phantoms.objects.TrafficScheme.Point;
import l2s.Phantoms.objects.TrafficScheme.RouteTask;
import l2s.Phantoms.taskmanager.BSoeTask;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public abstract class PhantomDefaultPartyAI
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomDefaultPartyAI.class);
	protected ScheduledFuture<?> _mainTask = null;
	protected ScheduledFuture<?> _subTask = null;
	protected ScheduledFuture<?> _pRecallTask = null;

	protected ArrayList<Player> _all_members;
	protected Player _partyLeader;
	protected Player _partyAssister;
	protected int _party_id;
	protected int _party_cooldown;
	protected int _regroupToLeaderChance;
	protected int _regroupToPlaceChance;
	protected int _randomMoveChance;
	protected byte _is_moving1;
	protected ArrayList<Player> _nukers;
	protected ArrayList<Player> _melee;
	protected ArrayList<Player> _healers;
	protected ArrayList<Player> _supports;
	protected ArrayList<Player> _disablers;
	protected ArrayList<Player> _tanks;
	protected PartyType _party_type;
	protected PartyState _partyState;
	private Creature _PartyTarget = null; // цель
	public int currentPointCounter = 0;
	private Point currentPointCoordinate = null;
	private long TimeoutAfterServerStartup = 0;
	private boolean partyInSiege = false;
	private PhantomRoute _cpRoute = null;
	private RouteTask _routeTask = null;

	private long _despawnTime = 0;
	private boolean superpointWait;

	public abstract void doPeaceAction();

	public abstract void doBattleAction();

	public abstract void getAndSetTarget();

	public abstract void onPartyMemberDebuffed(Player member, Skill skill);

	public abstract void onPartyMemberAttacked(Player member, Creature attacker);

	public Point getCurrentPointCoordinate()
	{
		return currentPointCoordinate;
	}

	public PhantomDefaultPartyAI(StatsSet set)
	{
		_partyLeader = null;
		_partyAssister = null;
		_all_members = new ArrayList<Player>();
		_party_id = set.getInteger("partyId", 0);
		_party_type = PartyType.valueOf(set.getString("partyType", "suspended"));
		_party_cooldown = (int) TimeUnit.SECONDS.toMillis(set.getInteger("partyCooldown", 0));
		_regroupToLeaderChance = set.getInteger("regroupToLeaderChance", 1);
		_regroupToPlaceChance = set.getInteger("regroupToPlaceChance", 1);
		_randomMoveChance = set.getInteger("randomMoveChance", 25);
		_nukers = new ArrayList<Player>();
		_melee = new ArrayList<Player>();
		_healers = new ArrayList<Player>();
		_supports = new ArrayList<Player>();
		_disablers = new ArrayList<Player>();
		_tanks = new ArrayList<Player>();
		_partyState = PartyState.peace;
	}

	public void setRoute(PhantomRoute cpRoute)
	{
		superpointWait = false;
		currentPointCounter = 0;
		currentPointCoordinate = null;
		TimeoutAfterServerStartup = 0;
		_cpRoute = cpRoute;
		_routeTask = cpRoute.getTaskGroupId(0).get(0);
	}

	public PhantomRoute getRoute()
	{
		return _cpRoute;
	}

	public void doAction()
	{
		switch(getPartyLeader().getPhantomType())
		{
			case PHANTOM_CLAN_MEMBER:
			{

				if(getPartyLeader().getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER && !getSubTask())
					if(getPartyPercentDead() == 1.) // проверяем, умерли ли все
					{
						changePartyState(PartyState.peace);
						initSubTask(new ChangePartyStateTask(getPartyId(), 1), Rnd.get(2, 7) * 1000);
						return;
					}

				if(/*(getDeadResurrectMan() == 1. &&*/ getPartyPercentDead() > 0.5 || getPartyState() == PartyState.peace) // умерли хилы, и часть пати 
				{
					if(getDeadResurrectMan() != 1. && getIsInPeaceZoneResurrectMan() != 1. && !getPRTask()) // есть хоть 1 живой хил в боевой зоне запустим таск на пр
						initPRTask(new CastPartyRecallTask(getPartyId()), 2000);

					if(!getPRTask()) // если таск на пр не запущен можно вставать в город и жмакать бсое
					{
						for(Player member : getAllMembers())
						{
							// бот не в мирной зоне, хилы умерли и бот не мертвый
							if(!member.isInPeaceZone())
							{
								if(!member.isDead())
								{
									if(!member.phantom_params.getSoeTask()) // кастуем бсое
										member.phantom_params.initSoeTask(new BSoeTask(member), Rnd.get(3000));
								}
								else// бот мертв
								{
									member.teleToClosestTown();
									member.doRevive(); // воскрешаем
								}
							}
						}
					}
					if(getPartyState() != PartyState.peace && !getSubTask())
					{
						changePartyState(PartyState.peace);
						initSubTask(new ChangePartyStateTask(getPartyId(), 1), Rnd.get(2, 7) * 1000);
					}
				}
				break;
			}

			case PHANTOM_INSTANCES:
				break;
			default:
				break;

		}
		switch(_partyState)
		{
			case battle:
			{
				doBattleAction();
				getAndSetTarget();
				if(getPartyAssister() != null && (getPartyAssister().phantom_params.getLockedTarget() == null || getPartyAssister().phantom_params.getLockedTarget().isDead()))
					doFollowAction();
			}
				break;
			case mission_to_kill:
			{
				doBattleAction();
				getAndSetTarget();
			}
				break;
			case peace:
				doPeaceAction();
				doFollowAction();
				break;
			case route:
				//if(this.InSiege())
					//doRouteSiegeAction();
				//else
					doRouteAction();
				doFollowAction();
				break;
			default:
				break;
		}
	}

	boolean _isClParty = false;

	public boolean isClParty()
	{
		return _isClParty;
	}

	public void setClParty(boolean b)
	{
		_isClParty = b;
	}

	private boolean isPartyDistance(int dist)
	{
		if(currentPointCoordinate == null)
			return false;
		for(Player member : getAllMembers())
		{
			if(currentPointCoordinate.getRegroupDistance() < 50)// в узких местах чекнуть высоту, по хорошему вынесты в отдельный конфиг
				if(Math.abs(member.getZ() - currentPointCoordinate.getLoc().getZ()) > 50)
					return false;
			if(member.getDistance(currentPointCoordinate.getLoc()) > dist)
				return false;
		}
		return true;
	}

	public boolean isMovingParty()
	{
		for(Player member : getAllMembers())
			if(member.isMoving())
				return true;
		return false;
	}

	private boolean isMoving()
	{
		if(this.getPartyAssister().isMoving())
		{
			if(currentPointCoordinate == null)
				return true;
			if(currentPointCounter <= _routeTask.getPoints().size() - 1)
			{
				if(!currentPointCoordinate.getAttachments())
				{
					if(currentPointCounter + 1 <= _routeTask.getPoints().size() - 1 && !_routeTask.getPoints().get(currentPointCounter + 1).getAttachments())
					{
						if(this.getPartyAssister().getDistance(_routeTask.getPoints().get(currentPointCounter + 1).getLoc()) <= 390)
						{
							//GmListTable.broadcastMessageToGMs("getPartyAssister().getDistance <= 20");
							return true;
						}
					}
				}
			}
			//GmListTable.broadcastMessageToGMs("isMoving() return false");
			return false;
		}
		else
		{
			//GmListTable.broadcastMessageToGMs("isMoving() return true");
			return true;
		}
	}

	public void doFollowAction()
	{
		if(/*getPartyAssister() ==null ||*/ getAllMembers() == null || currentPointCoordinate == null)
			return;
		Location loc = currentPointCoordinate.getLoc();

		for(Player member : getAllMembers())
		{
			//	if (member.getDistance3D(currentPointCoordinate.getLoc()) < 70 && (currentPointCoordinate!=null && currentPointCoordinate.getRnd()!= 0))
			//continue;
			/*	if (member.isInPeaceZone())
				{
					if (member.getDistance3D(loc) > currentPointCoordinate.getRegroupDistance())
					{
						if (member.isMoving() && member.getAI().getNextAction() == null)
							member.getAI().setNextAction(NextAction.MOVE, Location.coordsRandomize(loc, 50, 180), 20, true, false);
						else
						member.moveToLocation(Location.coordsRandomize(loc, 50, 180), 20, true);
					}
				}
				else
				{*/
			/*if (currentPointCoordinate!=null &&currentPointCoordinate.getRnd()== 0 && Math.abs(member.getZ() - currentPointCoordinate.getLoc().getZ()) > 10)
			{
				if (member.isMoving() && member.getAI().getNextAction() == null)
					member.getAI().setNextAction(NextAction.MOVE, loc, 5, true, false);
				else
				member.moveToLocation(loc, 5, true);
				//member.moveToLocation(Location.coordsRandomize(getPartyAssister().getLoc(), 5, 15), 5, true);
			}else*/
			if(Rnd.chance(getp1()))
			{
				if(member.phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("indexOf " + member);

				if(member.getDistance(loc) > getp4())
				{
					Location _loc = loc;

					switch(getAllMembers().indexOf(member))
					{
						case 1:
							_loc = Location.coordsRandomize(new Location(_loc.getX() - currentPointCoordinate.getRnd(), _loc.getY() + currentPointCoordinate.getRnd(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 2:
							_loc = Location.coordsRandomize(new Location(_loc.getX(), _loc.getY() + currentPointCoordinate.getRnd(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 3:
							_loc = Location.coordsRandomize(new Location(_loc.getX() + currentPointCoordinate.getRnd(), _loc.getY() + currentPointCoordinate.getRnd(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 4:
							_loc = Location.coordsRandomize(new Location(_loc.getX() - currentPointCoordinate.getRnd(), _loc.getY(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 5:
							_loc = Location.coordsRandomize(new Location(_loc.getX(), _loc.getY(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 6:
							_loc = Location.coordsRandomize(new Location(_loc.getX() + currentPointCoordinate.getRnd(), _loc.getY(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 7:
							_loc = Location.coordsRandomize(new Location(_loc.getX() - currentPointCoordinate.getRnd(), _loc.getY() - currentPointCoordinate.getRnd(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 8:
							_loc = Location.coordsRandomize(new Location(_loc.getX(), _loc.getY() - currentPointCoordinate.getRnd(), _loc.getZ()), 5, getRandomRMax());
							break;
						case 9:
							_loc = Location.coordsRandomize(new Location(_loc.getX() + currentPointCoordinate.getRnd(), _loc.getY() - currentPointCoordinate.getRnd(), _loc.getZ()), 5, getRandomRMax());
							break;
					}
					if(member.phantom_params.getGmLog())
						GmListTable.broadcastMessageToGMs("indexOf end");
					if(member.isMoving() && member.getAI().getNextAction() == null)
						member.getAI().setNextAction(AINextAction.MOVE, _loc, 0, true, false);
					else
						member.moveToLocation(_loc, 0, true);
				}
			}
			//}
			if(member.getDistance3D(getPartyAssister()) > 2000)
				changePartyState(PartyState.peace);
		}
	}

	private int getRandomRMax()
	{
		int dist = (int) (currentPointCoordinate.getRegroupDistance() / 2);
		if(dist < 50)
			return 50;

		return dist;
	}

	private void moveToGk(Player member)
	{
		List<NpcInstance> gk = World.getAroundNpc(currentPointCoordinate.getLoc(), 50, 100).stream().filter(npc -> npc != null && !npc.getTemplate().getTeleportList().isEmpty()).collect(Collectors.toList());
		if(!gk.isEmpty())
		{
			Location loc = Rnd.get(gk).getLoc();
			if(loc != null)
			{
				if(!member.isInRange(loc, 50))
					member.moveToLocation(Rnd.get(gk).getLoc(), Rnd.get(20, 50), true);
			}
		}
	}

	public void doRouteAction()
	{
		if(this.getPartyAssister() == null)
			return;
		if(getPartyPercentDead() != 0)// мертв хоть 1 член пати, меняем режим обратно на бой
		{
			changePartyState(PartyState.battle);
			return;
		}
		if(_cpRoute == null)
			return;
		if(_routeTask != null && !this.getPartyAssister().isCastingNow() && isMoving() && !this.getPartyAssister().isAttackingNow() && isPartyDistance(getp4()) && System.currentTimeMillis() > TimeoutAfterServerStartup)
		{
			currentPointCoordinate = _routeTask.getPoints().get(currentPointCounter);
			if(currentPointCoordinate == null || getPartyAssister() == null)
				return;
			if(this.getPartyAssister().getDistance(currentPointCoordinate.getLoc()) > 10000)
			{
				changePartyState(PartyState.peace);
				return;
			}
			if(this.getPartyAssister().getDistance(currentPointCoordinate.getLoc()) > 20)
			{
				//GmListTable.broadcastMessageToGMs("getLoc >20");
				getPartyAssister().moveToLocation(currentPointCoordinate.getLoc(), 0, true);
				return;
			}
			if(currentPointCounter >= _routeTask.getPoints().size() - 1) // дошли до конца
			{
				if(currentPointCoordinate.getSelectRandomTask() != null)//выбрать следующее рандомное задание 
				{
					List<RouteTask> new_task = _cpRoute.getTaskGroupId(currentPointCoordinate.getSelectRandomTask().getId());
					if(new_task != null)
					{
						superpointWait = false;
						currentPointCounter = 0;
						currentPointCoordinate = null;
						TimeoutAfterServerStartup = 0;
						if(new_task.size() == 1)
							_routeTask = new_task.get(0);
						else
							_routeTask = Rnd.get(new_task);
						return;
					}
				}

				if(Rnd.chance(50))// поменять тип или выбрать другой маршрут
					changePartyState(PartyState.battle);
				else
				{
					if(!getPRTask())
						initPRTask(new CastPartyRecallTask(getPartyId()), 5000);

					// запускаем такс телепорта в "отстойник" и выдаем новый маршрут
					changePartyState(PartyState.peace);
					initSubTask(new ChangePartyStateTask(getPartyId(), 1), Rnd.get(20, 60) * 1000);// тупим в городе 20-60 секунд
					return;
				}
			}
			else
			{
				//GmListTable.broadcastMessageToGMs(getPartyAssister() + currentPointCoordinate.getLoc().toXYZString());
				// moveToLocation(currentPointCoordinate.getLoc());
				getPartyAssister().moveToLocation(currentPointCoordinate.getLoc(), 0, true);
			}
			if(currentPointCoordinate != null && currentPointCoordinate.getTeleport() != null)
			{
				for(Player member : getAllMembers())
					moveToGk(member);
			}
			if(currentPointCoordinate.getDelay() != null && currentPointCoordinate.getDelay().getSeconds() > 0 && !superpointWait && (currentPointCoordinate.getRndDelay() == 0 ? true : System.currentTimeMillis() - TimeoutAfterServerStartup > currentPointCoordinate.getRndDelay()))
			{
				currentPointCoordinate.setRndDelay(currentPointCoordinate.getDelay().getSeconds() + (currentPointCoordinate.getDelay().getRnd() == 0 ? 0 : Rnd.get(currentPointCoordinate.getDelay().getRnd())));

				TimeoutAfterServerStartup = System.currentTimeMillis() + (currentPointCoordinate.getRndDelay() * 1000);
				superpointWait = true;
				return;
			}

			if(currentPointCoordinate.getTeleport() != null)
			{
				//GmListTable.broadcastMessageToGMs("Teleport: " + currentPointCoordinate.getTeleport().toString());
				// GmListTable.getAllGMs().forEach(p -> p.teleToLocation(Location.coordsRandomize(currentPointCoordinate.getTeleport().getLoc(), 10, 50)));
				for(Player member : getAllMembers())
					member.teleToLocation(Location.coordsRandomize(currentPointCoordinate.getTeleport().getLoc(), 10, 50));
			}

			if(currentPointCounter < _routeTask.getPoints().size() - 1)
				currentPointCounter++;
		}
	}

	public void startAITask(long delay)
	{
		try
		{
			abortMainTask();
			_mainTask = ThreadPoolManager.getInstance().PhantomAiScheduleAtFixedRate(new PartyTask(getPartyId()), delay, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean getPRTask()
	{
		if(_pRecallTask != null)
			return true;
		return false;
	}

	public void initPRTask(Runnable r, long delay)
	{
		try
		{
			abortPRTask();
			_pRecallTask = ThreadPoolManager.getInstance().schedule(r, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortPRTask()
	{
		if(_pRecallTask != null)
		{
			_pRecallTask.cancel(true);
			_pRecallTask = null;
		}
	}

	public void initSubTask(Runnable r, long delay)
	{
		try
		{
			abortSubTask();
			_subTask = ThreadPoolManager.getInstance().schedule(r, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortMainTask()
	{
		if(_mainTask != null)
		{
			_mainTask.cancel(true);
			_mainTask = null;
		}
	}

	public boolean getMainTask()
	{
		if(_mainTask != null && !_mainTask.isDone())
			return true;
		return false;
	}

	public boolean getSubTask()
	{
		if(_subTask != null && !_subTask.isDone())
			return true;
		return false;
	}

	public void abortSubTask()
	{
		if(_subTask != null)
		{
			_subTask.cancel(true);
			_subTask = null;
		}
	}

	public void setPartyTarget(Creature target)
	{
		try
		{
			_PartyTarget = target;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Creature getPartyTarget()
	{
		return _PartyTarget;
	}

	public void changePartyState(PartyState state)
	{
		_partyState = state;
	}

	public PartyState getPartyState()
	{
		return _partyState;
	}

	public void setPartyAssister(Player Assister)
	{
		_partyAssister = Assister;
	}

	public Player getPartyAssister()
	{
		return _partyAssister;
	}

	public void setPartyLeader(Player pl)
	{
		_partyLeader = pl;
	}

	public Player getPartyLeader()
	{
		return _partyLeader;
	}

	public Location getPartyLoc()
	{
		return _partyLeader.getLoc();
	}

	public int getRegroupToLeaderChance()
	{
		return _regroupToLeaderChance;
	}

	public int getRegroupToPlaceChance()
	{
		return _regroupToPlaceChance;
	}

	public int getRandomMoveChance()
	{
		return _randomMoveChance;
	}

	public int getPartyId()
	{
		return _party_id;
	}

	public PartyType getPartyType()
	{
		return _party_type;
	}

	public int getPartyCooldown()
	{
		return _party_cooldown;
	}

	public void addPartyMember(Player actor)
	{
		if(actor.phantom_params.getPhantomAI().isHealer())
			_healers.add(actor);
		else if(actor.phantom_params.getPhantomAI().isNuker())
			_nukers.add(actor);
		else if(actor.phantom_params.getPhantomAI().isMelee())
			_melee.add(actor);
		else if(actor.phantom_params.getPhantomAI().isSupport())
			_supports.add(actor);
		else if(actor.phantom_params.getPhantomAI().isDisabler())
			_disablers.add(actor);
		else if(actor.phantom_params.getPhantomAI().isTank())
			_tanks.add(actor);
		_all_members.add(actor);
	}

	public ArrayList<Player> getAllMembers()
	{
		return _all_members;
	}

	public List<Player> getlivingMembers()
	{
		List<Player> _livingMembers = new ArrayList<Player>();
		for(Player member : getAllMembers())
			if(!member.isDead())
				_livingMembers.add(member);
		return _livingMembers;
	}

	public Player getMemberWithMaxHp()
	{
		Player max = null;
		for(Player member : getlivingMembers())
		{
			if(member.isDead())
				continue;
			if(max != null)
			{
				if(member.getCurrentHpPercents() > max.getCurrentHpPercents())
					max = member;
			}
			else
				max = member;
		}
		return max;
	}

	public Player getAnyMelee()
	{
		if(_melee.isEmpty())
			return null;
		return _melee.get(Rnd.get(_melee.size()));
	}

	public Player getAnyNuker()
	{
		if(_nukers.isEmpty())
			return null;
		return _nukers.get(Rnd.get(_nukers.size()));
	}

	public Player getAnyDeathHealer()
	{
		List<Player> healers = new ArrayList<Player>();
		if(_healers.isEmpty())
			return null;
		for(Player healer : _healers)
		{
			if(healer.isDead())
				healers.add(healer);
		}
		if(healers.isEmpty())
			return null;
		return healers.get(Rnd.get(healers.size()));
	}

	public Player getAnyHealer()
	{
		List<Player> healers = new ArrayList<Player>();
		if(_healers.isEmpty())
			return null;
		for(Player healer : _healers)
		{
			if(!healer.isDead())
				healers.add(healer);
		}
		if(healers.isEmpty())
			return null;
		return healers.get(Rnd.get(healers.size()));
	}

	public Player getAnySupport()
	{
		if(_supports.isEmpty())
			return null;
		return _supports.get(Rnd.get(_supports.size()));
	}

	public Player getAnyDisabler()
	{
		if(_disablers.isEmpty())
			return null;
		return _disablers.get(Rnd.get(_disablers.size()));
	}

	public Player getAnyTank()
	{
		if(_tanks.isEmpty())
			return null;
		return _tanks.get(Rnd.get(_tanks.size()));
	}

	public Player getAnyMember()
	{
		if(_all_members.isEmpty())
			return null;
		return _all_members.get(Rnd.get(_all_members.size()));
	}

	public Player getDoomcryer()
	{
		if(_all_members.isEmpty())
			return null;
		for(Player member : _all_members)
		{
			if(member.getClassId().getId() == 116)
				return member;
		}
		return null;
	}

	public ArrayList<Player> getHealers()
	{
		return _healers;
	}

	public Player getAnyResurrectMan()
	{
		if(!_healers.isEmpty())
		{
			for(Player p : _healers)
				if(!p.isDead())
					return p;
		}
		if(!_supports.isEmpty())
		{
			for(Player p : _supports)
				if(!p.isDead())
					return p;
		}
		return null;
	}

	public boolean isMainAssistTaken()
	{
		Creature target_c = this.getPartyTarget();
		if(target_c == null)
			return false;
		/*
		 * if(!target_c.isPlayer()) return false; for(Player pl : _all_members) { if(pl.phantom_params.getPhantomAI().isHealer() || pl.phantom_params.getPhantomAI().isSupport()) continue; if(target_c !=
		 * pl.phantom_params.getLockedTarget()) return false; }
		 */
		return true;
	}

	public boolean isSubAssistTakenByHealers(Player target)
	{
		if(getAnyHealer() == null)
			return true;
		for(Player pl : _healers)
		{
			if(target != pl.phantom_params.getSubTarget())
				return false;
		}
		return true;
	}

	public boolean isSubAssistTakenBySupports(Player target)
	{
		if(getAnySupport() == null)
			return true;
		for(Player pl : _supports)
		{
			if(target != pl.phantom_params.getSubTarget())
				return false;
		}
		return true;
	}

	public void setSubAssist(Creature target)
	{
		if(target == null || target.getPlayer() == null)
			return;
		Player attacker = target.getPlayer();
		int id;
		// если атакующий в пати
		if(attacker.getParty() != null)
		{
			// то берем из его пати сапорта/хилера
			for(Player p : attacker.getParty())
			{
				// и если цель не мертва или не спит (не нужно будить), или достаточно близка
				if(p.isDead() || p.isSleeping() || p.getDistance(getPartyLoc()) > 1500)
					continue;
				id = p.getClassId().getId();
				// то помещаем её в саб-ассист
				if(!_healers.isEmpty() && (ClassesDictionary.isHealer(id)))
				{
					for(Player h : _healers)
						h.phantom_params.setSubTarget(target);
					break;
				}
				if(!_supports.isEmpty() && (ClassesDictionary.isHealer(id) || ClassesDictionary.isMageSupport(id)))
				{
					for(Player s : _supports)
						s.phantom_params.setSubTarget(target);
					break;
				}
			}
		}
		// если никого взять в пати не удалось, то берем в таргет не сапорта, а самого
		// нападающего
		if(!isSubAssistTakenByHealers(attacker) || !isSubAssistTakenBySupports(attacker))
		{
			for(Player h : _healers)
				h.phantom_params.setSubTarget(target);
			for(Player s : _supports)
				s.phantom_params.setSubTarget(target);
		}
	}

	public double getIsInPeaceZoneResurrectMan()
	{
		double all = _healers.size();
		double dead = 0;
		for(Player p : _healers)
			if(p.isInPeaceZone())
				dead++;
		return dead / all;
	}

	public double getDeadResurrectMan()
	{
		double all = _healers.size();
		double dead = 0;
		for(Player p : _healers)
			if(p.isDead())
				dead++;
		return dead / all;
	}

	public Player getDeadPartyMember()
	{
		// сначало проверяем хилеров
		for(Player p : _healers)
			if(p.isDead())
				return p;
		for(Player p : _all_members)
			if(p.isDead())
				return p;
		return null;
	}

	public int getDeadPartyMembersCount()
	{
		int res = 0;
		for(Player p : _all_members)
			if(p.isDead())
				res++;
		return res;
	}

	public double getPartyPercentDead()
	{
		double all = _all_members.size();
		double dead = 0;
		for(Player p : _all_members)
			if(p.isDead())
				dead++;
		return dead / all;
	}

	public void clearAssists()
	{
		for(Player p : _all_members)
		{
			p.phantom_params.setLockedTarget(null);
			// p.phantom_params.setAttackerTarget(null);
			p.phantom_params.setSubTarget(null);
		}
	}

	public void takeMainAssist(Creature target)
	{
		if(target == null)
			return;
		try
		{
			for(Player pl : _all_members)
			{
				if(pl.phantom_params.getPhantomAI().isHealer() || pl.phantom_params.getPhantomAI().isSupport())
					continue;
				pl.phantom_params.setLockedTarget(target);
			}
			Player attacker = target.getPlayer();
			int id;
			// если атакующий в пати, то дизейблерам раздаем ассист по сапорту
			if(attacker != null && attacker.getParty() != null)
			{
				// то берем из его пати сапорта/хилера
				for(Player p : attacker.getParty())
				{
					// и если цель не мертва или не спит (не нужно будить), или достаточно близка
					if(p.isDead() || p.isSleeping() || p.getDistance(getPartyLoc()) > 900)
						continue;
					id = p.getClassId().getId();
					// то помещаем её в саб-ассист
					if(!_disablers.isEmpty() && (ClassesDictionary.isHealer(id) || ClassesDictionary.isMageSupport(id)))
					{
						for(Player h : _disablers)
						{
							h.phantom_params.setSubTarget(p);
						}
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void regroup()
	{
		Player leader = getAnyHealer();
		if(leader == null)
			leader = getPartyLeader();
		Location loc = leader.getLoc();
		moveToLocation(loc);
	}

	/*
	 * регруп в городе
	 */
	public void regroupBotHanter()
	{
		Location spawn = new Location(116589 + (Rnd.chance(50) ? 1 : -1) * Rnd.get(50), 76268 + (Rnd.chance(50) ? 1 : -1) * Rnd.get(50), -2736);
		for(Player p : _all_members)
		{
			if(!p.phantom_params.getPhantomPartyAI().getPRTask())
				continue;
			p.stopPvPFlag();
			p.teleToLocation(spawn);
		}
		changePartyState(PartyState.peace);
	}

	public void moveToLocation(Location loc)
	{
		if(loc == null)
			return;
		for(Player p : _all_members)
			if(!p.isOutOfControl())
				p.moveToLocation(loc, 200, true);
	}

	public boolean isPartyMember(Player player)
	{
		return _all_members.contains(player);
	}

	public void randomMove(Player phantom, int min_range, int max_range)
	{
		Location loc = new Location(phantom.getX() + (Rnd.chance(50) ? 1 : -1) * Rnd.get(min_range, max_range), phantom.getY() + (Rnd.chance(50) ? 1 : -1) * Rnd.get(min_range, max_range), phantom.getZ(), 0);
		if(GeoEngine.canMoveToCoord(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, phantom.getGeoIndex()))
			phantom.moveToLocation(loc, 0, false);
	}

	public Location getRandomMove(Player phantom, int min_range, int max_range)
	{
		Location loc = new Location(phantom.getX() + (Rnd.chance(50) ? 1 : -1) * Rnd.get(min_range, max_range), phantom.getY() + (Rnd.chance(50) ? 1 : -1) * Rnd.get(min_range, max_range), phantom.getZ(), 0);
		if(GeoEngine.canMoveToCoord(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, phantom.getGeoIndex()))
			return loc;
		return null;
	}

	public int size()
	{
		return _all_members.size();
	}

	public boolean isInPeaceZone()
	{
		List<Player> filter_member = _all_members.stream().filter(d -> d != null && d.isInPeaceZone()).collect(Collectors.toList());
		return filter_member.size() == _all_members.size() ? true : false;
	}

	public void setDespawnTime(long currentTimeMillis)
	{
		_despawnTime = currentTimeMillis;
	}

	public long getDespawnTime()
	{
		return _despawnTime;
	}

	int p1 = 70;

	public void setp1(int _p1)
	{
		p1 = _p1;
	}

	public int getp1()
	{
		return p1;
	}

	int p2 = 250;

	public void setp2(int _p2)
	{
		p2 = _p2;
	}

	public int getp2()
	{
		return p2;
	}

	int p3 = 379;

	public void setp3(int _p3)
	{
		p3 = _p3;
	}

	public int getp3()
	{
		return p3;
	}

	int p4 = 460;

	public void setp4(int _p4)
	{
		p4 = _p4;
	}

	public int getp4()
	{
		return p4;
	}

	public boolean InSiege()
	{
		return partyInSiege;
	}

	public void setInSiege(boolean b)
	{
		partyInSiege = b;
	}

	public int getMoving()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
