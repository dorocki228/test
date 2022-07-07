package l2s.gameserver.dao;

import java.util.HashMap;
import java.util.Map;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.events.impl.casino.CasinoHistory;
import l2s.gameserver.model.entity.events.impl.casino.CasinoRoom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author KRonst
 */
public class CasinoDAO {

    private static final CasinoDAO INSTANCE = new CasinoDAO();
    private final JdbcTemplate jdbc;

    private CasinoDAO() {
        jdbc = DatabaseFactory.getInstance().getJdbcTemplate();
    }

    public static CasinoDAO getInstance() {
        return INSTANCE;
    }

    public CasinoRoom store(int creatorId, String name, int bet) {
        Map<String, Object> params = new HashMap<>();
        params.put("creator_id", creatorId);
        params.put("name", name);
        params.put("bet", bet);

        SimpleJdbcInsert insert = DatabaseFactory.getInstance().getJdbcInsert();
        Number key = insert.withTableName("casino_rooms")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(params);
        return new CasinoRoom(key.intValue(), creatorId, name, bet);
    }

    public void store(CasinoHistory history) {
        Map<String, Object> params = new HashMap<>();
        params.put("obj_id_1", history.getObjId1());
        params.put("obj_id_2", history.getObjId2());
        params.put("bet", history.getBet());
        params.put("date", history.getDate());
        params.put("winner_id", history.getWinnerId());

        SimpleJdbcInsert insert = DatabaseFactory.getInstance().getJdbcInsert();
        insert.withTableName("casino_history").execute(params);
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM casino_rooms WHERE id=?", id);
    }

    public Map<Integer, CasinoRoom> restore() {
        return jdbc.query("SELECT * FROM casino_rooms", rs -> {
            Map<Integer, CasinoRoom> rooms = new HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                int creatorId = rs.getInt("creator_id");
                String name = rs.getString("name");
                int bet = rs.getInt("bet");
                CasinoRoom room = new CasinoRoom(id, creatorId, name, bet);
                rooms.put(id, room);
            }
            return rooms;
        });
    }
}
