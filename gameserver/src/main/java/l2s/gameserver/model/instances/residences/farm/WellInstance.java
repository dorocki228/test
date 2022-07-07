package l2s.gameserver.model.instances.residences.farm;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.farm.Stead;
import l2s.gameserver.model.farm.zone.SteadZone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class WellInstance extends NpcInstance {
    public WellInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        String comm = tokenizer.nextToken();

        if ("pour".equalsIgnoreCase(comm)) {
            List<SeedInstance> seeds = getSeeds(player);
            if (seeds.size() > 0) {
                List<SeedInstance> dried = seeds.stream().filter(seed -> seed.isDried() && seed.calcProgression() < 150).collect(Collectors.toList());
                if (dried.size() > 0 && player.reduceAdena(SteadDataHolder.getInstance().getConfiguration().getInteger("pour_water") * dried.size(), true)) {
                    dried.forEach(seed -> {
                        seed.pourWater();
                        player.sendPacket(new MagicSkillUse(this, seed, 1235, 1, 1500, 0));
                    });
                }
            }
            return;
        }

        super.onBypassFeedback(player, command);
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg) {
        HtmlMessage html = new HtmlMessage(this, "stead/well.htm");
        List<SeedInstance> seeds = getSeeds(player);
        List<SeedInstance> dried = seeds.stream().filter(seed -> seed.isDried() && seed.calcProgression() < 150).collect(Collectors.toList());
        html.addVar("seeds", seeds);
        html.addVar("dried", dried);
        html.addVar("price", SteadDataHolder.getInstance().getConfiguration().getInteger("pour_water"));
        player.sendPacket(html.setPlayVoice(firstTalk));
    }

    private List<SeedInstance> getSeeds(Player player) {
        Zone zone = player.getZone(Zone.ZoneType.STEAD);
        if (zone == null) {
            return Collections.emptyList();
        }
        SteadZone steadZone = SteadZone.class.cast(zone);
        Stead stead = steadZone.getStead();
        if (stead == null) {
            return Collections.emptyList();
        }
        return stead.getSeeds(player);
    }
}
