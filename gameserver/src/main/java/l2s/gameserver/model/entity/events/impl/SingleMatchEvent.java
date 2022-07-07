package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.listener.actor.OnDeathFromUndyingListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.network.l2.components.IBroadcastPacket;

public abstract class SingleMatchEvent extends Event
{
	protected SingleMatchEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	protected SingleMatchEvent(int id, String name)
	{
		super(id, name);
	}

	public boolean checkPvPFlag(Creature target)
	{
		return !target.containsEvent(this);
	}

	public void onStatusUpdate(Player player)
	{}

	public void onEffectIconsUpdate(Player player, Abnormal[] effects)
	{}

	public void onDie(Player actor, Creature killer)
	{}

	public void sendPacket(IBroadcastPacket packet)
	{}

	public void sendPackets(IBroadcastPacket... packet)
	{}

	public class OnDeathFromUndyingListenerImpl implements OnDeathFromUndyingListener
	{
		@Override
		public void onDeathFromUndying(Creature actor, Creature killer)
		{
			onDie(actor.getPlayer(), killer);
		}
	}
}
