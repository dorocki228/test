package l2s.gameserver.stats;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncOwner;

import java.util.Arrays;

public final class Calculator
{
	private Func[] _functions;

	private double _base;
	private double _last;

	public final Stats _stat;
	public final Creature _character;

	public Calculator(Stats stat, Creature character)
	{
		_stat = stat;
		_character = character;
		_functions = Func.EMPTY_FUNC_ARRAY;
	}

	/**
	 * Return the number of Funcs in the Calculator.<BR><BR>
	 */
	public int size()
	{
		return _functions.length;
	}

	/**
	 * Add a Func to the Calculator.<BR><BR>
	 */
	public void addFunc(Func f)
	{
		_functions = ArrayUtils.add(_functions, f);
		Arrays.sort(_functions);
	}

	/**
	 * Remove a Func from the Calculator.<BR><BR>
	 */
	public void removeFunc(Func f)
	{
		_functions = ArrayUtils.remove(_functions, f);
		if(_functions.length == 0)
			_functions = Func.EMPTY_FUNC_ARRAY;
		else
			Arrays.sort(_functions);
	}

	/**
	 * Remove each Func with the specified owner of the Calculator.<BR><BR>
	 */
	public boolean removeOwner(Object owner)
	{
        boolean result = false;

		Func[] tmp = _functions;
		for(Func element : tmp)
			if(element.owner == owner) {
                removeFunc(element);
                result = true;
            }

        return result;
	}

	/**
	 * Run each Func of the Calculator.<BR><BR>
     * @return
     */
	public double calc(Creature creature, Creature target, Skill skill, double value)
	{
		Func[] funcs = _functions;
		_base = value;

		boolean overrideLimits = false;
		for(Func func : funcs)
		{
			if(func == null)
				continue;

			if(func.owner instanceof FuncOwner)
			{
				if(!((FuncOwner) func.owner).isFuncEnabled())
					continue;
				if(((FuncOwner) func.owner).overrideLimits())
					overrideLimits = true;
			}

			if(func.getCondition() == null || func.getCondition().test(creature, target, skill, null, value))
                value = func.calc(creature, target, skill, value);
		}

		if(!overrideLimits)
			value = _stat.validate(value);

		if (value != _last)
		{
			//double last = _last; //TODO [G1ta0] найти приминение в StatsChangeRecorder
			_last = value;
		}

        return value;
    }

	/**
	 * Для отладки
	 */
	public Func[] getFunctions()
	{
		return _functions;
	}

	public double getBase()
	{
		return _base;
	}

	public double getLast()
	{
		return _last;
	}
}
