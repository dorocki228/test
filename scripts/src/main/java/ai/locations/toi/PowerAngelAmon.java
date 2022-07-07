package ai.locations.toi;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;

public class PowerAngelAmon extends Fighter
{
	private final int ENERGY = 49685;

	public PowerAngelAmon(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);

		if(killer.isPlayable())
		{
			ItemInstance energy = ItemFunctions.createItem(ENERGY);
			energy.dropToTheGround(killer.getPlayer(), getActor());
		}
	}

}
