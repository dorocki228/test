package l2s.gameserver.dao;

import l2s.gameserver.component.player.ConfrontationComponent;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.skills.SkillEntry;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfrontationDAO {
    private static final ConfrontationDAO ourInstance = new ConfrontationDAO();
    // @formatter:off
    private static final String SELECT_TOTAL_POINTS =
            "SELECT\n" +
            "  f.fraction,\n" +
            "  SUM(c.currentPeriodPoints) AS totalPoints\n" +
            "FROM confrontation AS c\n" +
            "  RIGHT JOIN (SELECT\n" +
            "                c.obj_id,\n" +
            "                cv.value AS fraction\n" +
            "              FROM characters c LEFT JOIN character_variables cv ON c.obj_Id = cv.obj_id\n" +
            "              WHERE cv.name = 'fraction' AND cv.value != '0') AS f ON c.objId = f.obj_Id\n" +
            "GROUP BY f.fraction HAVING totalPoints IS NOT NULL";

    private static final String SELECT_CONFRONTATION_POINTS =
            "SELECT\n" +
                    "  f.obj_id,\n" +
                    "  f.char_name,\n" +
                    "  c.currentPeriodPoints AS points\n" +
                    "FROM confrontation AS c\n" +
                    "  RIGHT JOIN (SELECT\n" +
                    "                c.obj_id,\n" +
                    "                c.char_name\n" +
                    "              FROM characters c LEFT JOIN character_variables cv ON c.obj_Id = cv.obj_id\n" +
                    "              WHERE cv.name = 'fraction' AND cv.value = ?) AS f ON c.objId = f.obj_Id WHERE c.currentPeriodPoints > 0 ORDER BY c.currentPeriodPoints DESC";
    // @formatter:on

    private static final String SELECT_POINT_FROM_PLAYER = "SELECT * FROM confrontation WHERE objId=? LIMIT 1";

    private final JdbcTemplate jdbc = DatabaseFactory.getInstance().getJdbcTemplate();

    public static ConfrontationDAO getInstance() {
        return ourInstance;
    }

    public List<Triple<Integer, String, Integer>> selectConfrontationPlayer(Fraction fraction) {
        List<Triple<Integer, String, Integer>> list = new ArrayList<>();
        jdbc.query(SELECT_CONFRONTATION_POINTS, (rs) -> {
            int objId = rs.getInt("obj_id");
            int points = rs.getInt("points");
            String charName = rs.getString("char_name");
            list.add(Triple.of(objId, charName, points));
        }, fraction.ordinal());
        return list;
    }

    public void selectTotalPoints(Map<Fraction, AtomicInteger> totalPoints) {
        jdbc.query(SELECT_TOTAL_POINTS, rs -> {
            final Fraction fraction = Fraction.getIfPresent(rs.getInt("fraction"));
            int points = rs.getInt("totalPoints");
            totalPoints.get(fraction).set(points);
        });
    }

    public void selectPointFromPlayer(ConfrontationComponent confrontation) {
        jdbc.query(SELECT_POINT_FROM_PLAYER, rs -> {
            confrontation.setCurrentPeriodPoints(rs.getInt("currentPeriodPoints"));
            confrontation.setTotalPoints(rs.getInt("totalPoints"));
            confrontation.setAvailablePoints(rs.getInt("availablePoints"));
        }, confrontation.getObject().getObjectId());
    }

    public void selectPlayerSkills(ConfrontationComponent confrontation) {
        jdbc.query("SELECT skillId, skillLevel FROM confrontation_skills WHERE objId=?", rs -> {
            final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(rs.getInt("skillId"), rs.getInt("skillLevel"));
            if(skillEntry != null)
                confrontation.addSkill(skillEntry, true);
        }, confrontation.getObject().getObjectId());
    }

    public void updateConfrontation(ConfrontationComponent component) {
        jdbc.update("INSERT INTO confrontation (objId, currentPeriodPoints, totalPoints, availablePoints) " +
                        "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE currentPeriodPoints=VALUES(currentPeriodPoints)," +
                        " totalPoints=VALUES(totalPoints), availablePoints=VALUES(availablePoints)",
                component.getObject().getObjectId(),
                component.getCurrentPeriodPoints(),
                component.getTotalPoints(),
                component.getAvailablePoints()
        );
    }

    public void batchUpdateConfrontation(List<ConfrontationComponent> components) {
        jdbc.batchUpdate("INSERT INTO confrontation (objId, currentPeriodPoints, totalPoints, availablePoints) " +
                "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE currentPeriodPoints=VALUES(currentPeriodPoints)," +
                " totalPoints=VALUES(totalPoints), availablePoints=VALUES(availablePoints)", components, 500, (ps, argument) -> {
            ps.setInt(1, argument.getObject().getObjectId());
            ps.setInt(2, argument.getCurrentPeriodPoints());
            ps.setInt(3, argument.getTotalPoints());
            ps.setInt(4, argument.getAvailablePoints());
        });
    }

    public void updateSkill(int objId, SkillEntry skill) {
        jdbc.update("INSERT INTO confrontation_skills (objId, skillId, skillLevel) VALUES (?,?,?) ON DUPLICATE KEY UPDATE skillLevel=VALUES(skillLevel)", objId, skill.getId(), skill.getLevel());
    }

    public void resetPeriod() {
        jdbc.update("UPDATE confrontation SET currentPeriodPoints=0");
    }
}
