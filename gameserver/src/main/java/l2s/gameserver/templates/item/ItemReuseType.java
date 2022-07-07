package l2s.gameserver.templates.item;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;

public enum ItemReuseType
{
	NORMAL(new SystemMsg[] {
			SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME,
			SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME,
			SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME })
	{
		@Override
		public long next(ItemInstance item)
		{
			return System.currentTimeMillis() + item.getTemplate().getReuseDelay();
		}
	},
	EVERY_DAY_AT_6_30(new SystemMsg[] {
			SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_FOR_S1S_REUSE_TIME,
			SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_FOR_S1S_REUSE_TIME,
			SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_S1S_REUSE_TIME })
	{
		private final SchedulingPattern _pattern;

		{
			_pattern = new SchedulingPattern("30 6 * * *");
		}

		@Override
		public long next(ItemInstance item)
		{
			long result;
			for(result = _pattern.next(System.currentTimeMillis()); result < System.currentTimeMillis(); result += 86400000L)
			{}
			return result;
		}
	};

	public static final ItemReuseType[] VALUES;
	private final SystemMsg[] _messages;

	ItemReuseType(SystemMsg[] msg)
	{
		_messages = msg;
	}

	public abstract long next(ItemInstance p0);

	public SystemMsg[] getMessages()
	{
		return _messages;
	}

	static
	{
		VALUES = values();
	}
}
