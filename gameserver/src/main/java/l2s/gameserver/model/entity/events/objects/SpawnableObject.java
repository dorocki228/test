package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.events.Event;

public interface SpawnableObject
{
	void spawnObject(Event p0);

	void despawnObject(Event p0);

	void respawnObject(Event p0);

	void refreshObject(Event p0);
}
