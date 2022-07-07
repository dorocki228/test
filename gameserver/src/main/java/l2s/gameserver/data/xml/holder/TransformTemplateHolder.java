package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.templates.player.transform.TransformTemplate;

public final class TransformTemplateHolder extends AbstractHolder
{
	private static final TransformTemplateHolder _instance;
	private final TIntObjectMap<TIntObjectMap<TransformTemplate>> _templates;

	public TransformTemplateHolder()
	{
		_templates = new TIntObjectHashMap<>();
		for(Sex sex : Sex.VALUES)
			_templates.put(sex.ordinal(), new TIntObjectHashMap<>());
	}

	public static TransformTemplateHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(Sex sex, TransformTemplate template)
	{
		((TIntObjectMap) _templates.get(sex.ordinal())).put(template.getId(), template);
	}

	public TransformTemplate getTemplate(Sex sex, int id)
	{
		return (TransformTemplate) ((TIntObjectMap) _templates.get(sex.ordinal())).get(id);
	}

	@Override
	public int size()
	{
		int size = 0;
		for(Sex sex : Sex.VALUES)
			size += _templates.get(sex.ordinal()).size();
		return size;
	}

	@Override
	public void clear()
	{
		_templates.clear();
	}

	static
	{
		_instance = new TransformTemplateHolder();
	}
}
