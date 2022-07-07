package l2s.gameserver.listener.actor.ai;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.listener.AiListener;
import l2s.gameserver.model.Creature;

public interface OnAiEventListener extends AiListener
{
	void onAiEvent(Creature p0, CtrlEvent p1, Object[] p2);
}
