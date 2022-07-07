package l2s.gameserver.utils.spamfilter;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.config.templates.SpamRule;
import l2s.gameserver.config.xml.holder.SpamFilterConfigHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.tables.GmListTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

/**
 * @author KanuToIIIKa
 */

public class SpamFilterManager
{
	private final Logger _log = LoggerFactory.getLogger(SpamFilterManager.class);
	private static final SpamFilterManager _instance = new SpamFilterManager();

	private TIntObjectMap<SpamFilterEye> _filterEyes;

	public static SpamFilterManager getInstance()
	{
		return _instance;
	}

	private SpamFilterManager()
	{
		if(Config.SPAM_FILTER_ENABLED)
			_filterEyes = new TIntObjectHashMap<>();
	}

	public boolean isSpam(String name, String text, SpamType type)
	{
		if(Config.SPAM_FILTER_ENABLED)
		{
			boolean isSpam = false;
			for(SpamRule rule : SpamFilterConfigHolder.getInstance().getRules())
			{
				Matcher matcher = rule.getPattern().matcher(text);

				if(matcher.find())
				{
					isSpam = true;
					break;
				}
			}

			if(!isSpam)
				return false;

			StringBuilder sb = new StringBuilder();

			if(Config.SPAM_FILTER_NOTIFY_GM)
			{
				sb.append("Spam Filter[").append(type.name()).append("]: ");
				sb.append(name).append(" ");
				sb.append(text);

				GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.BLUE_UNK, "", sb.toString()));
			}

			if(Config.SPAM_FILTER_LOGGING)
				_log.info(sb.toString());

			return Config.SPAM_FILTER_BLOC_SPAM;
		}

		return false;
	}

	public boolean isSpam(Player player, String text, SpamType type)
	{
		if(Config.SPAM_FILTER_ENABLED)
		{

			SpamFilterEye eye = getEye(player);

			boolean spamer = player.isSpamer();

			if(!eye.isSpam(text) && !spamer)
				return false;

			StringBuilder sb = new StringBuilder();

			if(Config.SPAM_FILTER_NOTIFY_GM)
			{
				sb.append("Spam Filter[").append(type.name()).append("]: ");
				sb.append(eye.getPlayerName());
				sb.append("(").append(spamer ? "S" : eye.getPenalty() == -1 ? "T" : String.valueOf(eye.getPenalty())).append(") ");
				sb.append(text);

				GmListTable.broadcastToGMs(new SayPacket2(eye.getPlayerObjectId(), ChatType.BLUE_UNK, "", sb.toString()));
			}

			if(Config.SPAM_FILTER_LOGGING)
				_log.info(sb.toString());

			return Config.SPAM_FILTER_BLOC_SPAM;
		}

		return false;
	}

	private SpamFilterEye getEye(Player player)
	{
		SpamFilterEye eye = _filterEyes.get(player.getObjectId());
		if(eye == null)
			_filterEyes.put(player.getObjectId(), (eye = new SpamFilterEye(player)));

		return eye;
	}

	public enum SpamType
	{
		UNDERFIEND,
		CHAT_ALL,
		CHAT_SHOUT,
		CHAT_FRACTION_SHOUT,
		CHAT_TELL,
		CHAT_FRACTION_WORLD,
		STORE_SELL,
		STORE_MANUFACTURE,
		STORE_BUY,
		MAIL_TOPIC,
		MAIL_BODY;

		public static SpamType getFromChatType(ChatType type)
		{
			switch(type)
			{
				case ALL:
					return CHAT_ALL;
				case SHOUT:
					return CHAT_SHOUT;
				case FRACTION_SHOUT:
					return CHAT_FRACTION_SHOUT;
				case TELL:
					return CHAT_TELL;
				case FRACTION_WORLD:
					return CHAT_FRACTION_WORLD;
				default:
					return UNDERFIEND;
			}
		}
	}
}
