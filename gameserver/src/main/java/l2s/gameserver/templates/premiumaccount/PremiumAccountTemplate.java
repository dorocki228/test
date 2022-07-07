package l2s.gameserver.templates.premiumaccount;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.Language;

import java.util.*;

public class PremiumAccountTemplate extends StatTemplate
{
	public static PremiumAccountRates DEFAULT_RATES = new PremiumAccountRates(1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0);
	public static PremiumAccountModifiers DEFAULT_MODIFIERS =
			new PremiumAccountModifiers(1.0, 1.0, 1.0);
	public static PremiumAccountBonus DEFAULT_BONUS = new PremiumAccountBonus(0.0);

	private final int _type;
	private final Map<Language, String> _names;
	private final List<ItemData> _giveItemsOnStart;
	private final List<ItemData> _takeItemsOnEnd;
	private final TIntObjectMap<List<ItemData>> _fees;
	private PremiumAccountRates _rates;
	private PremiumAccountModifiers _modifiers;
	private PremiumAccountBonus _bonus;
	private SkillEntry[] _skills;

	public PremiumAccountTemplate(int type)
	{
		_names = new HashMap<>();
		_giveItemsOnStart = new ArrayList<>();
		_takeItemsOnEnd = new ArrayList<>();
		_fees = new TIntObjectHashMap<>();
		_rates = DEFAULT_RATES;
		_modifiers = DEFAULT_MODIFIERS;
		_bonus = DEFAULT_BONUS;
		_skills = SkillEntry.EMPTY_ARRAY;
		_type = type;
	}

	public int getType()
	{
		return _type;
	}

	public void addName(Language lang, String name)
	{
		_names.put(lang, name);
	}

	public String getName(Language lang)
	{
		String name = _names.get(lang);
		if(name == null)
			if(lang == Language.ENGLISH)
				name = _names.get(Language.RUSSIAN);
			else
				name = _names.get(Language.ENGLISH);
		return name;
	}

	public void setRates(PremiumAccountRates rates)
	{
		_rates = rates;
	}

	public PremiumAccountRates getRates()
	{
		return _rates;
	}

	public void setModifiers(PremiumAccountModifiers modifiers)
	{
		_modifiers = modifiers;
	}

	public PremiumAccountModifiers getModifiers()
	{
		return _modifiers;
	}

	public void setBonus(PremiumAccountBonus bonus)
	{
		_bonus = bonus;
	}

	public PremiumAccountBonus getBonus()
	{
		return _bonus;
	}

	public void addGiveItemOnStart(ItemData item)
	{
		_giveItemsOnStart.add(item);
	}

	public ItemData[] getGiveItemsOnStart()
	{
		return _giveItemsOnStart.toArray(new ItemData[0]);
	}

	public void addTakeItemOnEnd(ItemData item)
	{
		_takeItemsOnEnd.add(item);
	}

	public ItemData[] getTakeItemsOnEnd()
	{
		return _takeItemsOnEnd.toArray(new ItemData[0]);
	}

	public void addFee(int delay, ItemData item)
	{
		List<ItemData> items = _fees.get(delay);
		if(items == null)
		{
			items = new ArrayList<>();
			_fees.put(delay, items);
		}
		items.add(item);
	}

	public int[] getFeeDelays()
	{
		return _fees.keys();
	}

	public ItemData[] getFeeItems(int delay)
	{
		List<ItemData> items = _fees.get(delay);
		if(items == null)
			return null;
		return items.toArray(new ItemData[0]);
	}

	public void attachSkill(SkillEntry skill)
	{
		_skills = (SkillEntry[]) ArrayUtils.add((Object[]) _skills, skill);
	}

	public SkillEntry[] getAttachedSkills()
	{
		return _skills;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(!(o instanceof PremiumAccountTemplate))
			return false;
		PremiumAccountTemplate that = (PremiumAccountTemplate) o;
		return _type == that._type;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_type);
	}
}
