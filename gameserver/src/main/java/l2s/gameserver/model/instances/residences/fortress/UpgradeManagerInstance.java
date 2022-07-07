package l2s.gameserver.model.instances.residences.fortress;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.component.fraction.FractionTreasure;
import l2s.gameserver.dao.FortressUpgradeDAO;
import l2s.gameserver.data.xml.holder.FortressUpgradeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.fortress.UpgradeData;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public class UpgradeManagerInstance extends UpgradeFortressInstance {
    public UpgradeManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        int cond = getCond(player);
        String comm = tokenizer.nextToken();
        switch (cond) {
            case COND_OWNER:
                if ("upgrade".equalsIgnoreCase(comm)) {
                    UpgradeType type = UpgradeType.valueOf(tokenizer.nextToken());
                    Fortress fortress = Fortress.class.cast(getResidence());
                    int current = fortress.getUpgrade(type);
                    if (current == FortressUpgradeHolder.getInstance().get(getResidence().getId()).size(type)) {
                        player.sendMessage("Достигнут максимальный уровень улучшения!");
                        return;
                    }

                    UpgradeData data = FortressUpgradeHolder.getInstance().get(getResidence().getId()).getData(type, current + 1);
                    long treasure = FractionTreasure.getInstance().get(player.getFraction());
                    if (treasure < data.getPrice()) {
                        player.sendMessage("Недостаточно денег в казне гильдии!");
                        return;
                    }

                    FortressUpgradeDAO.getInstance().update(fortress, type, current + 1);
                    switch (type) {
                        case GUARDIAN: {
                            FortressSiegeEvent event = getCurrentEvent();
                            if (event != null)
                                event.spawnGuardian(); // Уровень всего один потому делает спавн
                            break;
                        }
                    }
                }
                break;
            default:
                super.onBypassFeedback(player, command);
        }
    }

    private FortressSiegeEvent getCurrentEvent() {
        return getEvent(FortressSiegeEvent.class);
    }

    @Override
    protected HtmlMessage getHtml(Player player, String filename) {
        HtmlMessage html = new HtmlMessage(this, filename);
        Fortress residence = Fortress.class.cast(getResidence());
        html.addVar("fraction", residence.getFraction());
        html.addVar("clan", player.getClan());
        html.addVar("fortress", residence);
        html.addVar("upgrades", UpgradeType.values());
        html.addVar("upgrade_holder", FortressUpgradeHolder.getInstance());
        html.addVar("treasure", FractionTreasure.getInstance().get(player.getFraction()));

        return html;
    }

    protected int getCond(Player player) {
        Residence residence = getResidence();
        if (FactionLeaderService.getInstance().isFactionLeader(player)) {
            if (residence.getSiegeEvent().isInProgress())
                return COND_SIEGE;
            else
                return COND_OWNER;
        } else
            return COND_FAIL;
    }
}
