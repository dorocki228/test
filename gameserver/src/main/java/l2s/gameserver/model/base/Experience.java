package l2s.gameserver.model.base;

import l2s.gameserver.Config;

public class Experience
{
	public static final long[] LEVEL = {
			-1L,
            0L,
            68L,
			364L,
			1169L,
			2885L,
			6039L,
			11288L,
			19424L,
			31379L,
			48230L,
			71203L,
			101678L,
			141194L,
			191455L,
			254331L,
			331868L,
			426289L,
			540001L,
			675597L,
			835864L,
			1023785L,
			1439215L,
			1948497L,
			2568850L,
			3320625L,
			4227172L,
			5315161L,
			6614929L,
			8161929L,
			9995812L,
			12162655L,
			14713777L,
			17708475L,
			21213445L,
			25304463L,
			30067485L,
			35599858L,
			42010312L,
			49421366L,
			57972427L,
			67818553L,
			79135431L,
			92117896L,
			106985763L,
			123986756L,
			143394645L,
			165516618L,
			190696911L,
			219317613L,
			251805374L,
			288635909L,
			330338848L,
			377507026L,
			430790086L,
			490916803L,
			558693890L,
			635018116L,
			720879370L,
			817380319L,
			925741335L,
			1047311009L,
			1183577349L,
			1336187067L,
			1506967658L,
			1697936136L,
			1911306680L,
			2149533465L,
			2415323168L,
			2711646440L,
			3041801165L,
			3409398455L,
			3818421441L,
			4273257148L,
			4778730308L,
			5340152664L,
			5901575020L,
			6563335189L,
			7138805250L, // 78
			9372198366L, // 79
			16072377713L, // 80
			150075964661L
	};

	public static double penaltyModifier(long count)
	{
		return Math.max(1.0 - count * Config.DEEPBLUE_DROP_PERCENT_EACH_LVL / 100.0, 0.0);
	}

	public static int getMaxLevel()
	{
		return Config.ALT_MAX_LEVEL;
	}

	public static int getMaxSubLevel()
	{
		return Config.ALT_MAX_SUB_LEVEL;
	}

	public static int getLevel(long thisExp)
	{
		int level = 0;
		for(int i = 0; i < LEVEL.length; ++i)
		{
			long exp = LEVEL[i];
			if(thisExp >= exp)
				level = i;
		}
		return level;
	}

	public static long getExpForLevel(int lvl)
	{
		if(lvl >= LEVEL.length)
			return 0L;
		return LEVEL[lvl];
	}

	public static double getExpPercent(int level, long exp)
	{
		return (exp - getExpForLevel(level)) / ((getExpForLevel(level + 1) - getExpForLevel(level)) / 100.0) * 0.01;
	}
}
