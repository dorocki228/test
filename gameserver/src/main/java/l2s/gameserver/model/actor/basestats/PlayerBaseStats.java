package l2s.gameserver.model.actor.basestats;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class PlayerBaseStats extends PlayableBaseStats
{
	public PlayerBaseStats(Player owner)
	{
		super(owner);
	}

	@Override
	public Player getOwner()
	{
		return (Player) _owner;
	}

	@Override
	public double getCollisionRadius()
	{
		if(getOwner().isMounted())
		{
			final int mountTemplate = getOwner().getMountNpcId();
			if(mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if(mountNpcTemplate != null)
					return mountNpcTemplate.getCollisionRadius();
			}
		}

		if(getOwner().isVisualTransformed() && getOwner().getVisualTransform().getCollisionRadius() > 0)
			return getOwner().getVisualTransform().getCollisionRadius();

		return getOwner().getBaseTemplate().getCollisionRadius();
	}

	@Override
	public double getCollisionHeight()
	{
		if(getOwner().isMounted())
		{
			final int mountTemplate = getOwner().getMountNpcId();
			if(mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if(mountNpcTemplate != null)
					return mountNpcTemplate.getCollisionHeight();
			}
		}

		if(getOwner().isVisualTransformed() && getOwner().getVisualTransform().getCollisionHeight() > 0)
			return getOwner().getVisualTransform().getCollisionHeight();

		return getOwner().getBaseTemplate().getCollisionHeight();
	}
}