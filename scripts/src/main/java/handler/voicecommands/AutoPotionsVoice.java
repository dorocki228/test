package handler.voicecommands;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import org.apache.commons.lang3.math.NumberUtils;

/*
 * @author Ro0TT
 * @date 25.06.2015
 */

public class AutoPotionsVoice implements OnInitScriptListener, IVoicedCommandHandler
{
    @Override
    public boolean useVoicedCommand(String command, Player player, String args)
    {
        if (!Config.ACP_ENABLED)
        {
            player.sendMessage(new CustomMessage("common.Disabled"));
            return false;
        }

        if (!AutoUsePotions.checkRestricts(player))
        {
            player.sendMessage(new CustomMessage("handler.voicecommands.autopotionsvoice.check.false"));
            return false;
        }

        HtmlMessage html = new HtmlMessage(0);
        html.setFile("command/auto_potions.htm");

        if (!args.isEmpty())
        {
            String[] param = args.split(" ");
            if (param.length == 2)
            {
                if (param[0].contains("enable"))
                {
                    if ("on".equalsIgnoreCase(param[1]))
                    {
                        player.setVar(param[0], "true", -1L);
                        AutoUsePotions.getInstance().enablePlayer(player);
                    }
                    else if ("of".equalsIgnoreCase(param[1]))
                    {
                        player.unsetVar(param[0]);
                        if (!AutoUsePotions.checkNesesary(player))
                            AutoUsePotions.getInstance().disblePlayer(player);
                    }
                }
                if (param[0].contains("percent"))
                {
                    int percent = NumberUtils.toInt(param[1], 90);
                    player.setVar(param[0], percent, -1L);
                }
            }
        }

        //html.replace("player", activeChar);
        String off = "<font color=FF0000>Disabled</font>";
        String on = "<font color=00FF00>Enabled</font>";
        html.replace("%enabled_cp%", player.getVarBoolean(AutoUsePotions.acpcp_enable, false) ? on : off);
        html.replace("%enabled_hp%", player.getVarBoolean(AutoUsePotions.acphp_enable, false) ? on : off);
        html.replace("%enabled_mp%", player.getVarBoolean(AutoUsePotions.acpmp_enable, false) ? on : off);
        html.replace("%percent_cp%", String.valueOf(Math.max(0, player.getVarInt(AutoUsePotions.acpcp_percent, 90))));
        html.replace("%percent_hp%", String.valueOf(Math.max(0, player.getVarInt(AutoUsePotions.acphp_percent, 90))));
        html.replace("%percent_mp%", String.valueOf(Math.max(0, player.getVarInt(AutoUsePotions.acpmp_percent, 90))));
        String btnCpOff = "<button value=\"Turn Off\" action=\"bypass -h user_acp acpcp_enable of\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
        String btnCpOn = "<button value=\"Turn On\" action=\"bypass -h user_acp acpcp_enable on\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
        html.replace("%buttonCP%", player.getVarBoolean(AutoUsePotions.acpcp_enable, false) ? btnCpOff : btnCpOn);
        String btnHpOff = "<button value=\"Turn Off\" action=\"bypass -h user_acp acphp_enable of\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
        String btnHpOn = "<button value=\"Turn On\" action=\"bypass -h user_acp acphp_enable on\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
        html.replace("%buttonHP%", player.getVarBoolean(AutoUsePotions.acphp_enable, false) ? btnHpOff : btnHpOn);
        String btnMpOff = "<button value=\"Turn Off\" action=\"bypass -h user_acp acpmp_enable of\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
        String btnMpOn = "<button value=\"Turn On\" action=\"bypass -h user_acp acpmp_enable on\" width=80 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
        html.replace("%buttonMP%", player.getVarBoolean(AutoUsePotions.acpmp_enable, false) ? btnMpOff : btnMpOn);

        player.sendPacket(html);
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return new String[]{ AutoUsePotions.acp_command};
    }

    @Override
    public void onInit()
    {
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }
}
