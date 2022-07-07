package l2s.gameserver.model.instances;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.service.CasinoEventService;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Functions;

/**
 * @author KRonst
 */
public class CasinoManagerInstance extends NpcInstance {

    private ScheduledFuture<?> phraseTask = null;

    public CasinoManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace) {
        CasinoEventService.getInstance().showWelcomeHtml(player);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        phraseTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> {
            CustomMessage message = new CustomMessage("services.casino.manager.phrase");
            Functions.npcSayCustomInRange(this, message, 5000);
        }, 60, 420, TimeUnit.SECONDS);
    }

    @Override
    protected void onDelete() {
        if (phraseTask != null) {
            phraseTask.cancel(false);
            phraseTask = null;
        }
        super.onDelete();
    }
}
