package l2s.gameserver.model.instances.residences.fortress;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public abstract class UpgradeFortressInstance extends NpcInstance {

    protected static final int COND_OWNER = 0;
    static final int COND_SIEGE = 1;
    static final int COND_FAIL = 2;

    private String _siegeDialog;
    private String _mainDialog;
    private String _failDialog;

    UpgradeFortressInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
        setDialogs();
    }

    private void setDialogs() {
        _siegeDialog = getTemplate().getAIParams().getString("siege_dialog");
        _mainDialog = getTemplate().getAIParams().getString("main_dialog");
        _failDialog = getTemplate().getAIParams().getString("fail_dialog");
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg) {
        String filename = null;
        int cond = getCond(player);
        switch (cond) {
            case COND_OWNER:
                filename = _mainDialog;
                break;
            case COND_SIEGE:
                filename = _siegeDialog;
                break;
            case COND_FAIL:
                filename = _failDialog;
                break;
        }

        final HtmlMessage html = getHtml(player, filename);
        player.sendPacket(html.setPlayVoice(firstTalk));
    }

    protected HtmlMessage getHtml(Player player, String filename) {
        return new HtmlMessage(this, filename);
    }

    protected abstract int getCond(Player player);

    protected Residence getResidence() {
        return ResidenceHolder.getInstance().getResidenceByCoord(Fortress.class, getX(), getY(), getZ(), getReflection());
    }
}
