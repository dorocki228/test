package l2s.gameserver.model.entity.events.impl.pvparena;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.snapshot.SnapshotPlayer;

public class PvpArenaPlayer {
    private final int objId;
    private final SnapshotPlayer snapshotPlayer;
    private final HardReference<Player> playerRef;

    public PvpArenaPlayer(Player player, SnapshotPlayer snapshotPlayer) {
        this.objId = player.getObjectId();
        this.playerRef = player.getRef();
        this.snapshotPlayer = snapshotPlayer;
    }

    public Player getPlayer() {
        return playerRef.get();
    }

    public int getObjId() {
        return objId;
    }

    public SnapshotPlayer getSnapshotPlayer() {
        return snapshotPlayer;
    }
}
