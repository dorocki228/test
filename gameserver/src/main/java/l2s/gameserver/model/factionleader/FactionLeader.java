package l2s.gameserver.model.factionleader;

import l2s.gameserver.dao.FactionLeaderDAO;
import l2s.gameserver.model.base.Fraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FactionLeader {
    private final Fraction faction;
    private final Map<Integer, FactionLeaderRequest> requestMap = new ConcurrentHashMap<>();
    private final Map<Integer, FactionLeaderPrivileges> privilegesMap = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<FactionLeaderVote> leaderVotes = new CopyOnWriteArrayList<>();

    public FactionLeader(Fraction faction) {
        this.faction = faction;
    }

    public void restore() {
        privilegesMap.putAll(FactionLeaderDAO.getInstance().selectFactionLeaders(this));
        requestMap.putAll(FactionLeaderDAO.getInstance().selectFactionRequests(this));
        leaderVotes.addAll(FactionLeaderDAO.getInstance().selectFactionVotes(this));
    }

    public Map<Integer, FactionLeaderPrivileges> getPrivilegesMap() {
        return privilegesMap;
    }

    public FactionLeaderPrivileges getLeaderPrivileges(int objId) {
        return privilegesMap.get(objId);
    }

    public Fraction getFaction() {
        return faction;
    }

    public Map<Integer, FactionLeaderRequest> getRequestMap() {
        return requestMap;
    }

    public CopyOnWriteArrayList<FactionLeaderVote> getLeaderVotes() {
        return leaderVotes;
    }

    public boolean isRequest(int objId) {
        return requestMap.containsKey(objId);
    }
}
