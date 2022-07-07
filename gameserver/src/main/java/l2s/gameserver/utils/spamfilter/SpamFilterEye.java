package l2s.gameserver.utils.spamfilter;

import l2s.gameserver.Config;
import l2s.gameserver.config.templates.SpamRule;
import l2s.gameserver.config.xml.holder.SpamFilterConfigHolder;
import l2s.gameserver.model.Player;

import java.util.regex.Matcher;

/**
 * @author KanuToIIIKa
 */

public class SpamFilterEye
{

	private final int _playerObjId;
	private final String _playerName;
	private int _penalty;

	private String _currentText;
	private String _lastText;
	private long _lastDelay;
	private long _lastUse;
	private int _counter;

	public SpamFilterEye(Player player)
	{
		_playerObjId = player.getObjectId();
		_playerName = player.getName();
	}

	public boolean isSpam(String text)
	{
		_penalty = 0;
		_currentText = text.toLowerCase().replaceAll("\\\\", "");

		checkRules();
		checkDelay();

		return _penalty == -1 || _penalty >= Config.SPAM_FILTER_PENALTIES_TO_SPAM;
	}

	private void checkDelay()
	{
		if(_lastText == null || !_lastText.equalsIgnoreCase(_currentText))
		{
			_lastUse = System.currentTimeMillis();
			_lastText = _currentText;
			_counter = 0;
			return;
		}

		long delay = (System.currentTimeMillis() - _lastUse) / 1000L;

		if(delay != _lastDelay)
		{
			_lastUse = System.currentTimeMillis();
			_lastDelay = delay;
			_counter = 0;
			return;
		}

		_lastUse = System.currentTimeMillis();
		_counter += 1;
		if(Config.SPAM_FILTER_MESSAGES_TO_SPAM > 0 && _counter >= Config.SPAM_FILTER_MESSAGES_TO_SPAM)
			_penalty = -1;

	}

	private void checkRules()
	{
		for(SpamRule rule : SpamFilterConfigHolder.getInstance().getRules())
		{
			if(_penalty >= Config.SPAM_FILTER_PENALTIES_TO_SPAM)
				break;

			Matcher matcher = rule.getPattern().matcher(_currentText);
			if(matcher.find())
				_penalty += rule.getPenalty();
		}
	}

	public int getPlayerObjectId()
	{
		return _playerObjId;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public int getPenalty()
	{
		return _penalty;
	}
}
