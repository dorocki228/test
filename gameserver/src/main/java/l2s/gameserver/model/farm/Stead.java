package l2s.gameserver.model.farm;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.farm.zone.SteadZone;
import l2s.gameserver.model.instances.residences.farm.SeedInstance;
import l2s.gameserver.templates.ZoneTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Stead {
    private final int size;
    private final Residence possession;
    private final ListMultimap<Player, SeedInstance> seeds;
    private final List<SteadZone> zones;

    // Участок может быть размером от 30 до 60, зависит от зоны
    public Stead(int id, int size) {
        seeds = Multimaps.newListMultimap(new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);
        zones = new ArrayList<>();
        possession = ResidenceHolder.getInstance().getResidence(id);
        this.size = size;
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Ticker(), 1000L, 1000L);
    }

    public SteadZone addZone(String name) {
        ZoneTemplate template = ZoneHolder.getInstance().getTemplate(name);
        SteadZone zone = new SteadZone(template);
        zone.setStead(this);
        zone.setActive(true);
        zone.setReflection(ReflectionManager.MAIN);
        zones.add(zone);
        return zone;
    }

    public void changeOwner() {
        zones.forEach(zone -> {
            if (zone != null) {
                zone.getInsideNpcs().stream()
                        .filter(SeedInstance.class::isInstance)
                        .map(SeedInstance.class::cast)
                        .forEach(seed -> seed.removeMe(false));
            }
        });
    }

    public boolean checkSeeds(Player player) {
        return getSeeds(player).size() < size;
    }

    public List<SeedInstance> getSeeds(Player player) {
        return seeds.get(player);
    }

    public void shutdown() {
        seeds.values().forEach(seed -> seed.removeMe(true));
    }

    public Residence getPossession() {
        return possession;
    }

    public int getSize() {
        return size;
    }

    public class Ticker implements Runnable {
        @Override
        public void run() {
            seeds.values().forEach(SeedInstance::tick);
        }
    }
}