package l2s.gameserver.model.instances.residences.farm;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.farm.zone.SteadZone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.StringTokenizer;

public class SteadScarecrowInstance extends NpcInstance {
    private static final Logger _log = LoggerFactory.getLogger(SeedInstance.class);

    public SteadScarecrowInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        String comm = tokenizer.nextToken();

        if ("upgrade".equalsIgnoreCase(comm)) {
            if (player.reduceAdena(SteadDataHolder.getInstance().getConfiguration().getInteger("scarecrow_price"), true)) {
                player.setVar(SeedInstance.SCARECROW_VAR, true, System.currentTimeMillis() + TimeUtils.addMinutes(SteadDataHolder.getInstance().getConfiguration().getInteger("scarecrow_time")));
                player.removeVisibleObject(this, null);
                player.addVisibleObject(this, null);
                SteadZone.class.cast(player.getZone(Zone.ZoneType.STEAD)).getStead().getSeeds(player).forEach(SeedInstance::useScarecrow);
            }
        } else
            super.onBypassFeedback(player, command);
    }

    // Костыль для подмены внешнего вида пугола для избежания создания хуилиона нпц под каждого игрока
    @Override
    public int getDisplayId(Creature creature) {
        if (creature.isPlayer())
            return creature.getPlayer().getVarBoolean(SeedInstance.SCARECROW_VAR) ? 40815 : 40814;
        return super.getDisplayId();
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg) {
        HtmlMessage html = new HtmlMessage(this, "stead/scarecrow.htm");
        html.addVar("upgrade", player.getVarBoolean(SeedInstance.SCARECROW_VAR));
        html.addVar("scarecrow_expire", TimeUtils.dateTimeFormat(Instant.ofEpochMilli(player.getVarExpireTime(SeedInstance.SCARECROW_VAR))));
        player.sendPacket(html.setPlayVoice(firstTalk));
    }
}