package l2s.gameserver.cache.player;

/**
 * @author Mangol
 */
public class PlayerData {
    private final int objId;
    private String name = "";
    private int sex;
    private int clanId;

    PlayerData(int objId) {
        this.objId = objId;
    }

    public int getObjId() {
        return objId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getClanId() {
        return clanId;
    }

    public void setClanId(int clanId) {
        this.clanId = clanId;
    }
}
