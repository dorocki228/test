package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Этот пакет отвечает за анимацию высасывания душ из трупов
 * @author SYS
 */
public class ExSpawnEmitter implements IClientOutgoingPacket
{
	private int _targetObjId;
	private int _killerObjId;
	private int _type;

	public ExSpawnEmitter(Creature target, Creature killer, int type)
	{
		_targetObjId = target.getObjectId();
		_killerObjId = killer.getObjectId();
		_type = type;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SPAWN_EMITTER.writeId(packetWriter);
		//ddd
		packetWriter.writeD(_targetObjId);
		packetWriter.writeD(_killerObjId);
		packetWriter.writeD(_type); //unk

		return true;
	}
}