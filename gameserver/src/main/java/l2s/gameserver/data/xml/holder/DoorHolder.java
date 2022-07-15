package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.DoorTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.*;

public final class DoorHolder extends AbstractHolder
{
	private static final DoorHolder _instance = new DoorHolder();

	private IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap<DoorTemplate>();
	private Map<String, Set<Integer>> doorsByGroup = new HashMap<>();

	public static DoorHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(DoorTemplate door)
	{
		_doors.put(door.getId(), door);
		doorsByGroup.computeIfAbsent(door.getGroupName(), key -> new HashSet<>())
				.add(door.getId());
	}

	public DoorTemplate getTemplate(int doorId)
	{
		return _doors.get(doorId);
	}

	public IntObjectMap<DoorTemplate> getDoors()
	{
		return _doors;
	}

	public Set<Integer> getDoorsByGroup(String groupName) {
		return doorsByGroup.getOrDefault(groupName, Collections.emptySet());
	}

	@Override
	public int size()
	{
		return _doors.size();
	}

	@Override
	public void clear()
	{
		_doors.clear();
	}
}
