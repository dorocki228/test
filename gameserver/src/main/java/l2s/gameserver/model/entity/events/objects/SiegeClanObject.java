package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.utils.TimeUtils;

import java.io.Serializable;
import java.util.Comparator;

public class SiegeClanObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String _type;
	private final Clan _clan;
	private NpcInstance _flag;
	private final long _date;
	private final long _param;

	public SiegeClanObject(String type, Clan clan, long param)
	{
		this(type, clan, param, System.currentTimeMillis());
	}

	public SiegeClanObject(String type, Clan clan, long param, long date)
	{
		_type = type;
		_clan = clan;
		_date = date;
		_param = param;
	}

	public int getObjectId()
	{
		return _clan.getClanId();
	}

	public Clan getClan()
	{
		return _clan;
	}

	public NpcInstance getFlag()
	{
		return _flag;
	}

	public void deleteFlag()
	{
		if(_flag != null)
		{
			_flag.deleteMe();
			_flag = null;
		}
	}

	public void setFlag(NpcInstance npc)
	{
		_flag = npc;
	}

	public void setType(String type)
	{
		_type = type;
	}

	public String getType()
	{
		return _type;
	}

	public void broadcast(IBroadcastPacket... packet)
	{
		getClan().broadcastToOnlineMembers(packet);
	}

	public void broadcast(L2GameServerPacket... packet)
	{
		getClan().broadcastToOnlineMembers(packet);
	}

	public void setEvent(boolean start, SiegeEvent<?, ?> event)
	{
		if(start)
			for(Player player : _clan.getOnlineMembers(0))
			{
				player.addEvent(event);
				player.broadcastCharInfo();
			}
		else
			for(Player player : _clan.getOnlineMembers(0))
			{
				player.removeEvent(event);
				player.getAbnormalList().stopEffects(5660);
				player.broadcastCharInfo();
			}
	}

	public boolean isParticle(Player player)
	{
		return true;
	}

	public long getParam()
	{
		return _param;
	}

	public long getDate()
	{
		return _date;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getClan().getName() + ", reg: " + TimeUtils.toSimpleFormat(getDate()) + ", param: " + getParam() + ", type: " + _type + "]";
	}

	public static class SiegeClanComparatorImpl implements Comparator<SiegeClanObject>
	{
		private static final SiegeClanComparatorImpl _instance = new SiegeClanComparatorImpl();

		public static SiegeClanComparatorImpl getInstance()
		{
			return _instance;
		}

		@Override
		public int compare(SiegeClanObject o1, SiegeClanObject o2)
		{
			return Long.compare(o2.getParam(), o1.getParam());
		}

	}
}
