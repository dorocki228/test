package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ExEventMatchSpelledInfo extends L2GameServerPacket
{
	private int char_obj_id;
	private final List<Abnormal> _effects;

	public ExEventMatchSpelledInfo()
	{
		char_obj_id = 0;
		_effects = new ArrayList<>();
	}

	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Abnormal(skillId, dat, duration));
	}

	public void addSpellRecivedPlayer(Player cha)
	{
		if(cha != null)
			char_obj_id = cha.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
        writeD(char_obj_id);
        writeD(_effects.size());
		for(Abnormal temp : _effects)
		{
            writeD(temp.skillId);
            writeH(temp.dat);
            writeD(temp.duration);
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
