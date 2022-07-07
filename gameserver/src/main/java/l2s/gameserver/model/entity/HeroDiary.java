package l2s.gameserver.model.entity;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Map;

public class HeroDiary
{
	public static final int ACTION_RAID_KILLED = 1;
	public static final int ACTION_HERO_GAINED = 2;
	public static final int ACTION_CASTLE_TAKEN = 3;
	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:** dd.MM.yyyy");
	private final int _id;
	private final long _time;
	private final int _param;

	public HeroDiary(int id, long time, int param)
	{
		_id = id;
		_time = time;
		_param = param;
	}

	public Map.Entry<String, String> toString(Player player)
	{
		CustomMessage message = null;
		switch(_id)
		{
			case ACTION_RAID_KILLED:
			{
				message = new CustomMessage("l2s.gameserver.model.entity.Hero.RaidBossKilled").addString(HtmlUtils.htmlNpcName(_param));
				break;
			}
			case ACTION_HERO_GAINED:
			{
				message = new CustomMessage("l2s.gameserver.model.entity.Hero.HeroGained");
				break;
			}
			case ACTION_CASTLE_TAKEN:
			{
				message = new CustomMessage("l2s.gameserver.model.entity.Hero.CastleTaken").addString(HtmlUtils.htmlResidenceName(_param));
				break;
			}
			default:
			{
				return null;
			}
		}
		return new AbstractMap.SimpleEntry<>(SIMPLE_FORMAT.format(_time), message.toString(player));
	}
}
