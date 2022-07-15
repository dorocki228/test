package l2s.gameserver.model.actor.flags;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.flags.flag.DefaultFlag;

/**
 * @author Bonux
**/
public final class PlayerFlags extends PlayableFlags
{
	private final DefaultFlag _partyBlocked = new DefaultFlag(); // Запрещаем вступление\создание группы.
	private final DefaultFlag _violetBoy = new DefaultFlag(); // Вечно фиолетовый.

	public PlayerFlags(Player owner)
	{
		super(owner);
	}

	public DefaultFlag getPartyBlocked()
	{
		return _partyBlocked;
	}

	public DefaultFlag getVioletBoy()
	{
		return _violetBoy;
	}
}