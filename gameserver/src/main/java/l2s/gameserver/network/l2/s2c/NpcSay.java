package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;

public class NpcSay extends NpcStringContainer
{
	private int _objId;
	private int _type;
	private int _id;

	public NpcSay(NpcInstance npc, ChatType chatType, String text)
	{
		this(npc, chatType, NpcString.NONE, text);
	}

	public NpcSay(NpcInstance npc, ChatType chatType, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objId = npc.getObjectId();
		_id = npc.getTemplate().displayId > 0 ? npc.getTemplate().displayId : npc.getNpcId();
		_type = chatType.ordinal();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.NPC_SAY.writeId(packetWriter);
		packetWriter.writeD(_objId);
		packetWriter.writeD(_type);
		packetWriter.writeD(1000000 + _id);
		writeElements(packetWriter);

		return true;
	}
}