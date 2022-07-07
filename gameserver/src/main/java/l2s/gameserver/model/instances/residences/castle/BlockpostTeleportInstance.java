package l2s.gameserver.model.instances.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.instances.TeleporterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

public class BlockpostTeleportInstance extends TeleporterInstance
{

	public BlockpostTeleportInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}
}