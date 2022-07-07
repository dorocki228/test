package gve.zones.model;

import com.cronutils.utils.StringUtils;
import gve.zones.GveZoneManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.actor.player.OnLevelChangeListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.utils.Location;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class GveZone
{
	private final Zone zone;

	// restrictions
	private int maxLevel = Experience.getMaxLevel();
	private ItemGrade maxGrade = ItemGrade.R99;
	private int enchantLevel = Integer.MAX_VALUE;
	private boolean restrictHeroItems;
	private List<Integer> restrictedItems = Collections.emptyList();

	private GveZone.LevelChangeListener levelChangeListener;

	public GveZone(Zone zone)
	{
		this.zone = zone;
		zone.addListener(GveZoneManager.zoneListener);
	}

	public String getName()
	{
		return zone.getName();
	}

	public String getInGameName()
	{
		return zone.getInGameName();
	}

	public Zone getZone()
	{
		return zone;
	}

	public GveZoneStatus getStatus()
	{
		String status = ServerVariables.getString("gve_zone_status_" + getName(), StringUtils.EMPTY);
		return status.isEmpty() ? GveZoneStatus.DISABLED : GveZoneStatus.valueOf(status);
	}

	public void setStatus(GveZoneStatus status)
	{
		ServerVariables.set("gve_zone_status_" + getName(), status.name());
	}

	public List<GveOutpost> getOutposts()
	{
		return zone.getOutposts();
	}

	public List<GveOutpost> getOutposts(Fraction fraction)
	{
		return getOutposts().stream().filter(outpost -> outpost.getFraction() == fraction).collect(Collectors.toList());
	}

	public List<GveOutpost> getOutposts(Fraction fraction, int status)
	{
		return getOutposts().stream().filter(outpost -> outpost.getFraction() == fraction).filter(outpost -> outpost.getStatus() == status).collect(Collectors.toList());
	}

	public GveOutpost getRandomOutpost(Fraction fraction)
	{
		return Rnd.get(getOutposts(fraction));
	}

	public ZoneType getType()
	{
		return zone.getType();
	}

	public void onChangeStatus()
	{
		GveZoneStatus status = getStatus();

		var spawns = getOutposts().stream().map(GveOutpost::name).collect(Collectors.toList());
		spawns.add(getName());

		spawns.forEach(spawn -> {
			if(status == GveZoneStatus.ACTIVATED)
				SpawnManager.getInstance().spawn(spawn);
			else
				SpawnManager.getInstance().despawn(spawn);
		});
	}

	public boolean canEnterZone(Player player)
	{
		if(player.getLevel() > maxLevel)
		{
			player.sendMessage(new CustomMessage("gve.zones.conditions.level").addString(getType().toString().toLowerCase()).addNumber(maxLevel));
			return false;
		}

		for(ItemInstance item : player.getInventory().getPaperdollItems())
		{
			if(item == null)
			{
				continue;
			}

			IBroadcastPacket packet = checkItem(player, item);
			if(packet != null)
			{
				player.sendPacket(packet);
				return false;
			}
		}

		return true;
	}

	public boolean canEquipItem(Player player, ItemInstance item)
	{
		if(checkItem(player, item) != null)
		{ return false; }

		return true;
	}

	protected IBroadcastPacket checkItem(Player player, ItemInstance item)
	{
		if(item.isEquipable() && item.getGrade().ordinal() > maxGrade.ordinal())
		{
			return new CustomMessage("gve.zones.conditions.item.grade").addString(getType().toString().toLowerCase()).addString(item.getGrade().toString()).addString(maxGrade.toString());
		}

		if(item.getEnchantLevel() > enchantLevel)
		{ return new CustomMessage("gve.zones.conditions.item.enchant").addString(getType().toString().toLowerCase()).addNumber(enchantLevel); }

		if(restrictHeroItems && item.isHeroItem())
		{ return new CustomMessage("gve.zones.conditions.item.heroic").addString(getType().toString().toLowerCase()).addString(item.getName(player)); }

		if(restrictedItems.contains(item.getItemId()))
		{
			return new CustomMessage("gve.zones.conditions.item.restricted").addString(getType().toString().toLowerCase()).addString(item.getName(player));
		}

		return null;
	}

	public void onZoneEnter(Creature actor)
	{
		if(levelChangeListener != null)
		{
			actor.addListener(levelChangeListener);
		}
	}

	public void onZoneLeave(Creature actor)
	{
		if(levelChangeListener != null)
		{
			actor.removeListener(levelChangeListener);
		}
	}

	public Location getRandomRespawnLoc(Player player)
	{
		var outpost = getRandomOutpost(player.getFraction());
		return outpost.getMain();
	}

	public Location getClosestRespawnLoc(Player player)
	{
		var outposts = getOutposts(player.getFraction());
		var playerLoc = player.getLoc();

		GveOutpost tempOutpost = null;
		var tempDistance = Double.MAX_VALUE;
		for(GveOutpost outpost : outposts)
		{
			if(outpost.getStatus() == GveOutpost.DEAD)
				continue;

			if(outpost.getMain() == null)
				continue;

			var distance = outpost.getMain().distance(playerLoc);
			if(distance < tempDistance)
			{
				tempOutpost = outpost;
				tempDistance = distance;
			}
		}

		return tempOutpost != null ? tempOutpost.getMain() : null;
	}

	public void setMaxLevel(int maxLevel)
	{
		this.maxLevel = maxLevel;
		levelChangeListener = new LevelChangeListener(getType(), maxLevel);
	}

	public ItemGrade getMaxGrade()
	{
		return maxGrade;
	}
	
	public void setMaxGrade(ItemGrade maxGrade)
	{
		this.maxGrade = maxGrade;
	}

	public void setEnchantLevel(int enchantLevel)
	{
		this.enchantLevel = enchantLevel;
	}

	public void setRestrictHeroItems(boolean restrictHeroItems)
	{
		this.restrictHeroItems = restrictHeroItems;
	}

	public void setRestrictedItems(List<Integer> restrictedItems)
	{
		this.restrictedItems = restrictedItems;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(!(o instanceof GveZone))
			return false;
		GveZone gveZone = (GveZone) o;
		return zone.equals(gveZone.zone);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(zone);
	}

	@Override
	public String toString()
	{
		return "GveZone{" + "zone=" + getName() + '}';
	}

	static class LevelChangeListener implements OnLevelChangeListener
	{
		private final ZoneType type;
		private final int maxLevel;

		LevelChangeListener(ZoneType type, int maxLevel)
		{
			this.type = type;
			this.maxLevel = maxLevel;
		}

		@Override
		public void onLevelChange(Player player, int oldLvl, int newLvl)
		{
			if(player.getLevel() > maxLevel)
			{
				player.sendMessage(new CustomMessage("gve.zones.conditions.level").addString(type.toString().toLowerCase()).addNumber(maxLevel));
				player.teleToClosestTown();
			}
		}
	}
}
