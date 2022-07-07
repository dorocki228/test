package l2s.gameserver.dao;

import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.promocode.PlayerPromocode;
import l2s.gameserver.model.promocode.Promocode;
import l2s.gameserver.network.l2.components.hwid.DefaultHwidHolder;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.service.PromocodeService;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Mangol
 */
public class PromocodeDAO
{
    private static PromocodeDAO ourInstance = new PromocodeDAO();

    private static final String INSERT = "INSERT INTO promocodes (code, uses) VALUES (?,?) ON DUPLICATE KEY UPDATE uses = ?";
    private static final String INSERT_DATA = "INSERT INTO promocodes_data (code, objId, uses, hwid) VALUES (?,?,?,?)";
    private static final String UPDATE = "UPDATE promocodes SET uses=? WHERE code=?";
    private static final String UPDATE_DATA = "UPDATE promocodes_data SET uses=?, hwid=? WHERE code=? AND objId=?";
    private static final String SELECT_ALL = "SELECT p.code, p.uses,  pd.objId,  pd.uses, pd.hwid FROM promocodes AS p LEFT JOIN promocodes_data AS pd ON(p.code = pd.code)";

    private final DatabaseFactory databaseFactory;

    private final JdbcTemplate jdbcTemplate;

    private PromocodeDAO()
    {
        databaseFactory = DatabaseFactory.getInstance();
        jdbcTemplate = databaseFactory.getJdbcTemplate();
    }

    public static PromocodeDAO getInstance()
    {
        return ourInstance;
    }

    public void restore()
    {
        jdbcTemplate.query(SELECT_ALL, rs ->
        {
            String code = rs.getString(1);
            int uses = rs.getInt(2);
            int objId = rs.getInt(3);
            int usesFromPlayer = rs.getInt(4);
            HwidHolder hwidHolder = new DefaultHwidHolder(rs.getBytes(5));
            Promocode promocode = PromocodeService.getInstance().getPromocodeFromIdsMap().computeIfAbsent(code, k -> new Promocode(k, uses));
            if(objId != 0)
            {
                promocode.getPlayerPromocodeMap().put(objId, new PlayerPromocode(code, objId, usesFromPlayer, hwidHolder));
                PromocodeService.getInstance().addAndGetHwid(code).add(hwidHolder);
            }
        });
    }

    public void insertOrUpdate(Promocode promocode)
    {
        jdbcTemplate.update(INSERT, promocode.getId(), promocode.getUses(), promocode.getUses());
    }

    public void insertData(PlayerPromocode playerPromocode)
    {
        jdbcTemplate.update(INSERT_DATA, playerPromocode.getId(), playerPromocode.getObjId(),
                playerPromocode.getUses(), playerPromocode.getHwidHolder().asByteArray());
    }

    public void update(Promocode promocode)
    {
        jdbcTemplate.update(UPDATE, promocode.getUses(), promocode.getId());
    }

    public void updateData(PlayerPromocode promocode)
    {
        jdbcTemplate.update(UPDATE_DATA, promocode.getUses(), promocode.getHwidHolder().asByteArray(), promocode.getId(), promocode.getObjId());
    }
}
