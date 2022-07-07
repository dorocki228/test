package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class ClanTraderInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	public ClanTraderInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		HtmlMessage html = new HtmlMessage(this);
		if("crp".equalsIgnoreCase(command))
		{
			if(player.getClan() != null && player.getClan().getLevel() > 4)
				html.setFile("default/" + getNpcId() + "-2.htm");
			else
				html.setFile("default/" + getNpcId() + "-1.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
