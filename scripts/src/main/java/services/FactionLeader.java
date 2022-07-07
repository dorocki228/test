package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.utils.Functions;

public class FactionLeader {
    @Bypass(value = "services.FactionLeader:request")
    public void request(Player player, NpcInstance npc, String[] param) {
        if(!Config.LEADER.enabled()) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        FactionLeaderService.getInstance().request(player);
    }

    @Bypass(value = "services.FactionLeader:vote")
    public void vote(Player player, NpcInstance npc, String[] param) {
        if(!Config.LEADER.enabled()) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        if(param.length != 1)
            return;
        int votedForObjId = Integer.parseInt(param[0]);
        FactionLeaderService.getInstance().vote(player, votedForObjId);
    }

    @Bypass(value = "services.FactionLeader:candidates")
    public void candidates(Player player, NpcInstance npc, String[] param) {
        if(!Config.LEADER.enabled()) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        if(param.length != 1)
            return;
        int page = Integer.parseInt(param[0]);
        FactionLeaderService.getInstance().sendCandidates(player, page);
    }

    @Bypass(value = "services.FactionLeader:main")
    public void main(Player player, NpcInstance npc, String[] param) {
        if(!Config.LEADER.enabled()) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        FactionLeaderService.getInstance().sendMain(player);
    }

    @Bypass(value = "services.FactionLeader:info")
    public void info(Player player, NpcInstance npc, String[] param) {
        if(player == null)
            return;
        if(player.getFraction() == Fraction.NONE)
            return;
        if(!Config.LEADER.enabled()) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        FactionLeaderService.getInstance().sendInfo(player);
    }
}
