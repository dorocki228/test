package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.dao.CharacterTrainingCampDAO;
import l2s.gameserver.instancemanager.TrainingCampManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.TrainingCamp;

/**
 * @author Sdw
 */
public class NotifyTrainingRoomEnd implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		TrainingCamp trainingCamp = TrainingCampManager.getInstance().getTrainingCamp(activeChar);
		if(trainingCamp == null || !trainingCamp.isTraining() && !trainingCamp.isValid(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		trainingCamp.setEndTime(System.currentTimeMillis());
		CharacterTrainingCampDAO.getInstance().replace(activeChar.getAccountName(), trainingCamp);
		TrainingCampManager.getInstance().onExitTrainingCamp(activeChar);
	}
}