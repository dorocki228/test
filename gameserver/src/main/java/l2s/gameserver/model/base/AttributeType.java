package l2s.gameserver.model.base;

import l2s.gameserver.stats.DoubleStat;

import java.util.Arrays;

public enum AttributeType
{
	FIRE(0, DoubleStat.ATTACK_FIRE, DoubleStat.DEFENCE_FIRE, 1),
	WATER(1, DoubleStat.ATTACK_WATER, DoubleStat.DEFENCE_WATER, 2),
	WIND(2, DoubleStat.ATTACK_WIND, DoubleStat.DEFENCE_WIND, 4),
	EARTH(3, DoubleStat.ATTACK_EARTH, DoubleStat.DEFENCE_EARTH, 8),
	HOLY(4, DoubleStat.ATTACK_HOLY, DoubleStat.DEFENCE_HOLY, 16),
	UNHOLY(5, DoubleStat.ATTACK_UNHOLY, DoubleStat.DEFENCE_UNHOLY, 32),
	NONE(-1, null, DoubleStat.BASE_ELEMENTS_DEFENCE, 0),
	NONE_ARMOR(-2, null, DoubleStat.BASE_ELEMENTS_DEFENCE, 0);

	/** Массив элементов без NONE **/
	public final static AttributeType[] VALUES = Arrays.copyOf(values(), 6);

	private final int id;
	private final DoubleStat attack;
	private final DoubleStat defence;
	private final int mask;

	private AttributeType(int id, DoubleStat attack, DoubleStat defence, int mask)
	{
		this.id = id;
		this.attack = attack;
		this.defence = defence;
		this.mask = mask;
	}

	public int getId()
	{
		return id;
	}

	public DoubleStat getAttack()
	{
		return attack;
	}

	public DoubleStat getDefence()
	{
		return defence;
	}

	public int getMask()
	{
		return mask;
	}

	public static AttributeType getElementById(int id)
	{
		for(AttributeType e : VALUES)
			if(e.getId() == id)
				return e;
		return NONE;
	}

	public static AttributeType getElementByStat(DoubleStat stat)
	{
		for(AttributeType e : VALUES)
			if(e.getAttack() == stat || e.getDefence() == stat)
				return e;
		return NONE;
	}

	/**
	 * Возвращает противоположный тип элемента
	 * @return значение элемента
	 */
	public static AttributeType getReverseElement(AttributeType attributeType)
	{
		switch(attributeType)
		{
			case WATER:
				return FIRE;
			case FIRE:
				return WATER;
			case WIND:
				return EARTH;
			case EARTH:
				return WIND;
			case HOLY:
				return UNHOLY;
			case UNHOLY:
				return HOLY;
		}

		return NONE;
	}

	public static AttributeType getElementByName(String name)
	{
		for(AttributeType e : VALUES)
			if(e.name().equalsIgnoreCase(name))
				return e;
		return NONE;
	}

	public static AttributeType find(String name) {
		return AttributeType.valueOf(name.replace("attr_", "").toUpperCase());
	}
}
