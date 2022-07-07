package l2s.gameserver.templates.item;

import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.templates.StatsSet;

public final class ArmorTemplate extends ItemTemplate
{
	public ArmorTemplate(StatsSet set)
	{
		super(set);
		_type = set.getEnum("type", ArmorType.class, ArmorType.NONE);
		if((0x3E & _bodyPart) == _bodyPart)
			_type1 = 0;
		else if(_bodyPart == 65536 || _bodyPart == 262144 || _bodyPart == 524288)
			_type1 = 2;
		else
			_type1 = 1;
		if(_type == ArmorType.SIGIL)
			_exType = ExItemType.SIGIL;
		else if(_bodyPart == 64)
			_exType = ExItemType.HELMET;
		else if(_bodyPart == 1024)
			_exType = ExItemType.UPPER_PIECE;
		else if(_bodyPart == 2048)
			_exType = ExItemType.LOWER_PIECE;
		else if(_bodyPart == 32768 || _bodyPart == 131072)
			_exType = ExItemType.FULL_BODY;
		else if(_bodyPart == 512)
			_exType = ExItemType.GLOVES;
		else if(_bodyPart == 4096)
			_exType = ExItemType.FEET;
		else if((_bodyPart & 0x10) == 0x10 || (_bodyPart & 0x20) == 0x20)
			_exType = ExItemType.RING;
		else if((_bodyPart & 0x2) == 0x2 || (_bodyPart & 0x4) == 0x4)
			_exType = ExItemType.EARRING;
		else if(_bodyPart == 8)
			_exType = ExItemType.NECKLACE;
		else if(_bodyPart == 1048576 || _bodyPart == 2097152)
			_exType = ExItemType.BRACELET;
		else if(_bodyPart == 65536 || _bodyPart == 262144 || _bodyPart == 524288)
			_exType = ExItemType.HAIR_ACCESSORY;
		else if(_bodyPart == 1)
			_exType = ExItemType.UNDERWEAR;
		else if(_bodyPart == 8192)
			_exType = ExItemType.CLOAK;

		_type2 = _exType.mask();
		initEnchantFuncs();
	}

	@Override
	public IItemHandler getHandler()
	{
		return ItemHandler.EQUIPABLE_HANDLER;
	}

	@Override
	public ArmorType getItemType()
	{
		return (ArmorType) super.getItemType();
	}

	@Override
	public final long getItemMask()
	{
		return getItemType().mask();
	}

	public enum ArmorType implements ItemType
	{
		NONE("None"),
		LIGHT("Light"),
		HEAVY("Heavy"),
		MAGIC("Magic"),
		SIGIL("Sigil");

		public static final ArmorType[] VALUES;
		private final long _mask;
		private final String _name;

		ArmorType(String name)
		{
			_mask = 1L << ordinal();
			_name = name;
		}

		@Override
		public long mask()
		{
			return _mask;
		}

		@Override
		public IItemHandler getHandler()
		{
			return null;
		}

		@Override
		public ExItemType getExType()
		{
			return null;
		}

		@Override
		public String toString()
		{
			return _name;
		}

		static
		{
			VALUES = values();
		}
	}
}
