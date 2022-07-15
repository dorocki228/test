package l2s.gameserver.templates.skill.restoration;

import java.util.List;

/**
 * @author Bonux
 */
public final class RestorationGroup
{
	private final double _chance;
	private final List<RestorationItem> _restorationItems;

	public RestorationGroup(double chance, List<RestorationItem> restorationItems)
	{
		_chance = chance;
		_restorationItems = restorationItems;
	}

	public double getChance()
	{
		return _chance;
	}

	public List<RestorationItem> getRestorationItems()
	{
		return _restorationItems;
	}
}
