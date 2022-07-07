package l2s.gameserver.templates;

import l2s.gameserver.model.base.BaseStats;

public final class BaseStatsBonus
{
	private final double _int;
	private final double _str;
	private final double _con;
	private final double _men;
	private final double _dex;
	private final double _wit;

	public BaseStatsBonus(double _int, double str, double con, double men, double dex, double wit)
	{
		this._int = _int;
		_str = str;
		_con = con;
		_men = men;
		_dex = dex;
		_wit = wit;
	}

	public double getINT()
	{
		return _int;
	}

	public double getSTR()
	{
		return _str;
	}

	public double getCON()
	{
		return _con;
	}

	public double getMEN()
	{
		return _men;
	}

	public double getDEX()
	{
		return _dex;
	}

	public double getWIT()
	{
		return _wit;
	}

	public double get(BaseStats stat)
	{
		switch(stat)
		{
			case STR:
			{
				return _str;
			}
			case DEX:
			{
				return _dex;
			}
			case CON:
			{
				return _con;
			}
			case INT:
			{
				return _int;
			}
			case WIT:
			{
				return _wit;
			}
			case MEN:
			{
				return _men;
			}
			default:
			{
				return 1.0;
			}
		}
	}
}
