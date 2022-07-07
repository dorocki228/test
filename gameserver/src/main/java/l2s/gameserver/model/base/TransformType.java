package l2s.gameserver.model.base;

public enum TransformType
{
	COMBAT(true),
	NON_COMBAT(false),
	MODE_CHANGE(true),
	RIDING_MODE(false),
	FLYING(true),
	PURE_STAT(true),
	CURSED(true);

	public static final TransformType[] VALUES;
	private final boolean _canAttack;

	TransformType(boolean canAttack)
	{
		_canAttack = canAttack;
	}

	public boolean isCanAttack()
	{
		return _canAttack;
	}

	static
	{
		VALUES = values();
	}
}
