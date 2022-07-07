package l2s.gameserver.data.xml.holder;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.component.farm.GatheringTemplate;
import l2s.gameserver.model.farm.Stead;

import java.util.*;

public class SteadDataHolder extends AbstractHolder {
    private static SteadDataHolder instance;
    private final Map<Integer, GatheringTemplate> gatherings;
    private final Map<Integer, Stead> steads;
    private final MultiValueSet<String> configuration;

    private SteadDataHolder() {
        gatherings = new HashMap<>();
        steads = new HashMap<>();
        configuration = new MultiValueSet<>();
    }

    public static SteadDataHolder getInstance() {
        if (instance == null)
            instance = new SteadDataHolder();
        return instance;
    }

    public GatheringTemplate getGathering(int id) {
        return gatherings.get(id);
    }

    public void addConfiguration(String key, String param) {
        configuration.set(key, param);
    }

    public void addGathering(GatheringTemplate template) {
        gatherings.put(template.getId(), template);
    }

    public void addStead(int possession, Stead stead) {
        steads.putIfAbsent(possession, stead);
    }

    public Stead getStead(int possession) {
        return steads.get(possession);
    }

    public MultiValueSet<String> getConfiguration() {
        return configuration;
    }

    public Collection<Stead> getSteads() {
        return steads.values();
    }

    @Override
    public int size() {
        return gatherings.size() + steads.size();
    }

    @Override
    public void clear() {
        gatherings.clear();
        steads.clear();
    }
}
