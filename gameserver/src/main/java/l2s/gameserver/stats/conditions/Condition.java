package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;

public abstract class Condition
{
	protected static final String TARGET = "TARGET";
	protected static final String THIS = "THIS";
	public static final Condition[] EMPTY_ARRAY = new Condition[0];
	private SystemMsg _message;
	private String customMessageLink;

	public void init(){

	}

	public final void setSystemMsg(int msgId)
	{
		_message = SystemMsg.valueOf(msgId);
	}

	public final SystemMsg getSystemMsg()
	{
		return _message;
	}

	public final String getCustomMessageLink() {
		return customMessageLink;
	}

	public final boolean isCustomMessageLink() {
		return customMessageLink != null && !customMessageLink.isEmpty();
	}

	public final boolean test(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(creature != null)
			for(Event event : creature.getEvents())
				if(!event.checkCondition(creature, getClass()))
					return false;
		return testImpl(creature, target, skill, item, value);
	}

	protected abstract boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value);

	public void setCustomMessageLink(String customMessageLink) {
		this.customMessageLink = customMessageLink;
	}
}
