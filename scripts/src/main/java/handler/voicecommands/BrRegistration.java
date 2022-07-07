package handler.voicecommands;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;

/**
 * @author : Nami
 * @date : 08.08.2018
 * @time : 17:46
 * <p/>
 */
public class BrRegistration implements IVoicedCommandHandler, OnInitScriptListener
{
    private static final String[] COMMANDS = new String[]{"joinbr", "leavebr"};

    @Override
    public void onInit()
    {
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }

    @Override
    public boolean useVoicedCommand(String command, Player player, String args) {
        if (command.equalsIgnoreCase("joinbr")) {
            BREvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 700);
            if(event == null)
                return false;

            if(event.registerPlayer(player)) {
                player.sendMessage("You have registered in Battle Royal event");
                return true;
            }
        } else if (command.equalsIgnoreCase("leavebr")) {
            BREvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 700);
            if(event == null)
                return false;

            if(event.unregisterPlayer(player))
            {
                player.sendMessage("You have unregistered from Battle Royal event");
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        return COMMANDS;
    }
}