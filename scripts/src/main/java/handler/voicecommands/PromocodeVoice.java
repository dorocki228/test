package handler.voicecommands;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.coupon.ShowPCCafeCouponShowUI;

import java.util.Objects;

/**
 * @author KRonst 10.03.2018
 */
public class PromocodeVoice implements IVoicedCommandHandler, OnInitScriptListener
{
    private final String[] _commandList = {"code"};

    @Override
    public String[] getVoicedCommandList()
    {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(String command, Player player, String args)
    {
        if(Objects.equals(command, "code"))
        {
            player.sendPacket(ShowPCCafeCouponShowUI.INSTANCE);
        }

        return false;
    }

    @Override
    public void onInit()
    {
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }
}
