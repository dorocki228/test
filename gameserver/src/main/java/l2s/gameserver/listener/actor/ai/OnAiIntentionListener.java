package l2s.gameserver.listener.actor.ai;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.listener.AiListener;
import l2s.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener
{
	void onAiIntention(Creature p0, CtrlIntention p1, Object p2, Object p3);
}
