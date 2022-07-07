package  l2s.Phantoms.ai.chat;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.enums.PhantomType;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.GameObjectsStorage;
import  l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;

public class GlobalChat
{
	protected final Logger _log = LoggerFactory.getLogger(GlobalChat.class);
	private static GlobalChat _instance;
	
	public static GlobalChat getInstance()
	{
		if (_instance == null)
			_instance = new GlobalChat();
		
		return _instance;
	}
	
	private  long _lastGreetingTime = 0;
	
	private  Map <Integer,Long> _p_temp = new HashMap <Integer,Long>();
	
	private final String[] hi =
	{"привет","привет","привет","привет","ку","ку!","ky","hi","Привет","Привет","Привет"};
	
	public boolean canGreeting(Player player)
	{
		if (System.currentTimeMillis() - player.getOnlineBeginTime() > 300000)
			return false;
		
		long currentMillis = System.currentTimeMillis();
		if (currentMillis-_lastGreetingTime < Rnd.get(1,2)*60*1000)
			return false;
		_lastGreetingTime = currentMillis;
		if (_p_temp.containsKey(player.getObjectId()))
		{
			if (currentMillis-_p_temp.get(player.getObjectId()) < 60*60*1000)
				return false;
			_p_temp.remove(player.getObjectId());
		}
		_p_temp.put(player.getObjectId(), currentMillis);
		return true;
	}

	public void textCheck(Player player, String _text)
	{

		if ((wordsearch(_text.toLowerCase(), "добрый день")) 
																																|| (wordsearch(_text.toLowerCase(), "привет")) 
																																|| (wordsearch(_text.toLowerCase(), "всем привет")) 
																																|| (wordsearch(_text.toLowerCase(), "привет всем")) 
																																|| (wordsearch(_text.toLowerCase(), "ку!")) 
																																|| (wordsearch(_text.toLowerCase(), "re!")) 
																																|| (wordsearch(_text.toLowerCase(), "ку,")) 
																																|| (wordsearch(_text.toLowerCase(), "re,")) 
																																|| (wordsearch(_text.toLowerCase(), "ky")) 
																																|| (wordsearch(_text.toLowerCase(), "qq"))
																																|| (wordsearch(_text.toLowerCase(), "qq ")) 
																																|| (wordsearch(_text.toLowerCase(), "qq all")) 
																																|| (wordsearch(_text.toLowerCase(), "ghbdtn")) 
																																|| (wordsearch(_text.toLowerCase(), "ку ")) 
																																|| (wordsearch(_text.toLowerCase(), "день добрый")) 
																																|| (wordsearch(_text.toLowerCase(), "хай ")) 
																																|| (wordsearch(_text.toLowerCase(), "хэй")) 
																																|| (wordsearch(_text.toLowerCase(), "доброго времени суток")) 
																																|| (wordsearch(_text.toLowerCase(), "превет")))
		{
			if (canGreeting(player))
				sendGreeting();
		}
	}
	
	//TODO реализовать другую логику
	public void sendGreeting()
	{
		int count = 0; 
		int max_count = Rnd.get(3, 15);
		for(Player player : GameObjectsStorage.getPlayers())
		{
			if (player.isPhantom() && player.getPhantomType() == PhantomType.PHANTOM && Rnd.chance(2))
			{
				count ++;
				if (count == max_count)
					break;
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							//TODO
							Thread.sleep(Rnd.get(3, 20)*1000);
							for(Player temp : GameObjectsStorage.getPlayers())
							{
								if (temp.isPhantom() || temp == player || player.getReflection() != temp.getReflection() || temp.isBlockAll() /*|| temp.isInBlockList(player)*/)
									continue;
								//temp.sendPacket(new SayPacket2(player.getObjectId(), ChatType.SHOUT, player.getName(), hi[Rnd.get(hi.length)]));
							}
							
						}catch(InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
	}
	
	public boolean wordsearch(String char1, String char2)
	{
		if (char1.length() != char2.length())
		{
			return false;
		}
		if (char1.equals(char2))
		{
			return true;
		}
		return false;
	}
}
