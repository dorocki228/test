package npc.model.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class SiegeTeleporterInstance extends NpcInstance
{
	private Castle _castle;

	public SiegeTeleporterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		switch (getNpcId()) {
			case 40050: {
				_castle = ResidenceHolder.getInstance().getResidence(Castle.class, 4);
				break;
			}
			case 40051: {
				_castle = ResidenceHolder.getInstance().getResidence(Castle.class, 10);
				break;
			}
			case 41924: {
				_castle = ResidenceHolder.getInstance().getResidence(Castle.class, 8);
				break;
			}
			case 41925: {
				_castle = ResidenceHolder.getInstance().getResidence(Castle.class, 9);
				break;
			}
		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(player.getLevel() < 62)
			return;
		if(command.startsWith("goSiege"))
		{
			if (_castle != null) {
				Location loc = _castle.getRestartPoint(player);
				if(loc != null)
					player.teleToLocation(loc);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}
