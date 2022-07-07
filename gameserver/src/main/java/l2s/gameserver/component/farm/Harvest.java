package l2s.gameserver.component.farm;

public class Harvest {
    private final int owner;
    private final int id;
    private final long count;
    private final boolean fresh;

    public Harvest(int id, long count, int owner, boolean fresh) {
        this.id = id;
        this.count = count;
        this.owner = owner;
        this.fresh = fresh;
    }

    public int getId() {
        return id;
    }

    public long getCount() {
        return count;
    }

    public int getOwner() {
        return owner;
    }

    public boolean isFresh() {
        return fresh;
    }
}