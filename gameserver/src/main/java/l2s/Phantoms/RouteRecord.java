package  l2s.Phantoms;

import l2s.Phantoms.enums.RouteType;
import  l2s.Phantoms.objects.TrafficScheme.Agation;
import l2s.Phantoms.objects.TrafficScheme.EquipItem;
import  l2s.Phantoms.objects.TrafficScheme.MoveDelay;
import  l2s.Phantoms.objects.TrafficScheme.PCastSkill;
import  l2s.Phantoms.objects.TrafficScheme.PSocialAction;
import  l2s.Phantoms.objects.TrafficScheme.PUseItem;
import  l2s.Phantoms.objects.TrafficScheme.Pet;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import  l2s.Phantoms.objects.TrafficScheme.Point;
import l2s.Phantoms.objects.TrafficScheme.RouteTask;
import  l2s.Phantoms.objects.TrafficScheme.TargetNpc;
import  l2s.Phantoms.objects.TrafficScheme.Teleport;
import  l2s.Phantoms.objects.TrafficScheme.Trader;
import l2s.Phantoms.parsers.PhantomRouteParser;
import  l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import  l2s.gameserver.model.instances.NpcInstance;
import  l2s.gameserver.model.items.ItemInstance;
import  l2s.gameserver.utils.Location;

public class RouteRecord
{
	public PhantomRoute _record;
	private long move_delay = 0;
	private long item_delay = 0;
	private long skill_delay = 0;

	public Player player;
	private boolean isLogging;

	private boolean enterOffshoreZone = false;
	private boolean startRecTrade = false;

	public RouteRecord(Player _player)
	{
		player = _player;
		_record = new PhantomRoute("", RouteType.SOLO, 0, _player);
		_record.addTask(new RouteTask(0));
	}

	public PhantomRoute getRoute()
	{
		return _record;
	}

	public void addPoints(Point point)
	{
		setItemDelay(0);
		if (_record.getTask().isEmpty())
			_record.addTask(new RouteTask(0));
		else
		_record.getTask().get(0).addPoints(point);
	}

	public void setMoveDelay(long currentTimeMillis)
	{
		move_delay = currentTimeMillis;
	}

	public long getCurrentMoveDelay()
	{
		return move_delay;
	}

	/**
	 * Выполняем в момент остановки движения (или изменения конечной точки)
	 */
	public void stopMove(boolean validate)
	{
		if(!isLogging() || startRecTrade)
			return;

		addPoints(new Point(player.getLoc(), Rnd.get(10))); // пишем новою точку

		if(validate)// полная остановка, запишем текущее время
			setMoveDelay(System.currentTimeMillis());
	}

	/** Выполняем на стерте движения */
	public void StartMove()
	{
		if(!isLogging() || startRecTrade)
			return;
		if(getCurrentMoveDelay() == 0)
			return;

		long delay = System.currentTimeMillis() - getCurrentMoveDelay();
		int delay_sec = 0;
		if(delay > 0)
			delay_sec = (int) delay / 1000; // преобразовать в секунды, округлить до целого

		if(delay_sec <= 1) // не пишем все что меньше 2 секунд
			return;
		setMoveDelay(0);
		if(_record.getPointsFirstTask().size() == 0)
			_record.addPointsFirstTask(new Point(player.getLoc(), 5));

		_record.getPointsFirstTask().get(getPointsSize()).setDelay(new MoveDelay(delay_sec, Rnd.get((int) (delay_sec / 2))));
	}

	private int getPointsSize()
	{
		return _record.getPointsFirstTask().size() - 1;
	}

	public void setMountAgation(Agation mountAgation)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
			_record.getPointsFirstTask().get(getPointsSize()).addMountAgation(mountAgation);
	}

	public void setTeleport(Location loc)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
			_record.getPointsFirstTask().get(getPointsSize()).setTeleport(new Teleport(loc));
	}

	public void setFraction(Fraction fraction)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
			_record.getPointsFirstTask().get(getPointsSize()).setFraction(fraction);
	}
	
	public void setTarget(int npc_id)
	{
		if(startRecTrade)
			return;
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
			_record.getPointsFirstTask().get(getPointsSize()).setTarget(new TargetNpc(npc_id));
	}

	public void setUseItem(PUseItem pUseItem)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
		{
			if(getItemDelay() != 0)
			{
				long delay = System.currentTimeMillis() - getItemDelay();
				if(delay > 250)
					_record.getPointsFirstTask().get(getPointsSize()).setLastItemDelay((int) delay);
			}
			if(player.getTarget() != null && pUseItem.getItemDelay() > 250)
			{
				if(player.getTarget().isNpc())
					pUseItem.setTargetId(((NpcInstance) player.getTarget()).getNpcId());
				else if(player.getTarget() == player)
					pUseItem.setTargetId(-1);
			}
			_record.getPointsFirstTask().get(getPointsSize()).addUseitem(pUseItem);
			setItemDelay(System.currentTimeMillis());
		}

	}

	public void setCheckTrader()
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
		{
			if(isEnterOffshoreZone())
			{
				startRecTrade = true;
				if(_record.getPointsFirstTask().get(getPointsSize()).getTrader() == null)
					_record.getPointsFirstTask().get(getPointsSize()).setTrader(new Trader());
				else
					_record.getPointsFirstTask().get(getPointsSize()).getTrader().incCount();
			}
		}
	}

	public void addCastSkill(PCastSkill castSkill, Creature target)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
		{
			if(isEnterOffshoreZone())
				return;
			if(getSkillDelay() != 0)
			{
				long delay = System.currentTimeMillis() - getSkillDelay();
				if(delay > 250)
					_record.getPointsFirstTask().get(getPointsSize()).setLastSkillDelay((int) delay);
			}
			if(target != null && castSkill.getSkillDelay() > 250)
			{
				if(target.isNpc())
					castSkill.setTargetId(((NpcInstance) player.getTarget()).getNpcId());
				else if(target == player)
					castSkill.setTargetId(-1);
			}
			_record.getPointsFirstTask().get(getPointsSize()).addCastSkill(castSkill);
			setSkillDelay(System.currentTimeMillis());
		}

	}

	public void newRecord()
	{
		_record = new PhantomRoute("", RouteType.SOLO, 0, player);
		_record.addTask(new RouteTask(0));
	}

	public void stopRecord(boolean exit)
	{
		if(getRoute().getPointsFirstTask().size() > 10)
		{
			getRoute().setFraction(player.getFraction());
			getRoute().setClassId(player.getClassId().getId());

			if(getRoute().getLvl() == 0) // установим уровень игрока
				getRoute().setLvl(player.getLevel());
			PhantomRouteParser.getInstance().addNewRoute(getRoute());
			//PhantomRouteParser.getInstance().SavePhantomRoute();
		}

		setLogging(false);
		if(!exit)
		{
			_record = new PhantomRoute("", RouteType.SOLO, 0, player);
			_record.addTask(new RouteTask(0));
		}
	}

	public boolean isLogging()
	{
		if(!Config.RECORDING_ROUTE || player.isGM()||  player.isFlying() || player.isPhantom() || player.isInOlympiadMode() || player.getOlympiadGame() != null || player.getReflectionId() > 0/* || player.isInPvPEvent()*/ || player.isInZoneBattle() || player.isInSiegeZone() || player.getParty() != null || player.isInDuel() || player.getPvpFlag() > 0 || player.getKarma() > 0)
		{
			stopRecord(false);
			return false;
		}

		if(player.isInPeaceZoneOld())
			setLogging(true);

		return isLogging;
	}

	public void setLogging(boolean isLogging)
	{
		this.isLogging = isLogging;
	}

	public long getItemDelay()
	{
		return item_delay;
	}

	public void setItemDelay(long item_delay)
	{
		this.item_delay = item_delay;
	}

	public long getSkillDelay()
	{
		return skill_delay;
	}

	public void setSkillDelay(long skill_delay)
	{
		this.skill_delay = skill_delay;
	}

	public boolean isEnterOffshoreZone()
	{
		return enterOffshoreZone;
	}

	public void setEnterOffshoreZone(boolean enterOffshoreZone)
	{
		this.enterOffshoreZone = enterOffshoreZone;
	}

	public void setStartRecTrade(boolean b)
	{
		this.startRecTrade = b;
	}

	public void addSocialAction(int value)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
		{
			_record.getPointsFirstTask().get(getPointsSize()).setSocial(new PSocialAction(value));
		}
	}

	public void summonPet(ItemInstance controlItem)
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
			_record.getPointsFirstTask().get(getPointsSize()).addSummonPet(new Pet(controlItem.getItemId(), false));
	}

	public void addEquipItem(ItemInstance i) 
	{
		if(_record.getPointsFirstTask() != null && _record.getPointsFirstTask().size() > 0)
		{
			_record.getPointsFirstTask().get(getPointsSize()).setEquipItem(new EquipItem(i.getItemId()));
		}
	}
}