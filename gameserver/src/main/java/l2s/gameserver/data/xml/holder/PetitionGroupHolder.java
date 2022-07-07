package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.petition.PetitionMainGroup;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.Collection;

public class PetitionGroupHolder extends AbstractHolder
{
	private static final PetitionGroupHolder _instance;
	private final IntObjectMap<PetitionMainGroup> _petitionGroups;

	public static PetitionGroupHolder getInstance()
	{
		return _instance;
	}

	private PetitionGroupHolder()
	{
		_petitionGroups = new HashIntObjectMap();
	}

	public void addPetitionGroup(PetitionMainGroup g)
	{
		_petitionGroups.put(g.getId(), g);
	}

	public PetitionMainGroup getPetitionGroup(int val)
	{
		return _petitionGroups.get(val);
	}

	public Collection<PetitionMainGroup> getPetitionGroups()
	{
		return _petitionGroups.values();
	}

	@Override
	public int size()
	{
		return _petitionGroups.size();
	}

	@Override
	public void clear()
	{
		_petitionGroups.clear();
	}

	static
	{
		_instance = new PetitionGroupHolder();
	}
}
