package l2s.gameserver.model.actor.recorder;

import l2s.gameserver.model.Servitor;

public class ServitorStatsChangeRecorder extends CharStatsChangeRecorder<Servitor>
{
	public ServitorStatsChangeRecorder(Servitor actor)
	{
		super(actor);
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();
		if((_changes & 0x2) == 0x2)
			_activeChar.sendPetInfo();
		else if((_changes & 0x1) == 0x1 || (_changes & 0x8) == 0x8 || (_changes & 0x10) == 0x10)
			_activeChar.broadcastCharInfo();
	}
}
