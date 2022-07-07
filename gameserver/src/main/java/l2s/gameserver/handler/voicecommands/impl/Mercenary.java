package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;

/**
 * @author mangol
 */
public class Mercenary implements IVoicedCommandHandler {
	@Override
	public boolean useVoicedCommand(String command, Player player, String args) {
		if(command.equalsIgnoreCase("mercenary")) {
			if(!player.isMercenary()) {
				return false;
			}
			long endTimeStamp = player.getMercenaryComponent().getEndTimeStamp();
			long currentTimeMillis = System.currentTimeMillis();
			if(endTimeStamp <= 0 || endTimeStamp < currentTimeMillis) {
				return false;
			}
			long timeLeft = (endTimeStamp - currentTimeMillis) / 1000;
			int hours = (int) (timeLeft / 3600);
			int minutes = (int) ((timeLeft - hours * 3600) / 60);
			player.sendMessage(new CustomMessage("mercenary.s4").addNumber(hours).addNumber(minutes));
		}
		return false;
	}

	@Override
	public String[] getVoicedCommandList() {
		return new String[]{"mercenary"};
	}
}
