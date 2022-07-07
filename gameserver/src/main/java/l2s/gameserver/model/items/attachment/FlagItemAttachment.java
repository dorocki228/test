package l2s.gameserver.model.items.attachment;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment
{
	void onLogout(Player p0);

	void onDeath(Player p0, Creature p1);

	void onLeaveSiegeZone(Player p0);

	boolean canAttack(Player p0);

	boolean canCast(Player p0, Skill p1);
}
