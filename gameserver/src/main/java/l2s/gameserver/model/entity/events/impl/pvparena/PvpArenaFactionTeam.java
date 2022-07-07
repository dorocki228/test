package l2s.gameserver.model.entity.events.impl.pvparena;

import l2s.gameserver.model.base.Fraction;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PvpArenaFactionTeam {
    private final Fraction fraction;
    private final Map<Integer, PvpArenaPlayer> playerMap;
    private final AtomicInteger counter = new AtomicInteger(0);

    public PvpArenaFactionTeam(Fraction fraction, Map<Integer, PvpArenaPlayer> playerMap) {
        this.fraction = fraction;
        this.playerMap = new ConcurrentHashMap<>(playerMap);
    }

    public int getAllKillCount() {
        return counter.get();
    }

    public Fraction getFraction() {
        return fraction;
    }

    public void incrementAllKillCount() {
        counter.incrementAndGet();
    }

    public PvpArenaPlayer getArenaPlayer(int objectId) {
        return playerMap.get(objectId);
    }

    public PvpArenaPlayer removeArenaPlayer(int objectId) {
        return playerMap.remove(objectId);
    }

    public Collection<PvpArenaPlayer> getPvpArenaPlayers() {
        return playerMap.values();
    }

    public boolean isParticipant(int objectId) {
        return playerMap.containsKey(objectId);
    }
}
