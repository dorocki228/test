package l2s.gameserver.model.entity.events.impl.arena;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.snapshot.SnapshotPlayer;
import l2s.gameserver.service.PlayerService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author mangol
 */
public class ArenaTeam {
	private final TeamType teamType;
	private final int leaderId;
	private String teamName;
	private List<ArenaPlayer> players = new CopyOnWriteArrayList<>();
	private int bidCount;

	public ArenaTeam(TeamType teamType, int leaderId, String teamName) {
		this.teamType = teamType;
		this.leaderId = leaderId;
		this.teamName = teamName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void addPlayer(Player player) {
		ArenaPlayer arenaPlayer = new ArenaPlayer(player);
		arenaPlayer.setTeamType(teamType);
		players.add(arenaPlayer);
	}

	public void removePlayer(int objId) {
		players.removeIf(e -> {
			Player player = e.getPlayer();
			if(player == null) {
				return true;
			}
			return player.getObjectId() == objId;
		});
	}

	public void generateSnapshots() {
		for(ArenaPlayer player : players) {
			SnapshotPlayer snapshot = PlayerService.getInstance().createSnapshot(player.getPlayer());
			player.setSnapshotPlayer(snapshot);
		}
	}

	public List<ArenaPlayer> getPlayers() {
		return players;
	}

	public TeamType getTeamType() {
		return teamType;
	}

	public void setPlayers(List<ArenaPlayer> players) {
		this.players = players;
	}

	public int getBidCount() {
		return bidCount;
	}

	public void setBidCount(int bidCount) {
		this.bidCount = bidCount;
	}

	public int getLeaderId() {
		return leaderId;
	}

	public Player getLeader() {
		return GameObjectsStorage.getPlayer(leaderId);
	}
}
