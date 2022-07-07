package l2s.gameserver.model.entity.events.impl.arena;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.snapshot.SnapshotPlayer;

/**
 * @author mangol
 */
public class ArenaPlayer {
	private final int objId;
	private TeamType teamType;
	private SnapshotPlayer snapshotPlayer;
	private HardReference<Player> playerRef;

	ArenaPlayer(Player player) {
		this.objId = player.getObjectId();
		this.playerRef = player.getRef();
	}

	public Player getPlayer() {
		return playerRef.get();
	}

	public TeamType getTeamType() {
		return teamType;
	}

	public int getObjId() {
		return objId;
	}

	public void setTeamType(TeamType teamType) {
		this.teamType = teamType;
	}

	public SnapshotPlayer getSnapshotPlayer() {
		return snapshotPlayer;
	}

	void setSnapshotPlayer(SnapshotPlayer snapshotPlayer) {
		this.snapshotPlayer = snapshotPlayer;
	}
}
