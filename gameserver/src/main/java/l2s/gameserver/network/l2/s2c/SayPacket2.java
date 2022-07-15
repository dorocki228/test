package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SysString;
import l2s.gameserver.network.l2.components.SystemMsg;

public class SayPacket2 extends NpcStringContainer
{
	// Flags
	private static final int IS_FRIEND = 1 << 0;
	private static final int IS_CLAN_MEMBER = 1 << 1;
	private static final int IS_MENTEE_OR_MENTOR = 1 << 2;
	private static final int IS_ALLIANCE_MEMBER = 1 << 3;
	private static final int IS_GM = 1 << 4;

	private ChatType _type;
	private SysString _sysString;
	private SystemMsg _systemMsg;

	private int _objectId;
	private String _charName;
	private int _mask;
	private int _charLevel = -1;
	private String _text;

	public SayPacket2(int objectId, ChatType type, SysString st, SystemMsg sm)
	{
		super(NpcString.NONE);
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
		_objectId = objectId;
		_type = type;
		_charName = charName;
		_text = params.length > 0 ? params[0] : null;
	}

	public void setCharName(String name)
	{
		_charName = name;
	}

	public void setSenderInfo(Player sender, Player receiver)
	{
		_charLevel = sender.getLevel();

		if(receiver.getFriendList().contains(sender.getObjectId()))
			_mask |= IS_FRIEND;

		if(receiver.getClanId() > 0 && receiver.getClanId() == sender.getClanId())
			_mask |= IS_CLAN_MEMBER;

		if(receiver.getAllyId() > 0 && receiver.getAllyId() == sender.getAllyId())
			_mask |= IS_ALLIANCE_MEMBER;

		// Does not shows level
		if(sender.isGM())
			_mask |= IS_GM;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SAY2.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_type.ordinal());
		switch(_type)
		{
			case SYSTEM_MESSAGE:
				packetWriter.writeD(_sysString.getId());
				packetWriter.writeD(_systemMsg.getId());
				break;
			case TELL:
				packetWriter.writeS(_charName);
				writeElements(packetWriter);
				packetWriter.writeC(_mask);
				if((_mask & IS_GM) == 0)
					packetWriter.writeC(_charLevel);
				break;
			case CLAN:
			case ALLIANCE:
				packetWriter.writeS(_charName);
				writeElements(packetWriter);
				packetWriter.writeC(0x00);	// TODO[UNDERGROUND]: UNK
				break;
			default:
				packetWriter.writeS(_charName);
				writeElements(packetWriter);
				break;
		}

		return true;
	}

	@Override
	public final void runImpl(Player player)
	{
		if (_text == null) {
			return;
		}

		if (player == null) {
			return;
		}

		player.getListeners().onChatMessageReceive(_type, _charName, _text);
	}
}