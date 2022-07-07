package l2s.gameserver.config.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.config.templates.SpamRule;

import java.util.ArrayList;
import java.util.List;

public final class SpamFilterConfigHolder extends AbstractHolder
{
	private static final SpamFilterConfigHolder _instance = new SpamFilterConfigHolder();

	private final List<SpamRule> _rules = new ArrayList<>();

	public static SpamFilterConfigHolder getInstance()
	{
		return _instance;
	}

	@Override
	public int size()
	{
		return _rules.size();
	}

	@Override
	public void clear()
	{
		_rules.clear();
	}

	public void addRule(SpamRule rule)
	{
		_rules.add(rule);
	}

	public List<SpamRule> getRules()
	{
		return _rules;
	}
}
