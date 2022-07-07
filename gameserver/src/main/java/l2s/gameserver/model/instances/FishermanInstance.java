package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public class FishermanInstance extends MerchantInstance
{
	public FishermanInstance(int objectID, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectID, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		String fileName = "fisherman/manager.htm";
		player.sendPacket(new HtmlMessage(this, fileName).setPlayVoice(firstTalk));
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if("FishingSkillList".equals(command))
			showFishingSkillList(player);
		else
			super.onBypassFeedback(player, command);
	}
}
