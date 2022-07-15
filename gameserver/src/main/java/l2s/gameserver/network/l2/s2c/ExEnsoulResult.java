package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.item.support.Ensoul;

/**
 * @author Bonux
**/
public class ExEnsoulResult implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket FAIL = new ExEnsoulResult();

	private final boolean _success;
	private final Ensoul[] _normalEnsouls;
	private final Ensoul[] _specialEnsouls;

	private ExEnsoulResult()
	{
		_success = false;
		_normalEnsouls = null;
		_specialEnsouls = null;
	}

	public ExEnsoulResult(Ensoul[] normalEnsouls, Ensoul[] specialEnsouls)
	{
		_success = true;
		_normalEnsouls = normalEnsouls;
		_specialEnsouls = specialEnsouls;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ENSOUL_RESULT.writeId(packetWriter);
		packetWriter.writeC(_success);
		if(_success)
		{
			packetWriter.writeC(_normalEnsouls.length);
			for(Ensoul ensoul : _normalEnsouls)
				packetWriter.writeD(ensoul.getId());

			packetWriter.writeC(_specialEnsouls.length);
			for(Ensoul ensoul : _specialEnsouls)
				packetWriter.writeD(ensoul.getId());
		}

		return true;
	}
}