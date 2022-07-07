package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
	private final int _summonObjectId;
	private final int _ownerObjectId;
	private final int _type;
	private final String _summonName;

	public ExPartyPetWindowDelete(Servitor summon)
	{
		_summonObjectId = summon.getObjectId();
		_summonName = summon.getName();
		_type = summon.getServitorType();
		_ownerObjectId = summon.getPlayer().getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_summonObjectId);
        writeD(_type);
        writeD(_ownerObjectId);
		writeS(_summonName);
	}
}
