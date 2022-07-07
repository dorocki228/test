package l2s.gameserver.dao;

import l2s.gameserver.data.xml.holder.ArtifactHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.entity.ArtifactEntity;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.templates.artifact.ArtifactTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

public class ArtifactDAO {
    private static ArtifactDAO INSTANCE = new ArtifactDAO();
    private final JdbcTemplate jdbc;

    private ArtifactDAO() {
        this.jdbc = DatabaseFactory.getInstance().getJdbcTemplate();
    }

    public static ArtifactDAO getInstance() {
        return INSTANCE;
    }

    public Map<Integer, ArtifactEntity> restore() {
        return jdbc.query("SELECT * FROM artifact", rs -> {
            Map<Integer, ArtifactEntity> map = new HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                Fraction faction = Fraction.getIfPresent(rs.getInt("faction"));
                long endProtect = rs.getLong("end_protect");
                final ArtifactTemplate artifactTemplate = ArtifactHolder.getInstance().get(id);
                if(artifactTemplate == null)
                    continue;
                final ArtifactEntity artifactEntity = new ArtifactEntity(artifactTemplate, faction);
                artifactEntity.setEndProtect(endProtect);
                map.put(id, artifactEntity);
            }
            return map;
        });
    }

    public void store(ArtifactEntity entity) {
        jdbc.update("INSERT INTO artifact (id, faction, end_protect) VALUES (?,?,?) ON DUPLICATE KEY UPDATE faction=?, end_protect=?", entity.getTemplate().getId(), entity.getFraction().ordinal(), entity.getEndProtect(), entity.getFraction().ordinal(), entity.getEndProtect());
    }
}
