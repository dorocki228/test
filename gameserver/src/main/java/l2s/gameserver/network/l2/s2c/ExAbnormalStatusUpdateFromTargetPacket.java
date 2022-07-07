package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class ExAbnormalStatusUpdateFromTargetPacket extends L2GameServerPacket
{
	public static final int INFINITIVE_EFFECT = -1;
	private final List<Abnormal> _effects;
	private final int _objectId;

	public ExAbnormalStatusUpdateFromTargetPacket(int objId)
	{
		_objectId = objId;
		_effects = new ArrayList<>();
	}

	public void addEffect(int effectorObjectId, int skillId, int dat, int duration, int comboId)
	{
		_effects.add(new Abnormal(effectorObjectId, skillId, dat, duration, comboId));
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
        writeH(_effects.size());
		for(Abnormal temp : _effects)
		{
            writeD(temp.skillId);
            writeH(temp.dat);
            writeH(temp.comboId);
            writeH(temp.duration);
            writeD(temp.effectorObjectId);
		}
	}

	class Abnormal
	{
		int skillId;
		int dat;
		int duration;
		int effectorObjectId;
		int comboId;

		public Abnormal(int effectorObjectId, int skillId, int dat, int duration, int comboId)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.duration = duration;
			this.effectorObjectId = effectorObjectId;
			this.comboId = comboId;
		}
	}
}
