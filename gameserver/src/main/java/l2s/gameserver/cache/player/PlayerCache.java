package l2s.gameserver.cache.player;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import l2s.gameserver.database.DatabaseFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class PlayerCache {
    private static final String SELECT_CHARACTER = "SELECT ch.char_name, ch.sex, ch.clanid FROM characters AS ch WHERE obj_Id=? LIMIT 1";

    private static PlayerCache ourInstance = new PlayerCache();
    private final LoadingCache<Integer, PlayerData> cache;

    public static PlayerCache getInstance() {
        return ourInstance;
    }

    private PlayerCache() {
        cache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(this::load);
    }

    public PlayerData get(int objId) {
        return cache.get(objId);
    }

    private PlayerData load(int objId) {
        return DatabaseFactory.getInstance().getJdbcTemplate().execute(new ConnectionCallback<>() {
            @Nullable
            @Override
            public PlayerData doInConnection(Connection con) throws SQLException
            {

                PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER);
                statement.setInt(1, objId);
                ResultSet resultSet = statement.executeQuery();
                PlayerData playerData = null;
                if(resultSet.next()) {
                    playerData = new PlayerData(objId);
                    String name = resultSet.getString("char_name");
                    int sex = resultSet.getInt("sex");
                    int clanId = resultSet.getInt("clanid");
                    playerData.setName(name);
                    playerData.setSex(sex);
                    playerData.setClanId(clanId);
                }
                JdbcUtils.closeStatement(statement);
                JdbcUtils.closeResultSet(resultSet);
                return playerData;
            }
        });
    }
}
