package handler.voicecommands;

import events.CaptureTeamFlagEvent;
import events.CustomInstantTeamEvent;
import events.TeamVsTeamEvent;

import java.util.Objects;
import java.util.stream.StreamSupport;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.LiberationFortressEvent;
import l2s.gameserver.model.entity.events.impl.PvpArenaEvent;
import l2s.gameserver.model.entity.events.impl.UpgradingEvent;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;

public class EventRegister implements IVoicedCommandHandler, OnInitScriptListener {
    private String[] _commandList = {"tvt", "ctf", "upgrading", "unreg", "pvparena", "liberation"};

    @Override
    public void onInit() {
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(String command, Player player, String args) {
        if ("tvt".equalsIgnoreCase(command)) {
            TeamVsTeamEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 500);
            if (event == null)
                return false;

            player.sendPacket(new HtmlMessage(0).setFile(event.registerPlayer(player) ? "events/tvt_reg_succ.htm" : "events/tvt_reg_fail.htm"));
        } else if ("ctf".equalsIgnoreCase(command)) {
            CaptureTeamFlagEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 600);
            if (event == null)
                return false;

            player.sendPacket(new HtmlMessage(0).setFile(event.registerPlayer(player) ? "events/ctf_reg_succ.htm" : "events/ctf_reg_fail.htm"));
        } else if ("upgrading".equalsIgnoreCase(command)) {
            UpgradingEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1001);
            if (event == null) {
                return false;
            }
            if (!event.isInProgress()) {
                player.sendMessage(new CustomMessage("events.upgrading.teleport.failed"));
                return false;
            } else {
                event.teleportPlayerToEvent(player);
            }
        } else if ("pvparena".equalsIgnoreCase(command)) {
            PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
            if (event == null) {
                return false;
            }
            boolean registerPlayer = event.registerPlayer(player);
            if (registerPlayer) {
                player.sendMessage(new CustomMessage("event.registration.success"));
            }
        } else if ("liberation".equalsIgnoreCase(command)) {
            LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
            if (event == null) {
                return false;
            }
            boolean registerPlayer = event.registerPlayer(player);
            if (registerPlayer) {
                player.sendMessage(new CustomMessage("event.registration.success"));
            }
        } else if ("unreg".equalsIgnoreCase(command)) {
            var events = EventHolder.getInstance().getEvents(CustomInstantTeamEvent.class);
            var eventOptional = events.stream()
                    .filter(event ->
                    {
                        var players = StreamSupport.stream(event.spliterator(), false);
                        return players.map(EventPlayerObject::getPlayer)
                                .filter(Objects::nonNull)
                                .anyMatch(eventPlayer -> Objects.equals(player, eventPlayer));
                    })
                    .findAny();

            if (eventOptional.isEmpty()) {
                return unregEvents(player);
            } else {
                unregCustomTeamEvent(player, eventOptional.get());
            }
        }

        return true;
    }


    private void unregCustomTeamEvent(Player player, CustomInstantTeamEvent event) {
        if (player == null || event == null) {
            return;
        }
        String html = event.unregisterPlayer(player) ? "events/unreg_succ.htm" : "events/unreg_fail.htm";
        player.sendPacket(new HtmlMessage(0).setFile(html));
    }

    private boolean unregEvents(Player player) {
        PvpArenaEvent arenaEvent = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
        LiberationFortressEvent liberationFortressEvent = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
        if (player == null || (arenaEvent == null && liberationFortressEvent == null)) {
            return false;
        }
        boolean unregisterPlayer = false;
        if (arenaEvent != null) {
            unregisterPlayer = arenaEvent.unregisterPlayer(player);
        }
        if (liberationFortressEvent != null) {
            unregisterPlayer |= liberationFortressEvent.unregisterPlayer(player);
        }
        String html = unregisterPlayer ? "events/unreg_succ.htm" : "events/unreg_fail.htm";
        player.sendPacket(new HtmlMessage(0).setFile(html));
        return unregisterPlayer;
    }
}
