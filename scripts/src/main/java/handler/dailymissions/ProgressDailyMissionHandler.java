package handler.dailymissions;

import l2s.gameserver.Config;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.network.l2.s2c.ExConnectedTimeAndGettableReward;
import l2s.gameserver.network.l2.s2c.ExOneDayReceiveRewardList;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
 **/
public abstract class ProgressDailyMissionHandler extends BasicDailyMissionHandler {

	protected static final Logger logger = LoggerFactory.getLogger("AnakimLilithDaily");

	@Override
	public boolean haveProgress(DailyMissionTemplate missionTemplate) {
		return missionTemplate.getRepetitionCount() > 1;
	}

	@Override
	public DailyMissionStatus getStatus(Player player, DailyMission mission, DailyMissionTemplate missionTemplate) {
		if(mission != null) {
			if(mission.isCompleted()) {
				return DailyMissionStatus.COMPLETED;
			}

			if(player.getLevel() < 70) {
				return DailyMissionStatus.NOT_AVAILABLE;
			}

			if(mission.getValue() >= missionTemplate.getRepetitionCount()) {
				return DailyMissionStatus.AVAILABLE;
			}
		}

		return DailyMissionStatus.NOT_AVAILABLE;
	}

	public void progressMission(Player player, int addValue) {
		progressMission(player, addValue, null);
	}

	public void progressMission(Player player, int addValue, Predicate<DailyMissionTemplate> predicate) {
		if(!Config.EX_USE_TO_DO_LIST) {
			return;
		}

		Party party = player.getParty();
		if(party != null) {
			progressMissionParty(player, party, addValue, predicate);
		}
		else {
			progressMissionNoParty(player, addValue, predicate);
		}
	}

	private void progressMissionNoParty(Player player, int addValue, Predicate<DailyMissionTemplate> predicate) {
		boolean update = false;
		Collection<DailyMissionTemplate> missionTemplates = player.getDailyMissionList().getAvailableMissions();
		for(DailyMissionTemplate missionTemplate : missionTemplates) {
			if(missionTemplate.getHandler() != this) {
				continue;
			}
			if(progressMissionPlayer(player, addValue, missionTemplate, predicate)) {
				update = true;
			}
		}
		if(update) {
			sendPacketsRewardList(player);
		}
	}


	private void progressMissionParty(Player player, Party party, int addValue, Predicate<DailyMissionTemplate> predicate) {
		List<Player> list = party.getPartyMembers().stream().filter(e -> player.getDistance(e) <= 5000).collect(Collectors.toList());
		for(Player partyMember : list) {
			boolean update = false;
			Collection<DailyMissionTemplate> missionTemplates = partyMember.getDailyMissionList().getAvailableMissions();
			for(DailyMissionTemplate missionTemplate : missionTemplates) {
				if(missionTemplate.getHandler() != this) {
					continue;
				}
				if(!missionTemplate.isPartyShared() || !canBeDistributedToParty()) {
					if(player != partyMember) {
						continue;
					}
					if(progressMissionPlayer(partyMember, addValue, missionTemplate, predicate)) {
						update = true;
					}
				}
				else if(progressMissionPlayer(partyMember, addValue, missionTemplate, predicate)) {
					update = true;
				}

			}
			if(update) {
				sendPacketsRewardList(partyMember);
			}
		}
	}

	private void sendPacketsRewardList(Player partyMember) {
		if (this instanceof KillAnakimLilithDailyMissionHandler) {
			String name = partyMember.getName();
			String hwid = "NO_HWID";
			final HwidHolder hwidHolder = partyMember.getHwidHolder();
			if (hwidHolder != null) {
				hwid = hwidHolder.asString();
			}
			logger.info("Reward sent: " + name + "[" + hwid + "]");
		}
		partyMember.sendPacket(new ExOneDayReceiveRewardList(partyMember));
		partyMember.sendPacket(new ExConnectedTimeAndGettableReward(partyMember));
	}

	private boolean progressMissionPlayer(Player player, int addValue, DailyMissionTemplate template, Predicate<DailyMissionTemplate> predicate) {
		DailyMission mission = player.getDailyMissionList().get(template, true);
		if(mission.isCompleted()) {
			return false;
		}

		if(predicate != null && !predicate.test(template)) {
			return false;
		}

		mission.setValue(mission.getValue() + addValue);
		return true;
	}
}
