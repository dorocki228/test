package l2s.gameserver.handler.voicecommands.impl;

import com.google.common.collect.ImmutableList;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.FactionLeaderCommandHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.utils.Language;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FactionLeaderCommands implements IVoicedCommandHandler {
    private final String[] commands;

    public FactionLeaderCommands(){
        String[] commands = FactionLeaderCommandHolder.getInstance().getTable().rowKeySet().toArray(new String[0]);
        String[] array = Arrays.copyOf(commands, commands.length + 1);
        array[commands.length] = "leader";
        this.commands = array;
    }

    @Override
    public boolean useVoicedCommand(String command, Player player, String args) {
        if(!FactionLeaderService.getInstance().isFactionLeader(player)) {
            player.sendMessage(new CustomMessage("faction.leader.s12"));
            return false;
        }
        if(!FactionLeaderService.getInstance().isAllOnlineLeaders() || !FactionLeaderService.getInstance().isLeaderTime()) {
            player.sendMessage(new CustomMessage("faction.leader.14"));
            return false;
        }
        if(command.equals("leader")) {
            listLeaderCommands(player);
            return true;
        }
        Map<Language, String> map = FactionLeaderCommandHolder.getInstance().getTable().row(command);
        if(map == null || map.isEmpty()) {
            player.sendMessage("Server Error (command is empty)");
            return false;
        }
        List<Player> players = World.getAroundPlayers(player,
                Config.LEADER.broadcastCommandRadius(), Config.LEADER.broadcastCommandRadius());
        var allPlayer = ImmutableList.<Player>builder().addAll(players).add(player).build();
        allPlayer.stream().
                filter(p -> p.getFraction() == player.getFraction()).
                forEach(p -> {
                    String message = map.getOrDefault(p.getLanguage(), "Server Error (No language message)");
                    p.sendPacket(new ExShowScreenMessage(message, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
                });
        listLeaderCommands(player);
        return true;
    }

    private void listLeaderCommands(Player player) {
        HtmlMessage message = new HtmlMessage(0);
        message.setFile("gve/leader/leader.htm");
        Map<String, String> map = FactionLeaderCommandHolder.getInstance().getTable().column(player.getLanguage());
        message.addVar("map", map);
        player.sendPacket(message);
    }

    @Override
    public String[] getVoicedCommandList() {
        return commands;
    }
}
