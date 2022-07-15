package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExCursedWeaponList implements IClientOutgoingPacket
{
	private int[] cursedWeapon_ids;

	public ExCursedWeaponList()
	{
		// TODO cursedWeapon_ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CURSED_WEAPON_LIST.writeId(packetWriter);
		packetWriter.writeDD(cursedWeapon_ids, true);

		return true;
	}
}