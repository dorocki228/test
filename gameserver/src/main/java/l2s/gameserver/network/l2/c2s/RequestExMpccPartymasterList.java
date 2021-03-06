package l2s.gameserver.network.l2.c2s;

import java.util.HashSet;
import java.util.Set;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.s2c.ExMpccPartymasterList;

/**
 * @author VISTALL
 */
public class RequestExMpccPartymasterList implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.CC_MATCHING)
			return;

		Set<String> set = new HashSet<String>();
		for(Player $member : room.getPlayers())
			if($member.getParty() != null)
				set.add($member.getParty().getPartyLeader().getName());

		player.sendPacket(new ExMpccPartymasterList(set));
	}
}