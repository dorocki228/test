package l2s.gameserver.model.farm;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import l2s.gameserver.GameServer;
import l2s.gameserver.component.farm.Harvest;
import l2s.gameserver.dao.SteadBarnDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.farm.listener.OnShutdown;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SteadBarnManager {
    private static SteadBarnManager instance;

    private final Multimap<Integer, Harvest> barn = Multimaps.newListMultimap(
            new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);

    private final Map<HwidHolder, Player> usedHwids = new HashMap<>();

    public static SteadBarnManager getInstance() {
        if (instance == null)
            instance = new SteadBarnManager();

        return instance;
    }

    public void init() {
        SteadBarnDAO.getInstance().select();
        GameServer.getInstance().addListener(new OnShutdown());
    }

    public void addHarvest(Harvest harvest) {
        barn.put(harvest.getOwner(), harvest);
    }

    public Collection<Harvest> getList(int owner) {
        return barn.get(owner);
    }

    public List<Harvest> getAllFresh() {
        return barn.values().stream()
                .filter(Harvest::isFresh)
                .collect(Collectors.toList());
    }

    public void addHwid(Player player) {
        usedHwids.put(player.getHwidHolder(), player);
    }

    public boolean checkHwid(Player player) {
        if (!usedHwids.containsKey(player.getHwidHolder()))
            return true;
        if (!usedHwids.containsValue(player))
            return false;
        return true;
    }
}