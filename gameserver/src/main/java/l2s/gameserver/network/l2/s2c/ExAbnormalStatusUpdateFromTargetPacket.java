package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExAbnormalStatusUpdateFromTargetPacket implements IClientOutgoingPacket
{
	public static final int INFINITIVE_EFFECT = -1;

	private List<Abnormal> _effects;
	private int _objectId;

	private static class Abnormal
	{
		public int effectorObjectId;
		public int skillId;
		public int skillLvl;
		public int abnormalType;
		public int duration;

		public Abnormal(int effectorObjectId, int skillId, int skillLvl, int abnormalType, int duration)
		{
			this.effectorObjectId = effectorObjectId;
			this.skillId = skillId;
			this.skillLvl = skillLvl;
			this.abnormalType = abnormalType;
			this.duration = duration;
		}
	}

	public ExAbnormalStatusUpdateFromTargetPacket(int objId)
	{
		_objectId = objId;
		_effects = new ArrayList<Abnormal>();
	}

	public void addEffect(int effectorObjectId, int skillId, int skillLvl, int abnormalType, int duration)
	{
		_effects.add(new Abnormal(effectorObjectId, skillId, skillLvl, abnormalType, duration));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ABNORMAL_STATUUPDATE_FROM_TARGET.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeH(_effects.size());
		for(Abnormal temp : _effects)
		{
			packetWriter.writeD(temp.skillId);
			packetWriter.writeH(temp.skillLvl);
			packetWriter.writeH(temp.abnormalType);
			writeOptionalD(packetWriter, temp.duration);
			packetWriter.writeD(temp.effectorObjectId); // Buffer OID
		}

		return true;
	}
}