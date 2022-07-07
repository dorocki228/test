package l2s.gameserver.dao;

import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.enums.FactionLeaderStateType;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.factionleader.FactionLeader;
import l2s.gameserver.model.factionleader.FactionLeaderPrivileges;
import l2s.gameserver.model.factionleader.FactionLeaderRequest;
import l2s.gameserver.model.factionleader.FactionLeaderVote;
import l2s.gameserver.network.l2.components.hwid.DefaultHwidHolder;
import l2s.gameserver.service.FactionLeaderService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class FactionLeaderDAO {
    private static final FactionLeaderDAO INSTANCE = new FactionLeaderDAO();
    private static final String[] requestsForDelete = {"DELETE FROM faction_leader_request", "DELETE FROM faction_leader_privileges", "DELETE FROM faction_leader_vote"};
    private static final JdbcTemplate jdbc = DatabaseFactory.getInstance().getJdbcTemplate();

    //@formatter:off
    private static final String SELECT_LEADER_NAMES_FROM_FACTION =
            "SELECT f.obj_Id, f.char_name\n" +
                    "FROM faction_leader_request fl\n" +
                    "       LEFT JOIN (SELECT c.obj_id, c.char_name\n" +
                    "                  FROM characters c\n" +
                    "                         LEFT JOIN character_variables cv ON c.obj_Id = cv.obj_id\n" +
                    "                  WHERE cv.name = 'fraction'\n" +
                    "                    AND cv.value = ?) AS f ON fl.obj_id = f.obj_Id WHERE f.obj_Id IS NOT NULL ORDER BY f.char_name";
    //@formatter:on
    private FactionLeaderDAO() {
    }

    public static FactionLeaderDAO getInstance() {
        return INSTANCE;
    }

    public Map<Integer, FactionLeaderPrivileges> selectFactionLeaders(FactionLeader factionLeader) {
        Map<Integer, FactionLeaderPrivileges> map = new HashMap<>();
        jdbc.query("SELECT * FROM faction_leader_privileges WHERE faction=?", rs -> {
            int objId = rs.getInt("obj_id");
            int privileges = rs.getInt("privileges");
            FactionLeaderPrivileges factionLeaderPrivileges = new FactionLeaderPrivileges(objId, privileges);
            map.put(factionLeaderPrivileges.getObjId(), factionLeaderPrivileges);
        }, factionLeader.getFaction().ordinal());
        return map;
    }

    public Map<Integer, FactionLeaderRequest> selectFactionRequests(FactionLeader factionLeader) {
        Map<Integer, FactionLeaderRequest> map = new HashMap<>();
        jdbc.query("SELECT * FROM faction_leader_request WHERE faction=?", rs -> {
            int objId = rs.getInt("obj_id");
            DefaultHwidHolder hwid = new DefaultHwidHolder(rs.getString("hwid"));
            FactionLeaderRequest factionLeaderRequest = new FactionLeaderRequest(objId, hwid);
            map.put(factionLeaderRequest.getObjId(), factionLeaderRequest);
        }, factionLeader.getFaction().ordinal());
        return map;
    }

    public List<FactionLeaderVote> selectFactionVotes(FactionLeader factionLeader) {
        List<FactionLeaderVote> list = new ArrayList<>();
        jdbc.query("SELECT * FROM faction_leader_vote WHERE faction=?", rs -> {
            int votedObjId = rs.getInt("voted_obj_id");
            int votedForObjId = rs.getInt("voted_for_obj_id");
            DefaultHwidHolder hwid = new DefaultHwidHolder(rs.getString("hwid"));
            FactionLeaderVote factionLeaderVote = new FactionLeaderVote(votedObjId, votedForObjId, hwid);
            list.add(factionLeaderVote);
        }, factionLeader.getFaction().ordinal());
        return list;
    }

    public void insertFactionState(FactionLeaderService service) {
        jdbc.update("REPLACE INTO faction_leader_state (type, cycle, state, end_cycle, start_cycle) VALUES(1,?,?,?,?)",
                service.getCycle(), service.getState().ordinal(),
                service.getEndCycle(), service.getStartCycle());
    }

    public void requestForDelete() {
        jdbc.batchUpdate(requestsForDelete);
    }

    public void batchLeaderPrivileges(Fraction faction, List<FactionLeaderPrivileges> values) {
        jdbc.batchUpdate("INSERT INTO faction_leader_privileges (faction, obj_id, privileges) VALUES (?,?,?)" +
                " ON DUPLICATE KEY UPDATE privileges=VALUES(privileges)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FactionLeaderPrivileges factionLeaderPrivileges = values.get(i);
                ps.setInt(1, faction.ordinal());
                ps.setInt(2, factionLeaderPrivileges.getObjId());
                ps.setInt(3, factionLeaderPrivileges.getPrivileges());
            }

            @Override
            public int getBatchSize() {
                return values.size();
            }
        });
    }

    public void insertVote(Fraction fraction, FactionLeaderVote vote) {
        jdbc.update("INSERT INTO faction_leader_vote (faction, voted_obj_id, voted_for_obj_id, hwid) VALUES (?,?,?,?)",
                fraction.ordinal(), vote.getVotedObjId(), vote.getVotedForObjId(), vote.getHwid().asString());
    }

    public void insertRequest(Fraction fraction, FactionLeaderRequest request) {
        jdbc.update("INSERT INTO faction_leader_request (faction, obj_id, hwid) VALUES (?,?,?) " +
                        "ON DUPLICATE KEY UPDATE hwid=VALUES(hwid)",
                fraction.ordinal(), request.getObjId(), request.getHwid().asString());
    }

    public List<Pair<Integer, String>> selectLeaderNamesFromFaction(Fraction faction) {
        if(faction == Fraction.NONE)
            return Collections.emptyList();
        List<Pair<Integer, String>> list = new ArrayList<>();
        jdbc.query(SELECT_LEADER_NAMES_FROM_FACTION, (rs) -> {
            int objId = rs.getInt("obj_id");
            String charName = rs.getString("char_name");
            list.add(Pair.of(objId, charName));
        }, faction.ordinal());
        return list;
    }

    public void selectState(FactionLeaderService service) {
        jdbc.query("SELECT * FROM faction_leader_state WHERE type=1 LIMIT 1", rs -> {
            int cycle = rs.getInt("cycle");
            int state = rs.getInt("state");
            long endCycle = rs.getLong("end_cycle");
            long startCycle = rs.getLong("start_cycle");
            service.setCycle(cycle);
            service.setState(FactionLeaderStateType.getValueFromOrdinal(state));
            service.setEndCycle(endCycle);
            service.setStartCycle(startCycle);
        });
    }
}
