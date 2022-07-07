package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.service.MercenaryService;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author mangol
 */
public class MercenaryNpcInstance extends NpcInstance {
	public MercenaryNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace) {
		String filename = getHtmlPath(getHtmlFilename(val, player), player);
		HtmlMessage packet = new HtmlMessage(this, filename).setPlayVoice(firstTalk);
		if(replace.length % 2 == 0) {
			for(int i = 0; i < replace.length; i += 2) {
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
			}
		}
		boolean canNotTransitionForFraction = MercenaryService.getInstance().isCanNotTransitionForFraction(player);
		packet.addVar("canNotTransitionForFraction", canNotTransitionForFraction);
		player.sendPacket(packet);
	}

	@Override
	public String getHtmlDir(String filename, Player player) {
		return "gve/mercenaries/";
	}

	@Override
	public String getHtmlFilename(int val, Player player) {
		return "mercenary_index.htm";
	}
}
