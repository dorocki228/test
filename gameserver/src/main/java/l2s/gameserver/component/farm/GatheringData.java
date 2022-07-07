package l2s.gameserver.component.farm;

public class GatheringData {
    private final int id;
    private final int min;
    private final int max;
    private final double chance;

    public GatheringData(int id, int min, int max, double chance) {
        this.id = id;
        this.min = min;
        this.max = max;
        this.chance = chance;
    }

    public int getId() {
        return id;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getChance() {
        return chance;
    }
}
