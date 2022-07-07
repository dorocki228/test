package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class AbnormalStatusUpdatePacket extends L2GameServerPacket
{
	public static final int INFINITIVE_EFFECT = -1;
	private final List<Abnormal> _effects;

	public AbnormalStatusUpdatePacket()
	{
		_effects = new ArrayList<>();
	}

	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Abnormal(skillId, dat, duration));
	}

	@Override
	protected final void writeImpl()
	{
        writeH(_effects.size());
		for(Abnormal temp : _effects)
		{
            writeD(temp.skillId);
            writeH(temp.dat);
            writeD(0);
            writeH(temp.duration);
		}
	}

	class Abnormal
	{
		int skillId;
		int dat;
		int duration;

		public Abnormal(int skillId, int dat, int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.duration = duration;
		}
	}
}
