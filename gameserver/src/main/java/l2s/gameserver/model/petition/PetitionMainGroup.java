package l2s.gameserver.model.petition;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.Collection;

public class PetitionMainGroup extends PetitionGroup
{
	private final IntObjectMap<PetitionSubGroup> _subGroups;

	public PetitionMainGroup(int id)
	{
		super(id);
		_subGroups = new HashIntObjectMap<>();
	}

	public void addSubGroup(PetitionSubGroup subGroup)
	{
		_subGroups.put(subGroup.getId(), subGroup);
	}

	public PetitionSubGroup getSubGroup(int val)
	{
		return _subGroups.get(val);
	}

	public Collection<PetitionSubGroup> getSubGroups()
	{
		return _subGroups.values();
	}
}
