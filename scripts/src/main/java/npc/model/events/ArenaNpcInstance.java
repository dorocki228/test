package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author mangol
 */
public class ArenaNpcInstance extends NpcInstance {
	public ArenaNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public String getHtmlDir(String filename, Player player) {
		return "gve/pvparena/";
	}

	@Override
	public String getHtmlFilename(int val, Player player) {
		return "pvparena_welcome.htm";
	}
}
