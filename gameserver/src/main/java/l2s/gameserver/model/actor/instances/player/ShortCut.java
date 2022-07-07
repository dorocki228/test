package l2s.gameserver.model.actor.instances.player;

public class ShortCut
{
	public static final int TYPE_ITEM = 1;
	public static final int TYPE_SKILL = 2;
	public static final int TYPE_ACTION = 3;
	public static final int TYPE_MACRO = 4;
	public static final int TYPE_RECIPE = 5;
	public static final int TYPE_TPBOOKMARK = 6;
	public static final int PAGE_NORMAL_0 = 0;
	public static final int PAGE_NORMAL_1 = 1;
	public static final int PAGE_NORMAL_2 = 2;
	public static final int PAGE_NORMAL_3 = 3;
	public static final int PAGE_NORMAL_4 = 4;
	public static final int PAGE_NORMAL_5 = 5;
	public static final int PAGE_NORMAL_6 = 6;
	public static final int PAGE_NORMAL_7 = 7;
	public static final int PAGE_NORMAL_8 = 8;
	public static final int PAGE_NORMAL_9 = 9;
	public static final int PAGE_FLY_TRANSFORM = 10;
	public static final int PAGE_AIRSHIP = 11;
	public static final int PAGE_MAX = 11;
	private final int _slot;
	private final int _page;
	private final int _type;
	private final int _id;
	private final int _level;
	private final int _characterType;

	public ShortCut(int slot, int page, int type, int id, int level, int characterType)
	{
		_slot = slot;
		_page = page;
		_type = type;
		_id = id;
		_level = level;
		_characterType = characterType;
	}

	public int getSlot()
	{
		return _slot;
	}

	public int getPage()
	{
		return _page;
	}

	public int getType()
	{
		return _type;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _type != 2 ? 0 : _level;
	}

	public int getCharacterType()
	{
		return _characterType;
	}

	@Override
	public String toString()
	{
		return "ShortCut: " + _slot + "/" + _page + " ( " + _type + "," + _id + "," + _level + "," + _characterType + ")";
	}
}