package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.NSPacket;

public class NpcSayAction implements EventAction
{
	private final int _npcId;
	private final int _range;
	private final ChatType _chatType;
	private final NpcString _text;

	public NpcSayAction(int npcId, int range, ChatType type, NpcString string)
	{
		_npcId = npcId;
		_range = range;
		_chatType = type;
		_text = string;
	}

	@Override
	public void call(Event event)
	{
		NpcInstance npc = GameObjectsStorage.getByNpcId(_npcId);
		if(npc == null)
			return;
		for(Player player : World.getAroundObservers(npc))
			if(_range <= 0 || player.isInRangeZ(npc, _range))
				packet(npc, player);
	}

	private void packet(NpcInstance npc, Player player)
	{
		player.sendPacket(new NSPacket(npc, _chatType, _text));
	}
}
