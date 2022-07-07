package services;

import java.util.Arrays;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;

/**
 * @author KRonst
 */
public class PrivateBufferService {

    @Bypass("services.PrivateBuffer:start")
    public void start(Player player, NpcInstance npc, String[] param) {
        if (!Config.PRIVATE_BUFFER.enabled()) {
            return;
        }
        if (param.length < 2) {
            player.sendMessage(new CustomMessage("services.privatebuffer.error.params"));
            return;
        }
        long price;
        String title;
        try {
            price = Long.parseLong(param[0]);
            title = String.join(" ", Arrays.copyOfRange(param, 1, param.length));
        } catch (Exception ignored) {
            player.sendMessage(new CustomMessage("services.privatebuffer.error.params"));
            return;
        }

        if (price < Config.PRIVATE_BUFFER.minPrice()) {
            player.sendMessage(
                new CustomMessage("services.privatebuffer.error.price.min")
                    .addNumber(Config.PRIVATE_BUFFER.minPrice())
            );
            return;
        }
        if (price > Config.PRIVATE_BUFFER.maxPrice()) {
            player.sendMessage(
                new CustomMessage("services.privatebuffer.error.price.max")
                    .addNumber(Config.PRIVATE_BUFFER.maxPrice())
            );
            return;
        }
        player.getPrivateBuffer().start(price, title);
    }

    @Bypass("services.PrivateBuffer:list")
    public void showList(Player player, NpcInstance npc, String[] param) {
        Player buffer = checkBuffer(player);
        if (buffer == null) {
            return;
        }
        if (param.length == 0) {
            return;
        }
        int page = Integer.parseInt(param[0]);
        buffer.getPrivateBuffer().sendList(player, page);
    }

    @Bypass("services.PrivateBuffer:target")
    public void setTarget(Player player, NpcInstance npc, String[] param) {
        Player buffer = checkBuffer(player);
        if (buffer == null) {
            return;
        }
        String target = param[0];
        int page = Integer.parseInt(param[1]);
        buffer.getPrivateBuffer().setTarget(player, target, page);
    }

    @Bypass("services.PrivateBuffer:buff")
    public void buff(Player player, NpcInstance npc, String[] param) {
        Player buffer = checkBuffer(player);
        if (buffer == null) {
            return;
        }

        int skillId = Integer.parseInt(param[0]);
        int page = Integer.parseInt(param[1]);
        buffer.getPrivateBuffer().buff(player, skillId, page);
    }

    private Player checkBuffer(Player player) {
        if (player == null) {
            return null;
        }
        if (player.getTarget() == null) {
            return null;
        }
        Player buffer = player.getTarget().getPlayer();
        if (buffer == null) {
            return null;
        }
        if (!buffer.isPrivateBuffer()) {
            return null;
        }
        return buffer;
    }
}
