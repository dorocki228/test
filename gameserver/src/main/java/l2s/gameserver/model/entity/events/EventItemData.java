package l2s.gameserver.model.entity.events;

public class EventItemData {
    private final int id;
    private final long count;

    public EventItemData(int id, long count) {
        this.id = id;
        this.count = count;
    }

    public EventItemData(String str) {
        String[] split = str.split(":");
        id = Integer.parseInt(split[0]);
        count = Integer.parseInt(split[1]);
    }

    public int getId() {
        return id;
    }

    public long getCount() {
        return count;
    }
}
