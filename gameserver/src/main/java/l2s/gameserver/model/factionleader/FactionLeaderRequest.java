package l2s.gameserver.model.factionleader;

import l2s.gameserver.network.l2.components.hwid.HwidHolder;

public class FactionLeaderRequest {
    private final int objId;
    private final HwidHolder hwid;

    public FactionLeaderRequest(int objId, HwidHolder hwid) {
        this.objId = objId;
        this.hwid = hwid;
    }

    public int getObjId() {
        return objId;
    }

    public HwidHolder getHwid() {
        return hwid;
    }
}
