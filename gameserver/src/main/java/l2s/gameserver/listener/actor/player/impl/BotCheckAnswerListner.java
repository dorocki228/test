package l2s.gameserver.listener.actor.player.impl;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.instancemanager.BotCheckManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;

public class BotCheckAnswerListner implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final int _qId;

	public BotCheckAnswerListner(Player player, int qId)
	{
		_playerRef = player.getRef();
		_qId = qId;
	}

	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;
		boolean rightAnswer = BotCheckManager.checkAnswer(_qId, true);
		if(rightAnswer)
		{
			player.increaseBotRating();
			sendFeedBack(player, true, player.isLangRus());
		}
		else
		{
			sendFeedBack(player, false, player.isLangRus());
			player.decreaseBotRating();
		}
	}

	@Override
	public void sayNo()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;
		boolean rightAnswer = BotCheckManager.checkAnswer(_qId, false);
		if(rightAnswer)
		{
			player.increaseBotRating();
			sendFeedBack(player, true, player.isLangRus());
		}
		else
		{
			player.decreaseBotRating();
			sendFeedBack(player, false, player.isLangRus());
		}
	}

	private void sendFeedBack(Player player, boolean rightAnswer, boolean isLangRus)
	{
		if(rightAnswer)
		{
			if(isLangRus)
				player.sendMessage("\u0412\u044b \u043e\u0442\u0432\u0435\u0442\u0438\u043b\u0438 \u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e!");
			else
				player.sendMessage("Your answer is correct!");
		}
		else if(isLangRus)
			player.sendMessage("\u0412\u044b \u043e\u0442\u0432\u0435\u0442\u0438\u043b\u0438 \u043d\u0435 \u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e! \u0412 \u0441\u043b\u0443\u0447\u0430\u0435 \u0438 \u0432\u044b \u043e\u0442\u0432\u0435\u0442\u0438\u0442\u0435 \u043d\u0435 \u0432\u0435\u0440\u043d\u043e \u043d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u0440\u0430\u0437 \u043f\u043e\u0434\u0440\u044f\u0434, \u0442\u043e \u0432\u044b \u0431\u0443\u0434\u0435\u0442\u0435 \u043f\u043e\u043c\u0435\u0449\u0435\u043d\u044b \u0432 \u0442\u044e\u0440\u044c\u043c\u0443.");
		else
			player.sendMessage("Your answer is incorrect! In case you will answer several time incorectly, you will be placed in jail for botting");
	}
}
