package l2s.gameserver.config.templates;

import java.util.regex.Pattern;

public class SpamRule
{

	private final Pattern _pattern;
	private final int _penalty;

	public SpamRule(Pattern pattern, int penalty)
	{
		_pattern = pattern;
		_penalty = penalty;
	}

	public int getPenalty()
	{
		return _penalty;
	}

	public Pattern getPattern()
	{
		return _pattern;
	}
}
