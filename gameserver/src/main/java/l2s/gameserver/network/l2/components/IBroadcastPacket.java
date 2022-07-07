package l2s.gameserver.network.l2.components;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public interface IBroadcastPacket {
    L2GameServerPacket packet(Player p0);

    default boolean isInPacketRange(final Creature sender, final Player recipient) {
        return true;
    }
}
