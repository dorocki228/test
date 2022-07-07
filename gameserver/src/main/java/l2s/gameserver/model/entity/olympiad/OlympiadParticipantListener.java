package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.Config;
import l2s.gameserver.listener.actor.player.OnLevelChangeListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Player;

public class OlympiadParticipantListener implements OnPlayerEnterListener, OnLevelChangeListener
{
	public static final OlympiadParticipantListener LISTENER = new OlympiadParticipantListener();

	@Override
	public void onPlayerEnter(Player player)
	{
		if(player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL && player.getClassId().getClassLevel().ordinal() >= 2)
			Olympiad.addParticipant(player);

	}

	@Override
	public void onLevelChange(Player player, int oldLvl, int newLvl)
	{
		if(newLvl >= Config.OLYMPIAD_MIN_LEVEL && player.getClassId().getClassLevel().ordinal() >= 2)
			Olympiad.addParticipant(player);
	}
}
