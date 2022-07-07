package l2s.gameserver.stats.funcs;

import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class FuncTemplate
{
	private static final Logger _log;
	public static final FuncTemplate[] EMPTY_ARRAY;
	public Condition _applyCond;
	public Class<?> _func;
	public Constructor<?> _constructor;
	public Stats _stat;
	public int _order;
	public double _value;

	public FuncTemplate(Condition applyCond, String func, Stats stat, int order, double value)
	{
		_applyCond = applyCond;
		_stat = stat;
		_order = order;
		_value = value;
		try
		{
			_func = Class.forName("l2s.gameserver.stats.funcs.Func" + func);
			_constructor = _func.getConstructor(Stats.class, Integer.TYPE, Object.class, Double.TYPE);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public Func getFunc(Object owner)
	{
		try
		{
			Func f = (Func) _constructor.newInstance(_stat, _order, owner, _value);
			if(_applyCond != null)
				f.setCondition(_applyCond);
			return f;
		}
		catch(IllegalAccessException e)
		{
			_log.error("", e);
			return null;
		}
		catch(InstantiationException e2)
		{
			_log.error("", e2);
			return null;
		}
		catch(InvocationTargetException e3)
		{
			_log.error("", e3);
			return null;
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(FuncTemplate.class);
		EMPTY_ARRAY = new FuncTemplate[0];
	}
}
