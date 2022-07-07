package l2s.gameserver.service;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import l2s.gameserver.dao.PromocodeDAO;
import l2s.gameserver.data.xml.holder.PromocodeHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.promocode.PlayerPromocode;
import l2s.gameserver.model.promocode.Promocode;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;
import l2s.gameserver.templates.promocode.PromocodeTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Mangol
 */
public class PromocodeService
{
    private static final PromocodeService ourInstance = new PromocodeService();

    private final Map<String, Promocode> promocodeFromIdsMap = new ConcurrentHashMap<>();
    private final SetMultimap<String, HwidHolder> hwidsFromIdsMap = Multimaps.newSetMultimap(
            new ConcurrentHashMap<>(), CopyOnWriteArraySet::new);

    public static PromocodeService getInstance()
    {
        return ourInstance;
    }

    private PromocodeService()
    {
    }

    public void restore()
    {
        PromocodeDAO.getInstance().restore();
    }

    public void use(Player player, String id)
    {
        boolean succes = use0(player, id);
        var message = new ParameterizedMessage("{} using promocode {}: {}", player, id, succes ? "Success" : "Fail");
        LogService.getInstance().log(LoggerType.SERVICES, message);
    }

    private boolean use0(Player player, String id)
    {
        if(player == null || id == null || id.isEmpty())
            return false;
        PromocodeTemplate template = PromocodeHolder.getInstance().get(id);
        if(template == null || !template.isEnable())
        {
            player.sendMessage(new CustomMessage("service.promocode.wrongCode"));
            return false;
        }
        Set<HwidHolder> hwidHolders = addAndGetHwid(id);
        Promocode promocode = promocodeFromIdsMap.computeIfAbsent(id, k -> new Promocode(k, 0));
        if(hwidHolders.contains(player.getHwidHolder())
                || template.getReuse() > 0 && promocode.getUses() >= template.getReuse())
        {
            player.sendMessage(new CustomMessage("service.promocode.wrongCode"));
            return false;
        }

        hwidHolders.add(player.getHwidHolder());

        boolean[] create = new boolean[1];
        PlayerPromocode playerPromocode = promocode.getPlayerPromocodeMap().computeIfAbsent(player.getObjectId(), k ->
        {
            create[0] = true;
            return new PlayerPromocode(id, player.getObjectId(), 0, player.getHwidHolder());
        });
        if(playerPromocode.getUses() >= 1)
        {
            player.sendMessage(new CustomMessage("service.promocode.wrongCode"));
            return false;
        }
        synchronized(promocode)
        {
            if(playerPromocode.getUses() >= 1 || hwidHolders.contains(player.getHwidHolder())
                    || template.getReuse() > 0 && promocode.getUses() >= template.getReuse())
            {
                player.sendMessage(new CustomMessage("service.promocode.wrongCode"));
                return false;
            }
            playerPromocode.incForUses();
            promocode.incForUses();
            DatabaseFactory.getInstance().getTransactionTemplate().execute(status ->
            {
                PromocodeDAO.getInstance().insertOrUpdate(promocode);
                if(create[0])
                    PromocodeDAO.getInstance().insertData(playerPromocode);
                else
                    PromocodeDAO.getInstance().updateData(playerPromocode);
                return null;
            });
        }
        reward(player, template);
        player.sendMessage(new CustomMessage("service.promocode.succ"));
        return true;
    }

    private void reward(Player player, PromocodeTemplate template)
    {
        if(player == null || template == null)
            return;
        template.getList().forEach(i -> ItemFunctions.addItem(player, i.getId(), i.getCount(), true));
    }


    public Map<String, Promocode> getPromocodeFromIdsMap()
    {
        return promocodeFromIdsMap;
    }

    public Set<HwidHolder> addAndGetHwid(String code)
    {
        return hwidsFromIdsMap.get(code);
    }
}
