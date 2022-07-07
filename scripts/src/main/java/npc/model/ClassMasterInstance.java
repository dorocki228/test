package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class ClassMasterInstance extends MerchantInstance
{

	public ClassMasterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(firstTalk)
		{
			IVoicedCommandHandler vc = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cm");
			if(vc != null)
				vc.useVoicedCommand("cm", player, "");
		}
	}

}