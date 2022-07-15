package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.ShortCutList.AutoShortCutType;
import l2s.gameserver.model.items.ItemInstance;

public class ShortCut
{
	public enum ShortCutType {
		/*0*/NONE,
		/*1*/ITEM,
		/*2*/SKILL,
		/*3*/ACTION,
		/*4*/MACRO,
		/*5*/RECIPE,
		/*6*/TPBOOKMARK;

		public static final ShortCutType[] VALUES = values();
	}

	// номера панельек для шарткатов
	public final static int PAGE_NORMAL_0 = 0;
	public final static int PAGE_NORMAL_1 = 1;
	public final static int PAGE_NORMAL_2 = 2;
	public final static int PAGE_NORMAL_3 = 3;
	public final static int PAGE_NORMAL_4 = 4;
	public final static int PAGE_NORMAL_5 = 5;
	public final static int PAGE_NORMAL_6 = 6;
	public final static int PAGE_NORMAL_7 = 7;
	public final static int PAGE_NORMAL_8 = 8;
	public final static int PAGE_NORMAL_9 = 9;
	public final static int PAGE_NORMAL_10 = 10;
	public final static int PAGE_NORMAL_11 = 11;
	public final static int PAGE_NORMAL_12 = 12;
	public final static int PAGE_NORMAL_13 = 13;
	public final static int PAGE_NORMAL_14 = 14;
	public final static int PAGE_NORMAL_15 = 15;
	public final static int PAGE_NORMAL_16 = 16;
	public final static int PAGE_NORMAL_17 = 17;
	public final static int PAGE_NORMAL_18 = 18;
	public final static int PAGE_NORMAL_19 = 19;
	public final static int PAGE_FLY_TRANSFORM = 20;
	public final static int PAGE_AIRSHIP = 21;
	public final static int PAGE_AUTO_USABLE_ITEMS = 22;
	public final static int PAGE_AUTO_USABLE_MACRO = 23;

	public final static int PAGE_MAX = PAGE_AUTO_USABLE_MACRO;

	private final int _slot;
	private final int _page;
	private final ShortCutType _type;
	private final int _id;
	private final int _level;
	private final int _characterType;

	private final AutoShortCutType autoShortCutType;
	private boolean autoUseEnabled;

	public ShortCut(Player player, int slot, int page, ShortCutType type, int id, int level, int characterType) {
		_slot = slot;
		_page = page;
		_type = type;
		_id = id;
		_level = level;
		_characterType = characterType;

		if (type == ShortCut.ShortCutType.SKILL) {
			final Skill skill = SkillHolder.getInstance().getSkill(id, level);
			if (skill == null) {
				throw new IllegalArgumentException("Can't find skill " + id);
			}

			this.autoShortCutType = skill.isAutoUsable() ? AutoShortCutType.SKILLS : AutoShortCutType.NONE;
		} else if (type == ShortCutType.ITEM && player != null) {
			ItemInstance item = player.getInventory().getItemByObjectId(id);
			if (item == null) {
				throw new IllegalArgumentException("Can't find item " + id);
			}

			this.autoShortCutType = item.getTemplate().isAutousable() ? AutoShortCutType.ITEMS : AutoShortCutType.NONE;
		} else {
			autoShortCutType = AutoShortCutType.NONE;
		}
	}

	public int getSlot()
	{
		return _slot;
	}

	public int getPage()
	{
		return _page;
	}

	public int getIndex() {
		return _slot + _page * 12;
	}

	public ShortCutType getType()
	{
		return _type;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _type != ShortCutType.SKILL ? 0 : _level;
	}

	public int getCharacterType()
	{
		return _characterType;
	}

	public AutoShortCutType getAutoShortCutType() {
		return autoShortCutType;
	}

	public boolean isAutoUseEnabled() {
		return autoUseEnabled;
	}

	public void setAutoUseEnabled(boolean autoUseEnabled) {
		this.autoUseEnabled = autoUseEnabled;
	}

	@Override
	public String toString()
	{
		return "ShortCut: " + _slot + "/" + _page + " ( " + _type + "," + _id + "," + _level + "," + _characterType + ")"+autoUseEnabled+"/"+autoShortCutType;
	}
}