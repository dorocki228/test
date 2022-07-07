package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDailyMissionsDAO;
import l2s.gameserver.data.xml.holder.DailyMissionsHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;
import l2s.gameserver.templates.dailymissions.DailyRewardTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DailyMissionList
{
	private static final Logger _log = LoggerFactory.getLogger(DailyMissionList.class);
	private final Player _owner;
	private final Map<Integer, DailyMission> _missions;

	public DailyMissionList(Player owner)
	{
		_missions = new HashMap<>();
		_owner = owner;
	}

	public void restore()
	{
		CharacterDailyMissionsDAO.getInstance().restore(_owner, _missions);
		for(DailyMission mission : values())
			mission.getTemplate().getHandler().onRestoreMission(_owner, mission);
	}

	public void store()
	{
		CharacterDailyMissionsDAO.getInstance().store(_owner, values());
	}

	/*public void reset()
	{
		values().stream()
				.filter(DailyMission::isCompleted)
				.filter(DailyMission::getReusePattern)
				.forEach(mission ->
				{
					mission.setCompleted(null);
					mission.setValue(0);
				});
	}*/

	public Collection<DailyMission> values()
	{
		return _missions.values();
	}

	public DailyMission get(DailyMissionTemplate missionTemplate, boolean init)
	{
		DailyMission mission = _missions.get(missionTemplate.getId());
		if(mission == null && init)
		{
			mission = new DailyMission(missionTemplate, null, 0);
			_missions.put(mission.getId(), mission);
		}
		return mission;
	}

	public Collection<DailyMissionTemplate> getAvailableMissions()
	{
		if(!Config.EX_USE_TO_DO_LIST)
			return Collections.emptyList();
		return DailyMissionsHolder.getInstance().getMissions(_owner.getBaseClassId());
	}

	public DailyMissionStatus getStatus(DailyMissionTemplate missionTemplate)
	{
		if(!Config.EX_USE_TO_DO_LIST)
			return DailyMissionStatus.NOT_AVAILABLE;

		if(_owner.containsEvent(SingleMatchEvent.class))
			return DailyMissionStatus.NOT_AVAILABLE;

		return missionTemplate.getHandler().getStatus(_owner, _missions.get(missionTemplate.getId()), missionTemplate);
	}

	public int getProgress(DailyMissionTemplate missionTemplate)
	{
		if(!Config.EX_USE_TO_DO_LIST)
			return 0;
		DailyMission mission = get(missionTemplate, false);
		if(mission == null)
			return 0;
		if(mission.isCompleted())
			return missionTemplate.getRepetitionCount();
		return mission.getValue();
	}

	public boolean complete(int missionId)
	{
		DailyMissionTemplate missionTemplate = DailyMissionsHolder.getInstance().getMission(missionId);
		if(missionTemplate == null)
			return false;
		DailyMissionStatus status = getStatus(missionTemplate);
		if(status != DailyMissionStatus.AVAILABLE)
			return false;
		if(_owner.getWeightPenalty() >= 3 || _owner.getInventoryLimit() * 0.8 < _owner.getInventory().getSize())
		{
			_owner.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		DailyMission mission = get(missionTemplate, true);
		int missionValue = mission.getValue();
		mission.setCompleted(ZonedDateTime.now());
		if(!CharacterDailyMissionsDAO.getInstance().insert(_owner, mission))
		{
			mission.setValue(missionValue);
			mission.setCompleted(null);
			return false;
		}

		missionTemplate.getHandler().onComplete(mission, _owner);

		for(DailyRewardTemplate reward : missionTemplate.getRewards())
			if(reward.containsClassId(_owner.getBaseClassId()))
				for(ItemData item : reward.getRewardItems())
					ItemFunctions.addItem(_owner, item.getId(), item.getCount());
		return true;
	}

	@Override
	public String toString()
	{
		return "DailyMissionList[owner=" + _owner.getName() + "]";
	}
}
