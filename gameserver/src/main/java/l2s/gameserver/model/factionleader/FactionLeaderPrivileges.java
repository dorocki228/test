package l2s.gameserver.model.factionleader;

public class FactionLeaderPrivileges {
    private final int objId;
    private int privileges;

    public FactionLeaderPrivileges(int objId, int privileges) {
        this.objId = objId;
        this.privileges = privileges;
    }

    public int getObjId() {
        return objId;
    }

    public int getPrivileges() {
        return privileges;
    }

    public void setPrivileges(int privileges) {
        this.privileges = privileges;
    }
}
