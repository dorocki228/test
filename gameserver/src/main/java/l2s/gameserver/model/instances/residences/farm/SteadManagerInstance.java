package l2s.gameserver.model.instances.residences.farm;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.component.farm.Harvest;
import l2s.gameserver.dao.SteadBarnDAO;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TeleportPoint;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.farm.SteadBarnManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;
import l2s.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.StringTokenizer;

public class SteadManagerInstance extends NpcInstance {
    private static final Logger _log = LoggerFactory.getLogger(SeedInstance.class);

    public SteadManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        String comm = tokenizer.nextToken();

        if ("slave".equalsIgnoreCase(comm)) {
            if (player.reduceAdena(SteadDataHolder.getInstance().getConfiguration().getInteger("slave_price"), true)) {
                player.setVar(SeedInstance.HAVE_SLAVE, true, System.currentTimeMillis() + TimeUtils.addMinutes(SteadDataHolder.getInstance().getConfiguration().getInteger("slave_time")));
                SteadDataHolder.getInstance().getSteads().forEach(stead -> stead.getSeeds(player).forEach(seed -> seed.updateSlave(true)));
            }
            return;
        } else if ("barn".equalsIgnoreCase(comm)) {
            if (player.reduceAdena(SteadDataHolder.getInstance().getConfiguration().getInteger("barn_price"), true)) {
                player.setVar(SeedInstance.HAVE_BARN, true, System.currentTimeMillis() + TimeUtils.addMinutes(SteadDataHolder.getInstance().getConfiguration().getInteger("barn_time")));
                SteadDataHolder.getInstance().getSteads().forEach(stead -> stead.getSeeds(player).forEach(seed -> seed.updateBarn(true)));
            }
            return;
        } else if ("use_barn".equalsIgnoreCase(comm)) {
            Collection<Harvest> barn = SteadBarnManager.getInstance().getList(player.getObjectId());
            if (barn.size() < 1) {
                if (!player.getVarBoolean(SeedInstance.HAVE_BARN))
                    player.sendPacket(new CustomMessage("farmer.string.s6"));
                else
                    player.sendPacket(new CustomMessage("farmer.string.s7"));
                return;
            }

            barn.forEach(harvest -> ItemFunctions.addItem(player, harvest.getId(), harvest.getCount()));
            barn.clear();
            SteadBarnDAO.getInstance().clear(player.getObjectId());
            return;
        } else if ("teleport".equalsIgnoreCase(comm)) {
            TeleportPoint loc = TeleportUtils.getRestartPoint(player, RestartType.TO_VILLAGE);
            player.teleToLocation(loc.getLoc());
            return;
        } else if ("goto".equalsIgnoreCase(comm)) {
            final int[] teleport = SteadDataHolder.getInstance().getConfiguration().getIntegerArray("possession_" + tokenizer.nextToken(), " ");
            final Location location = new Location(teleport[0], teleport[1], teleport[2]);
            player.teleToLocation(location);
            return;
        }

        super.onBypassFeedback(player, command);
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg) {
        HtmlMessage html = new HtmlMessage(this, "stead/manager.htm");
        html.addVar("slave", player.getVarBoolean(SeedInstance.HAVE_SLAVE));
        html.addVar("barn", player.getVarBoolean(SeedInstance.HAVE_BARN));
        html.addVar("barn_expire", TimeUtils.dateTimeFormat(Instant.ofEpochMilli(player.getVarExpireTime(SeedInstance.HAVE_BARN))));
        html.addVar("storage", SteadBarnManager.getInstance().getList(player.getObjectId()));
        html.addVar("player", player);
        html.addVar("holder", SteadDataHolder.getInstance().getSteads());
        player.sendPacket(html.setPlayVoice(firstTalk));
    }
}