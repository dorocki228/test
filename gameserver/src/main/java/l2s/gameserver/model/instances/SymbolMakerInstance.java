package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.HennaEquipListPacket;
import l2s.gameserver.network.l2.s2c.HennaUnequipListPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	public SymbolMakerInstance(int objectID, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectID, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if("Draw".equals(command))
			player.sendPacket(new HennaEquipListPacket(player));
		else if("RemoveList".equals(command))
			player.sendPacket(new HennaUnequipListPacket(player));
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "symbolmaker/";
	}

	@Override
	public String getHtmlFilename(int val, Player player)
	{
		if(val == 0)
			return "SymbolMaker.htm";
		return "SymbolMaker-" + val + ".htm";
	}
}
