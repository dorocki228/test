package handler.voicecommands;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.TimeUtils;

import java.time.Instant;

public class Premium implements IVoicedCommandHandler, OnInitScriptListener
{
    private String[] _commandList = { "premium" };

    @Override
    public void onInit()
    {
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(String command, Player player, String args)
    {
        if(!"premium".equalsIgnoreCase(command))
        {
            return false;
        }

        var target = player.getTarget();
        if(target == null || !target.isPlayer())
            target = player;
        var targetPlayer = target.getPlayer();

        var connection = targetPlayer.getNetConnection();

        if(connection.getPremiumAccountType() == 0)
        {
            String message;
            if(player.equals(targetPlayer))
                message = player.isLangRus()
                        ? "На этои персонаже не активен Премиум Аккаунт. Вы можете приобрести его у NPC Donate."
                        : "Premium Account is not active on this character. You can purchase it from NPC Donate.";
            else
                message = player.isLangRus()
                        ? "На этои персонаже не активен Премиум Аккаунт."
                        : "Premium Account is not active on this character.";
            player.sendMessage(message);

            return true;
        }

        String message;
        if(player.equals(targetPlayer))
            message = player.isLangRus()
                    ? "Ваш %pa_type% Премиум Аккаунт будет действителен до: %date%"
                    : "Your %pa_type% Premium Account will be valid until: %date%.";
        else
            message = player.isLangRus()
                    ? "%target_name% %pa_type% Премиум Аккаунт будет действителен до: %date%"
                    : "%target_name% %pa_type% Premium Account will be valid until: %date%.";

        if(!player.equals(targetPlayer))
            message = message.replaceFirst("%target_name%", targetPlayer.getName());
        message = message.replaceFirst("%pa_type%",
                targetPlayer.getPremiumAccount().getName(player.getLanguage()));
        var endDate = Instant.ofEpochSecond(connection.getPremiumAccountExpire());
        var endString = TimeUtils.dateTimeFormat(endDate);
        message = message.replaceFirst("%date%", endString);

        player.sendMessage(message);

        return true;
    }
}