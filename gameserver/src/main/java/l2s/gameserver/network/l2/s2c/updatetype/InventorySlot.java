package l2s.gameserver.network.l2.s2c.updatetype;

public enum InventorySlot implements IUpdateTypeComponent
{
	PENDANT(0),
	REAR(1),
	LEAR(2),
	NECK(3),
	RFINGER(4),
	LFINGER(5),
	HEAD(6),
	RHAND(7),
	LHAND(8),
	GLOVES(9),
	CHEST(10),
	LEGS(11),
	FEET(12),
	CLOAK(13),
	LRHAND(14),
	HAIR(15),
	HAIR2(16),
	RBRACELET(17),
	LBRACELET(18),
	DECO1(19),
	DECO2(20),
	DECO3(21),
	DECO4(22),
	DECO5(23),
	DECO6(24),
	BELT(25),
	BROOCH(26),
	BROOCH_JEWEL(27),
	BROOCH_JEWEL2(28),
	BROOCH_JEWEL3(29),
	BROOCH_JEWEL4(30),
	BROOCH_JEWEL5(31),
	BROOCH_JEWEL6(32);

	public static final InventorySlot[] VALUES;
	private final int _paperdollSlot;

	public static InventorySlot valueOf(int slot)
	{
		for(InventorySlot s : VALUES)
			if(s.getSlot() == slot)
				return s;
		return null;
	}

	InventorySlot(int paperdollSlot)
	{
		_paperdollSlot = paperdollSlot;
	}

	public int getSlot()
	{
		return _paperdollSlot;
	}

	@Override
	public int getMask()
	{
		return ordinal();
	}

	static
	{
		VALUES = values();
	}
}
