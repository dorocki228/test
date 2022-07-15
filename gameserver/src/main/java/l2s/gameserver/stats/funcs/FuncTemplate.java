package l2s.gameserver.stats.funcs;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.conditions.Condition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class FuncTemplate
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static final FuncTemplate[] EMPTY_ARRAY = new FuncTemplate[0];

	public final Condition _applyCond;
	public final DoubleStat _stat;
	public final int _order;
	public final double _value;
	public final StatModifierType _modifierType;

	public Class<?> _func;
	public Constructor<?> _constructor;
	public Constructor<?> _constructorNew;

	public FuncTemplate(Condition applyCond, String func, DoubleStat stat, int order, double value)
	{
		_applyCond = applyCond;
		_stat = stat;
		_order = order;
		_value = value;
		_modifierType = null;
		_constructorNew = null;

		try
		{
			_func = Class.forName("l2s.gameserver.stats.funcs.Func" + func);
			_constructor = _func.getConstructor(DoubleStat.class, // stats to update
					Integer.TYPE, // order of execution
					Object.class, // owner
					Double.TYPE // value for function
			);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
	}

	public FuncTemplate(Condition applyCond, DoubleStat stat, double value, StatModifierType modifierType)
	{
		_applyCond = applyCond;
		_stat = stat;
		_order = modifierType == StatModifierType.DIFF ? 0x40 : 0x30;
		_value = value;
		_modifierType = modifierType;
		_constructor = null;

		try
		{
			_func = FuncNew.class;
			_constructorNew = _func.getConstructor(DoubleStat.class, // stats to update
					Integer.TYPE, // order of execution
					Object.class, // owner
					Double.TYPE, // value for function
					StatModifierType.class);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
	}

	public Func getFunc(Object owner)
	{
		try
		{
			if(_constructor != null)
			{
				Func f = (Func) _constructor.newInstance(_stat, _order, owner, _value);
				if(_applyCond != null)
					f.setCondition(_applyCond);
				return f;
			}

			Func f = (Func) _constructorNew.newInstance(_stat, _order, owner, _value, _modifierType);
			if(_applyCond != null)
				f.setCondition(_applyCond);
			return f;
		}
		catch(IllegalAccessException | InstantiationException | InvocationTargetException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
			return null;
		}
	}
}