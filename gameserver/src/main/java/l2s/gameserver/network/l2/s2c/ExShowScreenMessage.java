package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.components.NpcString;

public class ExShowScreenMessage extends NpcStringContainer
{
	public static enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT,
	}

	public static final int SYSMSG_TYPE = 0;
	public static final int STRING_TYPE = 1;

	private int _type, _sysMessageId;
	private boolean _big_font, _effect;
	private ScreenMessageAlign _text_align;
	private int _time;
	private int _unk;

	//@Deprecated
	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		this(text, time, text_align, big_font, 1, -1, false);
	}

	//@Deprecated
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
		this(t, time, text_align, true, STRING_TYPE, -1, false, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, String... params)
	{
		this(npcString, time, text_align, big_font, STRING_TYPE, -1, false, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, boolean showEffect, String... params)
	{
		this(npcString, time, text_align, big_font, STRING_TYPE, -1, showEffect, 0, params);
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
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_SCREEN_MESSAGE.writeId(packetWriter);
		packetWriter.writeD(_type); // 0 - system messages, 1 - your defined text
		packetWriter.writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		packetWriter.writeD(_text_align.ordinal() + 1); // размещение текста
		packetWriter.writeD(0x00); // ?
		packetWriter.writeD(_big_font ? 0 : 1); // размер текста
		packetWriter.writeD(0x00); // ?
		packetWriter.writeD(_unk); // ?
		packetWriter.writeD(_effect ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		packetWriter.writeD(_time); // время отображения сообщения в милисекундах
		packetWriter.writeD(0x01); // Угасание
		writeElements(packetWriter);

		return true;
	}
}