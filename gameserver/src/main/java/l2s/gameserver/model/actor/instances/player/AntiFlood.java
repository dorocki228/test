package l2s.gameserver.model.actor.instances.player;

import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class AntiFlood
{
	private final TIntSet _interlocutors;
	private final TIntLongMap _recentReceivers;
	private final Player _owner;
	private long _lastSent;
	private String _lastText;
	private long _allChatUseTime;
	private long _shoutChatUseTime;
	private long fractionShoutChatUseTime;
	private long _heroChatUseTime;
	private long _privateChatUseTime;
	private long _mailUseTime;

	public AntiFlood(Player owner)
	{
		_interlocutors = new TIntHashSet();
		_recentReceivers = new TIntLongHashMap();
		_lastSent = 0L;
		_lastText = "";
		_owner = owner;
	}

	public boolean canAll(String text)
	{
		if(_owner.isGM())
			return true;
		if(_owner.hasPremiumAccount())
		{
			if(Config.ALL_CHAT_USE_MIN_LEVEL > _owner.getLevel())
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.all.level").addNumber(Config.ALL_CHAT_USE_MIN_LEVEL));
				return false;
			}
		}
		else if(Config.ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA > _owner.getLevel())
		{
			_owner.sendPacket(new SystemMessagePacket(SystemMsg.PLAYERS_CAN_USE_GENERAL_CHAT_AFTER_LV_S1).addNumber(Config.ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA - 1));
			return false;
		}
		if(Config.ALL_CHAT_USE_DELAY > 0)
		{
			long currentMillis = System.currentTimeMillis();
			int delay = (int) ((_allChatUseTime - currentMillis) / 1000L);
			if(delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.all.delay").addNumber(delay));
				return false;
			}
			_allChatUseTime = currentMillis + Config.ALL_CHAT_USE_DELAY * 1000L;
		}
		return true;
	}

	public boolean canShout(String text)
	{
		if(_owner.isGM())
			return true;
		if(_owner.hasPremiumAccount())
		{
			if(Config.SHOUT_CHAT_USE_MIN_LEVEL > _owner.getLevel())
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.shout.level").addNumber(Config.ALL_CHAT_USE_MIN_LEVEL));
				return false;
			}
		}
		else if(Config.SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA > _owner.getLevel())
		{
			_owner.sendPacket(new SystemMessagePacket(SystemMsg.PLAYERS_CAN_SHOUT_AFTER_LV_S1).addNumber(Config.SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA - 1));
			return false;
		}
		if(Config.SHOUT_CHAT_USE_DELAY > 0)
		{
			long currentMillis = System.currentTimeMillis();
			int delay = (int) ((_shoutChatUseTime - currentMillis) / 1000L);
			if(delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.shout.delay").addNumber(delay));
				return false;
			}
			_shoutChatUseTime = currentMillis + Config.SHOUT_CHAT_USE_DELAY * 1000L;
		}
		return true;
	}

	public boolean canUseFractionShoutChat(String text)
	{
		if(_owner.isGM())
			return true;
		if(_owner.hasPremiumAccount())
		{
			if(Config.FRACTION_SHOUT_CHAT_USE_MIN_LEVEL > _owner.getLevel())
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.fraction_shout.level").addNumber(Config.FRACTION_SHOUT_CHAT_USE_MIN_LEVEL));
				return false;
			}
		}
		else if(Config.FRACTION_SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA > _owner.getLevel())
		{
			_owner.sendPacket(new SystemMessagePacket(SystemMsg.PLAYERS_CAN_USE_FRACTION_SHOUT_CHAT_AFTER_LV_S1).addNumber(Config.SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA - 1));
			return false;
		}
		if(Config.FRACTION_SHOUT_CHAT_USE_DELAY > 0)
		{
			long currentMillis = System.currentTimeMillis();
			int delay = (int) ((fractionShoutChatUseTime - currentMillis) / 1000L);
			if(delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.fraction_shout.delay").addNumber(delay));
				return false;
			}
			fractionShoutChatUseTime = currentMillis + Config.FRACTION_SHOUT_CHAT_USE_DELAY * 1000L;
		}
		return true;
	}

	public boolean canHero(String text)
	{
		if(_owner.isGM())
			return true;
		if(_owner.hasPremiumAccount())
		{
			if(Config.HERO_CHAT_USE_MIN_LEVEL > _owner.getLevel())
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.hero.level").addNumber(Config.HERO_CHAT_USE_MIN_LEVEL));
				return false;
			}
		}
		else if(Config.HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.hero.level").addNumber(Config.HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA));
			return false;
		}
		if(Config.HERO_CHAT_USE_DELAY > 0)
		{
			long currentMillis = System.currentTimeMillis();
			int delay = (int) ((_heroChatUseTime - currentMillis) / 1000L);
			if(delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.hero.delay").addNumber(delay));
				return false;
			}
			_heroChatUseTime = currentMillis + Config.HERO_CHAT_USE_DELAY * 1000L;
		}
		return true;
	}

	public boolean canMail()
	{
		if(_owner.isGM())
			return true;
		if(_owner.hasPremiumAccount())
		{
			if(Config.MAIL_USE_MIN_LEVEL > _owner.getLevel())
			{
				_owner.sendMessage(new CustomMessage("antispam.no_mail.level").addNumber(Config.MAIL_USE_MIN_LEVEL));
				return false;
			}
		}
		else if(Config.MAIL_USE_MIN_LEVEL_WITHOUT_PA > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_mail.level").addNumber(Config.MAIL_USE_MIN_LEVEL_WITHOUT_PA));
			return false;
		}
		if(Config.MAIL_USE_DELAY > 0)
		{
			long currentMillis = System.currentTimeMillis();
			int delay = (int) ((_mailUseTime - currentMillis) / 1000L);
			if(delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_mail.delay").addNumber(delay));
				return false;
			}
			_mailUseTime = currentMillis + Config.MAIL_USE_DELAY * 1000L;
		}
		return true;
	}

	public boolean canTell(int receiverId, String text)
	{
		if(_owner.isGM())
			return true;
		if(!_interlocutors.contains(receiverId))
			if(_owner.hasPremiumAccount())
			{
				if(Config.PRIVATE_CHAT_USE_MIN_LEVEL > _owner.getLevel())
				{
					_owner.sendMessage(new CustomMessage("antispam.no_chat.private.level").addNumber(Config.PRIVATE_CHAT_USE_MIN_LEVEL));
					return false;
				}
			}
			else if(Config.PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA > _owner.getLevel())
			{
				_owner.sendPacket(new SystemMessagePacket(SystemMsg.PLAYERS_CAN_RESPOND_TO_A_WHISPER_BUT_CANNOT_INITIATE_A_WHISPER_UNTIL_LV_S1).addNumber(Config.SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA - 1));
				return false;
			}
		if(Config.PRIVATE_CHAT_USE_DELAY > 0)
		{
			long currentMillis = System.currentTimeMillis();
			int delay = (int) ((_privateChatUseTime - currentMillis) / 1000L);
			if(delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.private.delay").addNumber(delay));
				return false;
			}
			_privateChatUseTime = currentMillis + Config.PRIVATE_CHAT_USE_DELAY * 1000L;
		}
		long currentMillis = System.currentTimeMillis();
		TIntLongIterator itr = _recentReceivers.iterator();
		int recent = 0;
		while(itr.hasNext())
		{
			itr.advance();
			long lastSent = itr.value();
			if(currentMillis - lastSent < (text.equalsIgnoreCase(_lastText) ? 600000L : 60000L))
				++recent;
			else
				itr.remove();
		}
		long lastSent = _recentReceivers.put(receiverId, currentMillis);
		long delay2 = 333L;
		if(recent > 3)
		{
			lastSent = _lastSent;
			delay2 = (recent - 3) * 3333L;
		}
		_lastText = text;
		_lastSent = currentMillis;
		int remainingDelay = (int) ((delay2 - (currentMillis - lastSent)) / 1000L);
		if(remainingDelay > 0)
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.private.delay").addNumber(remainingDelay));
			return false;
		}
		return true;
	}

	public void addInterlocutorId(int id)
	{
		_interlocutors.add(id);
	}
}
