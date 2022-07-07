package l2s.gameserver.model;

import l2s.gameserver.network.l2.components.IBroadcastPacket;

import java.util.Collections;
import java.util.Iterator;

public interface PlayerGroup extends Iterable<Player>
{
	PlayerGroup EMPTY = new PlayerGroup(){
		@Override
		public void broadCast(IBroadcastPacket... packet)
		{}

		@Override
		public int getMemberCount()
		{
			return 0;
		}

		@Override
		public Player getGroupLeader()
		{
			return null;
		}

		@Override
		public Iterator<Player> iterator()
		{
			return Collections.emptyIterator();
		}
	};

	void broadCast(IBroadcastPacket... p0);

	int getMemberCount();

	Player getGroupLeader();
}
