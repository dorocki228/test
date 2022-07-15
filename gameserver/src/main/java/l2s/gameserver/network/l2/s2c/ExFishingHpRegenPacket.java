package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Format (ch)dddcccd
 * d: cahacter oid
 * d: time left
 * d: fish hp
 * c:
 * c:
 * c: 00 if fish gets damage 02 if fish regens
 * d:
 */
public class ExFishingHpRegenPacket implements IClientOutgoingPacket
{
	private int _time, _fishHP, _HPmode, _Anim, _GoodUse, _Penalty, _hpBarColor;
	private int char_obj_id;

	public ExFishingHpRegenPacket(Creature character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
	{
		char_obj_id = character.getObjectId();
		_time = time;
		_fishHP = fishHP;
		_HPmode = HPmode;
		_GoodUse = GoodUse;
		_Anim = anim;
		_Penalty = penalty;
		_hpBarColor = hpBarColor;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FISHING_HP_REGEN.writeId(packetWriter);
		packetWriter.writeD(char_obj_id);
		packetWriter.writeD(_time);
		packetWriter.writeD(_fishHP);
		packetWriter.writeC(_HPmode); // 0 = HP stop, 1 = HP raise
		packetWriter.writeC(_GoodUse); // 0 = none, 1 = success, 2 = failed
		packetWriter.writeC(_Anim); // Anim: 0 = none, 1 = reeling, 2 = pumping
		packetWriter.writeD(_Penalty); // Penalty
		packetWriter.writeC(_hpBarColor); // 0 = normal hp bar, 1 = purple hp bar

		return true;

	}
}