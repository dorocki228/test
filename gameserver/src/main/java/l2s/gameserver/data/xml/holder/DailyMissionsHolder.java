package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;
import l2s.gameserver.templates.dailymissions.DailyRewardTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DailyMissionsHolder extends AbstractHolder
{
	private static final DailyMissionsHolder _instance;
	private final TIntObjectMap<DailyMissionTemplate> _missions;
	private final TIntObjectMap<Set<DailyMissionTemplate>> _missionsByClassId;

	public DailyMissionsHolder()
	{
		_missions = new TIntObjectHashMap<>();
		_missionsByClassId = new TIntObjectHashMap<>(ClassId.VALUES.length);
	}

	public static DailyMissionsHolder getInstance()
	{
		return _instance;
	}

	public void addMission(DailyMissionTemplate mission)
	{
		_missions.put(mission.getId(), mission);
		for(DailyRewardTemplate reward : mission.getRewards())
			for(ClassId classId : ClassId.VALUES)
				if(reward.containsClassId(classId.getId()))
				{
					Set<DailyMissionTemplate> missionsByClassId = _missionsByClassId.get(classId.getId());
					if(missionsByClassId == null)
					{
						missionsByClassId = new HashSet<>();
						_missionsByClassId.put(classId.getId(), missionsByClassId);
					}
					missionsByClassId.add(mission);
				}
	}

	public DailyMissionTemplate getMission(int id)
	{
		return _missions.get(id);
	}

	public Collection<DailyMissionTemplate> getMissions()
	{
		return _missions.valueCollection();
	}

	public Collection<DailyMissionTemplate> getMissions(int classId)
	{
		Collection<DailyMissionTemplate> missions = _missionsByClassId.get(classId);
		if(missions == null)
			return Collections.emptyList();
		return missions;
	}

	@Override
	public int size()
	{
		return _missions.size();
	}

	@Override
	public void clear()
	{
		_missions.clear();
	}

	static
	{
		_instance = new DailyMissionsHolder();
	}
}
