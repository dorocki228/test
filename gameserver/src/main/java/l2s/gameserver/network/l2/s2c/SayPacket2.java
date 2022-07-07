package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SysString;
import l2s.gameserver.network.l2.components.SystemMsg;

public class SayPacket2 extends NpcStringContainer
{
	private static final int IS_FRIEND = 1;
	private static final int IS_CLAN_MEMBER = 2;
	private static final int IS_MENTEE_OR_MENTOR = 4;
	private static final int IS_ALLIANCE_MEMBER = 8;
	private static final int IS_GM = 16;
	private final ChatType _type;
	private SysString _sysString;
	private SystemMsg _systemMsg;
	private final int _objectId;
	private String _charName;
	private int _mask;
	private int _charLevel;

	public SayPacket2(int objectId, ChatType type, SysString st, SystemMsg sm)
	{
		super(NpcString.NONE);
		_charLevel = -1;
		_objectId = objectId;
		_type = type;
		_sysString = st;
		_systemMsg = sm;
	}

	public SayPacket2(int objectId, ChatType type, String charName, String text)
	{
		this(objectId, type, charName, NpcString.NONE, text);
	}

	public SayPacket2(int objectId, ChatType type, String charName, NpcString npcString, String... params)
	{
		super(npcString, params);
		_charLevel = -1;
		_objectId = objectId;
		_type = type;
		_charName = charName;
	}

	public void setCharName(String name)
	{
		_charName = name;
	}

	public void setSenderInfo(Player sender, Player receiver)
	{
		_charLevel = sender.getLevel();
		if(receiver.getFriendList().contains(sender.getObjectId()))
			_mask |= 0x1;
		if(receiver.getClanId() > 0 && receiver.getClanId() == sender.getClanId())
			_mask |= 0x2;
		if(receiver.getAllyId() > 0 && receiver.getAllyId() == sender.getAllyId())
			_mask |= 0x8;
		if(sender.isGM())
			_mask |= 0x10;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
        writeD(_type.ordinal());
		switch(_type)
		{
			case SYSTEM_MESSAGE:
			{
                writeD(_sysString.getId());
                writeD(_systemMsg.getId());
				break;
			}
			case TELL:
			{
				writeS(_charName);
				writeElements();
                writeC(_mask);
				if((_mask & 0x10) == 0x0)
				{
                    writeC(_charLevel);
					break;
				}
				break;
			}
			default:
			{
				writeS(_charName);
				writeElements();
				break;
			}
		}
	}

	/* TODO
	@Override
	public L2GameServerPacket packet(Player player)
	{
		Language lang = player.getLanguage();
		// если _itemLinkLang нулл(тоисть нету итем линков), или язык совпадает с языком игрока - возращаем this
		if(_itemLinkLang == null || _itemLinkLang == lang)
			return this;

		String text = null;
		Matcher m = Say2C.EX_ITEM_LINK_PATTERN.matcher(_parameters[0]);
		while(m.find())
		{
			int objectId = Integer.parseInt(m.group(1));
			ItemInfo itemInfo = ItemInfoCache.getInstance().get(objectId);
			if(itemInfo == null)
				return this;

			ItemNameLine line = ItemNameLineHolder.getInstance().get(lang, itemInfo.getItemId());
			if(line != null)
			{
				String replace = line.getName();
				if(itemInfo.getAugmentationMineralId() > 0)
					replace = line.getAugmentName();

				text = (text == null ? _parameters[0] : text).replace(m.group(2), replace);
			}
		}

		return new Say2(_objectId, _type, _charName, _npcString, text);
	}*/
}
