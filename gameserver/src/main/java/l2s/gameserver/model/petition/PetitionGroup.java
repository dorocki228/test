package l2s.gameserver.model.petition;

import l2s.gameserver.utils.Language;

import java.util.HashMap;
import java.util.Map;

public abstract class PetitionGroup
{
	private final Map<Language, String> _name;
	private final Map<Language, String> _description;
	private final int _id;

	public PetitionGroup(int id)
	{
		_name = new HashMap<>(Language.VALUES.length);
		_description = new HashMap<>(Language.VALUES.length);
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public String getName(Language lang)
	{
		return _name.get(lang);
	}

	public void setName(Language lang, String name)
	{
		_name.put(lang, name);
	}

	public String getDescription(Language lang)
	{
		return _description.get(lang);
	}

	public void setDescription(Language lang, String name)
	{
		_description.put(lang, name);
	}
}
