package l2s.gameserver.model;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public final class ArmorSet
{
	private final TIntHashSet _chests;
	private final TIntHashSet _legs;
	private final TIntHashSet _head;
	private final TIntHashSet _gloves;
	private final TIntHashSet _feet;
	private final TIntHashSet _shield;
	private final TIntObjectHashMap<List<SkillEntry>> _skills;
	private final List<SkillEntry> _shieldSkills;
	private final List<SkillEntry> _enchant6skills;
	private final List<SkillEntry> _enchant7skills;
	private final List<SkillEntry> _enchant8skills;

	public ArmorSet(String[] chests, String[] legs, String[] head, String[] gloves, String[] feet, String[] shield, String[] shield_skills, String[] enchant6skills, String[] enchant7skills, String[] enchant8skills)
	{
		_chests = new TIntHashSet();
		_legs = new TIntHashSet();
		_head = new TIntHashSet();
		_gloves = new TIntHashSet();
		_feet = new TIntHashSet();
		_shield = new TIntHashSet();
		_skills = new TIntObjectHashMap();
		_shieldSkills = new ArrayList<>();
		_enchant6skills = new ArrayList<>();
		_enchant7skills = new ArrayList<>();
		_enchant8skills = new ArrayList<>();
		_chests.addAll(parseItemIDs(chests));
		_legs.addAll(parseItemIDs(legs));
		_head.addAll(parseItemIDs(head));
		_gloves.addAll(parseItemIDs(gloves));
		_feet.addAll(parseItemIDs(feet));
		_shield.addAll(parseItemIDs(shield));
		if(shield_skills != null)
			for(String skill : shield_skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_shieldSkills.add(SkillHolder.getInstance().getSkillEntry(skillId, skillLvl));
				}
			}
		if(enchant6skills != null)
			for(String skill : enchant6skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant6skills.add(SkillHolder.getInstance().getSkillEntry(skillId, skillLvl));
				}
			}
		if(enchant7skills != null)
			for(String skill : enchant7skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant7skills.add(SkillHolder.getInstance().getSkillEntry(skillId, skillLvl));
				}
			}
		if(enchant8skills != null)
			for(String skill : enchant8skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant8skills.add(SkillHolder.getInstance().getSkillEntry(skillId, skillLvl));
				}
			}
	}

	private static int[] parseItemIDs(String[] items)
	{
		TIntHashSet result = new TIntHashSet();
		if(items != null)
			for(String s_id : items)
			{
				int id = Integer.parseInt(s_id);
				if(id > 0)
					result.add(id);
			}
		return result.toArray();
	}

	public void addSkills(int partsCount, String[] skills)
	{
		List<SkillEntry> skillList = new ArrayList<>();
		if(skills != null)
			for(String skill : skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					skillList.add(SkillHolder.getInstance().getSkillEntry(skillId, skillLvl));
				}
			}
		_skills.put(partsCount, skillList);
	}

	public boolean containAll(Player player)
	{
		Inventory inv = player.getInventory();
		ItemInstance chestItem = inv.getPaperdollItem(10);
		ItemInstance legsItem = inv.getPaperdollItem(11);
		ItemInstance headItem = inv.getPaperdollItem(6);
		ItemInstance glovesItem = inv.getPaperdollItem(9);
		ItemInstance feetItem = inv.getPaperdollItem(12);
		int chest = 0;
        if(chestItem != null)
			chest = chestItem.getItemId();
        int legs = 0;
        if(legsItem != null)
			legs = legsItem.getItemId();
        int head = 0;
        if(headItem != null)
			head = headItem.getItemId();
        int gloves = 0;
        if(glovesItem != null)
			gloves = glovesItem.getItemId();
        int feet = 0;
        if(feetItem != null)
			feet = feetItem.getItemId();
		return containAll(chest, legs, head, gloves, feet);
	}

	public boolean containAll(int chest, int legs, int head, int gloves, int feet)
	{
		return (_chests.isEmpty() || _chests.contains(chest)) && (_legs.isEmpty() || _legs.contains(legs)) && (_head.isEmpty() || _head.contains(head)) && (_gloves.isEmpty() || _gloves.contains(gloves)) && (_feet.isEmpty() || _feet.contains(feet));
	}

	public boolean containItem(int slot, int itemId)
	{
		switch(slot)
		{
			case 10:
			{
				return _chests.contains(itemId);
			}
			case 11:
			{
				return _legs.contains(itemId);
			}
			case 6:
			{
				return _head.contains(itemId);
			}
			case 9:
			{
				return _gloves.contains(itemId);
			}
			case 12:
			{
				return _feet.contains(itemId);
			}
			default:
			{
				return false;
			}
		}
	}

	public int getEquipedSetPartsCount(Player player)
	{
		Inventory inv = player.getInventory();
		ItemInstance chestItem = inv.getPaperdollItem(10);
		ItemInstance legsItem = inv.getPaperdollItem(11);
		ItemInstance headItem = inv.getPaperdollItem(6);
		ItemInstance glovesItem = inv.getPaperdollItem(9);
		ItemInstance feetItem = inv.getPaperdollItem(12);
		int chest = 0;
        if(chestItem != null)
			chest = chestItem.getItemId();
        int legs = 0;
        if(legsItem != null)
			legs = legsItem.getItemId();
        int head = 0;
        if(headItem != null)
			head = headItem.getItemId();
        int gloves = 0;
        if(glovesItem != null)
			gloves = glovesItem.getItemId();
        int feet = 0;
        if(feetItem != null)
			feet = feetItem.getItemId();
		int result = 0;
		if(!_chests.isEmpty() && _chests.contains(chest))
			++result;
		if(!_legs.isEmpty() && _legs.contains(legs))
			++result;
		if(!_head.isEmpty() && _head.contains(head))
			++result;
		if(!_gloves.isEmpty() && _gloves.contains(gloves))
			++result;
		if(!_feet.isEmpty() && _feet.contains(feet))
			++result;
		return result;
	}

	public List<SkillEntry> getSkills(int partsCount)
	{
		if(_skills.get(partsCount) == null)
			return new ArrayList<>();
		return _skills.get(partsCount);
	}

	public List<SkillEntry> getSkillsToRemove()
	{
		List<SkillEntry> result = new ArrayList<>();
		for(int i : _skills.keys())
		{
			List<SkillEntry> skills = _skills.get(i);
			if(skills != null)
				result.addAll(skills);
		}
		return result;
	}

	public List<SkillEntry> getShieldSkills()
	{
		return _shieldSkills;
	}

	public List<SkillEntry> getEnchant6skills()
	{
		return _enchant6skills;
	}

	public List<SkillEntry> getEnchant7skills()
	{
		return _enchant7skills;
	}

	public List<SkillEntry> getEnchant8skills()
	{
		return _enchant8skills;
	}

	public boolean containShield(Player player)
	{
		Inventory inv = player.getInventory();
		ItemInstance shieldItem = inv.getPaperdollItem(8);
		return shieldItem != null && _shield.contains(shieldItem.getItemId());
	}

	public boolean containShield(int shield_id)
	{
		return !_shield.isEmpty() && _shield.contains(shield_id);
	}

	public int getEnchantLevel(Player player)
	{
		if(!containAll(player))
			return 0;
		Inventory inv = player.getInventory();
		ItemInstance chestItem = inv.getPaperdollItem(10);
		ItemInstance legsItem = inv.getPaperdollItem(11);
		ItemInstance headItem = inv.getPaperdollItem(6);
		ItemInstance glovesItem = inv.getPaperdollItem(9);
		ItemInstance feetItem = inv.getPaperdollItem(12);
		int value = -1;
		if(!_chests.isEmpty())
			value = value > -1 ? Math.min(value, chestItem.getFixedEnchantLevel(player)) : chestItem.getFixedEnchantLevel(player);
		if(!_legs.isEmpty())
			value = value > -1 ? Math.min(value, legsItem.getFixedEnchantLevel(player)) : legsItem.getFixedEnchantLevel(player);
		if(!_gloves.isEmpty())
			value = value > -1 ? Math.min(value, glovesItem.getFixedEnchantLevel(player)) : glovesItem.getFixedEnchantLevel(player);
		if(!_head.isEmpty())
			value = value > -1 ? Math.min(value, headItem.getFixedEnchantLevel(player)) : headItem.getFixedEnchantLevel(player);
		if(!_feet.isEmpty())
			value = value > -1 ? Math.min(value, feetItem.getFixedEnchantLevel(player)) : feetItem.getFixedEnchantLevel(player);
		return value;
	}

	public int[] getChestIds()
	{
		return _chests.toArray();
	}

	public int[] getLegIds()
	{
		return _legs.toArray();
	}

	public int[] getHeadIds()
	{
		return _head.toArray();
	}

	public int[] getGlovesIds()
	{
		return _gloves.toArray();
	}

	public int[] getFeetIds()
	{
		return _feet.toArray();
	}

	public int[] getShieldIds()
	{
		return _shield.toArray();
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		ArmorSet armorSet = (ArmorSet) o;
		return Objects.equals(_chests, armorSet._chests) &&
				Objects.equals(_legs, armorSet._legs) &&
				Objects.equals(_head, armorSet._head) &&
				Objects.equals(_gloves, armorSet._gloves) &&
				Objects.equals(_feet, armorSet._feet) &&
				Objects.equals(_shield, armorSet._shield);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_chests, _legs, _head, _gloves, _feet, _shield);
	}
}
