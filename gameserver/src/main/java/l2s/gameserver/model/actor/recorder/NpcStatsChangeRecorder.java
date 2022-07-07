package l2s.gameserver.model.actor.recorder;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcInfoAbnormalVisualEffect;

public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance>
{
	public NpcStatsChangeRecorder(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();
		if((_changes & 0x1) == 0x1)
			_activeChar.broadcastCharInfo();
		if((_changes & 0x8) == 0x8 || (_changes & 0x10) == 0x10)
			_activeChar.broadcastPacket(new NpcInfoAbnormalVisualEffect(_activeChar));
	}
}
