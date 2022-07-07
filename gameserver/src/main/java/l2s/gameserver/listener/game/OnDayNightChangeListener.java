package l2s.gameserver.listener.game;

import l2s.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener
{
	void onDay();

	void onNight();
}
