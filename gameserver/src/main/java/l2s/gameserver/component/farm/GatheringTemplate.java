package l2s.gameserver.component.farm;

import java.util.ArrayList;
import java.util.List;

public class GatheringTemplate {
    private final int id;
    private final int maturation;
    private final List<GatheringData> crops;
    private final List<GatheringData> seeds;
    private int[] model;

    public GatheringTemplate(int id, int maturation) {
        this.id = id;
        this.maturation = maturation;
        this.crops = new ArrayList<>();
        this.seeds = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setModel(final int[] model) {
        this.model = model;
    }

    public int getModel(int index) {
        return model[index];
    }

    public int getMaturationTime() {
        return maturation;
    }

    public List<GatheringData> getCrops() {
        return crops;
    }

    public List<GatheringData> getSeeds() {
        return seeds;
    }

    public void addCrop(GatheringData crop) {
        crops.add(crop);
    }

    public void addSeed(GatheringData seed) {
        seeds.add(seed);
    }
}