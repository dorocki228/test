package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.components.NpcString;

public class ExShowScreenMessage extends NpcStringContainer
{
	public static final int SYSMSG_TYPE = 0;
	public static final int STRING_TYPE = 1;
	private final int _type;
	private final int _sysMessageId;
	private final boolean _big_font;
	private final boolean _effect;
	private final ScreenMessageAlign _text_align;
	private final int _time;
	private int _unk;

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		this(text, time, text_align, big_font, 1, -1, false);
	}

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect)
	{
		super(NpcString.NONE, text);
		_type = type;
		_sysMessageId = messageId;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}

	public ExShowScreenMessage(NpcString t, int time, ScreenMessageAlign text_align, String... params)
	{
		this(t, time, text_align, true, 1, -1, false, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, String... params)
	{
		this(npcString, time, text_align, big_font, 1, -1, false, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, boolean showEffect, String... params)
	{
		this(npcString, time, text_align, big_font, 1, -1, showEffect, 0, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, int type, int systemMsg, boolean showEffect, String... params)
	{
		this(npcString, time, text_align, big_font, type, systemMsg, showEffect, 0, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, int type, int systemMsg, boolean showEffect, int unk, String... params)
	{
		super(npcString, params);
		_type = type;
		_sysMessageId = systemMsg;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
		_unk = unk;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_type);
        writeD(_sysMessageId);
        writeD(_text_align.ordinal() + 1);
        writeD(0);
        writeD(_big_font ? 0 : 1);
        writeD(0);
        writeD(_unk);
        writeD(_effect ? 1 : 0);
        writeD(_time);
        writeD(1);
		writeElements();
	}

	public enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT
    }
}
