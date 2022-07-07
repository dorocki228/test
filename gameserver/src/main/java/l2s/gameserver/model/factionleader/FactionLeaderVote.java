package l2s.gameserver.model.factionleader;

import l2s.gameserver.network.l2.components.hwid.HwidHolder;

public class FactionLeaderVote {
    private final int votedObjId;
    private final int votedForObjId;
    private final HwidHolder hwid;

    public FactionLeaderVote(int votedObjId, int votedForObjId, HwidHolder hwid) {
        this.votedObjId = votedObjId;
        this.votedForObjId = votedForObjId;
        this.hwid = hwid;
    }

    public int getVotedObjId() {
        return votedObjId;
    }

    public int getVotedForObjId() {
        return votedForObjId;
    }

    public HwidHolder getHwid() {
        return hwid;
    }
}
