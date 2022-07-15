package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.ArrayList;
import java.util.List;

public class ExOlympiadSpelledInfoPacket implements IClientOutgoingPacket
{
	// chdd(dhd)
	private int char_obj_id = 0;
	private List<Abnormal> _effects;

	static class Abnormal
	{
		int skillId;
		int dat;
		int abnormalType;
		int duration;

		public Abnormal(int skillId, int dat, int abnormalType, int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.abnormalType = abnormalType;
			this.duration = duration;
		}
	}

	public ExOlympiadSpelledInfoPacket()
	{
		_effects = new ArrayList<Abnormal>();
	}

	public void addEffect(int skillId, int dat, int abnormalType, int duration)
	{
		_effects.add(new Abnormal(skillId, dat, abnormalType, duration));
	}

	public void addSpellRecivedPlayer(Player cha)
	{
		if(cha != null)
			char_obj_id = cha.getObjectId();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_OLYMPIAD_SPELLED_INFO.writeId(packetWriter);
		packetWriter.writeD(char_obj_id);
		packetWriter.writeD(_effects.size());
		for(Abnormal temp : _effects)
		{
			packetWriter.writeD(temp.skillId);
			packetWriter.writeH(temp.dat);
			packetWriter.writeD(temp.abnormalType);
			writeOptionalD(packetWriter, temp.duration);
		}

		return true;
	}
}