package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.data.xml.holder.StaticObjectHolder;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.StaticObjectInstance;

/**
 * @author VISTALL
 * @date 22:50/09.03.2011
 */
public class StaticObjectObject implements SpawnableObject
{
	private int _uid;
	private StaticObjectInstance _instance;

	public StaticObjectObject(int id)
	{
		_uid = id;
	}

	@Override
	public void spawnObject(Event event, Reflection reflection)
	{
		_instance = StaticObjectHolder.getInstance().getObject(_uid);
	}

	@Override
	public void despawnObject(Event event, Reflection reflection)
	{
		//
	}

	@Override
	public void respawnObject(Event event, Reflection reflection)
	{

	}

	@Override
	public void refreshObject(Event event, Reflection reflection)
	{
		if(!event.isInProgress())
			_instance.removeEvent(event);
		else
			_instance.addEvent(event);
	}

	public void setMeshIndex(int id)
	{
		_instance.setMeshIndex(id);
		_instance.broadcastInfo(false);
	}

	public int getUId()
	{
		return _uid;
	}
}
