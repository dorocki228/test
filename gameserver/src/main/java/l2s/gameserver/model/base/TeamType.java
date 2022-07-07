package l2s.gameserver.model.base;

import java.util.Arrays;

public enum TeamType
{
	NONE
			{
				@Override
				public String toString()
				{
					return "None";
				}
			},
	BLUE
			{
				@Override
				public String toString()
				{
					return "Blue";
				}
			},
	RED
			{
				@Override
				public String toString()
				{
					return "Red";
				}
			};

	public static TeamType[] VALUES;

	public int ordinalWithoutNone()
	{
		return ordinal() - 1;
	}

	public TeamType revert()
	{
		return this == BLUE ? RED : this == RED ? BLUE : NONE;
	}

	static
	{
		VALUES = Arrays.copyOfRange(values(), 1, 3);
	}
}
