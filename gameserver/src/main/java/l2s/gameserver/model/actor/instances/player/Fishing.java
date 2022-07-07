package l2s.gameserver.model.actor.instances.player;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.FishDataHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExAutoFishAvailable;
import l2s.gameserver.network.l2.s2c.ExFishingEndPacket;
import l2s.gameserver.network.l2.s2c.ExUserInfoFishing;
import l2s.gameserver.templates.fish.*;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Fishing
{
	private final Player _owner;
	private boolean _started = false;
	private boolean _inProcess = false;
	private Location _hookLoc = new Location();
	private RodTemplate _rod = null;
	private LureTemplate _lure = null;
	private ScheduledFuture<?> _processTask = null;

	public Fishing(Player owner)
	{
		_owner = owner;
	}

	public boolean inStarted()
	{
		return _started;
	}

	public boolean isInProcess()
	{
		return _inProcess;
	}

	public Location getHookLocation()
	{
		return _hookLoc;
	}

	public void start(RodTemplate rod, LureTemplate lure)
	{
		_started = true;
		_rod = rod;
		_lure = lure;
		_owner.sendPacket(SystemMsg.YOU_CAST_YOUR_LINE_AND_START_TO_FISH);
		throwHook();
	}

	public void stop()
	{
		_started = false;
		_inProcess = false;
		_hookLoc = new Location();
		_rod = null;
		_lure = null;
		stopProcessTask();
		_owner.sendPacket(SystemMsg.YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING);
		_owner.sendPacket(ExAutoFishAvailable.REMOVE);
		_owner.broadcastPacket(new ExUserInfoFishing(_owner));
		_owner.broadcastPacket(new ExFishingEndPacket(_owner, 2));
	}

	private boolean throwHook()
	{
		WeaponTemplate weaponItem = _owner.getActiveWeaponTemplate();

		if(weaponItem == null || weaponItem.getItemType() != WeaponTemplate.WeaponType.ROD)
			return false;

		if(_rod.getId() != weaponItem.getItemId())
			return false;

		ItemInstance lureItem = _owner.getInventory().getPaperdollItem(8);
		if(lureItem == null || lureItem.getCount() < _rod.getShotConsumeCount())
		{
			_owner.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_BAIT);
			return false;
		}

		if(_lure.getId() != lureItem.getItemId())
		{
			_owner.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_BAIT);
			return false;
		}

		List<FishTemplate> fishes = _lure.getFishes();
		if(fishes.isEmpty())
			return false;

		if(!ItemFunctions.deleteItem(_owner, lureItem, _rod.getShotConsumeCount(), false))
		{
			_owner.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_BAIT);
			return false;
		}

		double shotPower = 1.0 + _owner.getChargedFishshotPower() / 100.0;
		double chancesAmount = 0.0;
		for(FishTemplate fish : fishes)
			chancesAmount += fish.getChance() / (fish.getId() == -1 ? shotPower : 1.0);

		double chanceMod = (100.0 - chancesAmount) / (double) fishes.size();
		ArrayList<FishTemplate> successFishes = new ArrayList<>();
		int tryCount = 0;
		while(successFishes.isEmpty())
		{
			++tryCount;
			for(FishTemplate fish : fishes)
			{
				if(tryCount % 10 == 0)
					chanceMod += 1.0;

				if(!Rnd.chance(fish.getChance() / (fish.getId() == 0 ? shotPower : 1.0) + chanceMod))
					continue;

				successFishes.add(fish);
			}
		}

		FishTemplate fish = Rnd.get(successFishes);
		if(fish == null)
			return false;

		_hookLoc = findHookLocation(_owner);
		if(_hookLoc == null)
			return false;
		_owner.unChargeFishShot();
		stopProcessTask();
		_owner.sendPacket(ExAutoFishAvailable.FISHING);
		_owner.broadcastPacket(new ExUserInfoFishing(_owner));
		_inProcess = true;
		_processTask = ThreadPoolManager.getInstance().schedule(new FishingTask(fish), (long) (TimeUnit.SECONDS.toMillis(Rnd.get(_lure.getDurationMin(), _lure.getDurationMax())) * _rod.getDurationModifier()));
		return true;
	}

	private void stopProcessTask()
	{
		if(_processTask != null)
		{
			_processTask.cancel(false);
			_processTask = null;
		}
	}

	public static Location findHookLocation(Player player)
	{
		int rnd = Rnd.get(50) + 150;
		double angle = PositionUtils.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle - 90);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x1 = -(int) (sin * rnd);
		int y1 = (int) (cos * rnd);
		int x = player.getX() + x1;
		int y = player.getY() + y1;
		//z - уровень карты
		int z = GeoEngine.getHeight(x, y, player.getZ(), player.getGeoIndex()) + 1;

		// Проверяем, что поплавок оказался в воде
        List<Zone> zones = new ArrayList<>();
		World.getZones(zones, new Location(x, y, z), player.getReflection());
        boolean isInWater = false;
        for(Zone zone : zones)
		{
			if(zone.getType() == ZoneType.water)
			{
				//z - уровень воды
				z = zone.getTerritory().getZmax();
				isInWater = true;
				break;
			}
		}

		if(!isInWater)
			return null;

		return new Location(x, y, z);
	}

	private class ThrowHookTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!throwHook())
				stop();
		}
	}

	private class FishingTask implements Runnable
	{
		private final FishTemplate _fish;

		public FishingTask(FishTemplate fish)
		{
			_fish = fish;
		}

		@Override
		public void run()
		{
			_inProcess = false;
			if(_fish.getId() == -1)
			{
				_owner.sendPacket(SystemMsg.THE_BAIT_HAS_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
				_owner.broadcastPacket(new ExUserInfoFishing(_owner));
				_owner.broadcastPacket(new ExFishingEndPacket(_owner, 0));
				_owner.getListeners().onFishing(OptionalInt.empty());
			}
			else
			{
				FishRewardsTemplate rewards = FishDataHolder.getInstance().getRewards(_fish.getRewardType());
				if(rewards != null)
				{
                    long sp = 0;
					for(FishRewardTemplate reward : rewards.getRewards())
					{
						if(_owner.getLevel() < reward.getMinLevel() || _owner.getLevel() > reward.getMaxLevel())
							continue;
						sp += reward.getSp();
					}
//                    long exp = (long) (Math.pow(_owner.getLevel(), 2) * _rod.getRewardModifier() * Config.RATE_XP_BY_LVL[_owner.getLevel()]);
                    long exp = (long) (Math.pow(_owner.getLevel(), 2) * _rod.getRewardModifier() * Config.RATE_FISH_EXP);
                    sp = (long) (sp * _rod.getRewardModifier() * Config.RATE_SP_BY_LVL[_owner.getLevel()]);
					_owner.addExpAndSp(exp, sp, 0, 0, false, false, false);
				}
				ItemFunctions.addItem(_owner, _fish.getId(), Config.RATE_FISH_DROP_COUNT * 1, true);
				_owner.broadcastPacket(new ExUserInfoFishing(_owner));
				_owner.broadcastPacket(new ExFishingEndPacket(_owner, 1));

				_owner.getListeners().onFishing(OptionalInt.of(_fish.getId()));
			}
			_processTask = ThreadPoolManager.getInstance().schedule(new ThrowHookTask(), TimeUnit.SECONDS.toMillis(_rod.getRefreshDelay()));
		}
	}

}
