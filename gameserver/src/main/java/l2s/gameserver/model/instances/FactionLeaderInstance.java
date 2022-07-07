package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FactionLeaderInstance extends NpcInstance {
    public FactionLeaderInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace) {
        FactionLeaderService.getInstance().sendMain(player);
    }
}
