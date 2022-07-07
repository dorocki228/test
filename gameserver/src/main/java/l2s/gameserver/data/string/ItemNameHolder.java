package l2s.gameserver.data.string;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class ItemNameHolder extends AbstractHolder
{
	private static final ItemNameHolder _instance;
	private final Map<Language, TIntObjectMap<String>> _itemNames;

	public static ItemNameHolder getInstance()
	{
		return _instance;
	}

	private ItemNameHolder()
	{
		_itemNames = new HashMap<>();
	}

	public String getItemName(Language lang, int itemId)
	{
		TIntObjectMap<String> itemNames = _itemNames.get(lang);
		String name = itemNames.get(itemId);
		if(name == null)
			if(lang == Language.ENGLISH)
			{
				itemNames = _itemNames.get(Language.RUSSIAN);
				name = itemNames.get(itemId);
			}
			else
			{
				itemNames = _itemNames.get(Language.ENGLISH);
				name = itemNames.get(itemId);
			}
		return name;
	}

	public String getItemName(Player player, int itemId)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getItemName(lang, itemId);
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_itemNames.put(lang, new TIntObjectHashMap());
			if(Config.AVAILABLE_LANGUAGES.contains(lang))
			{
				File file = new File(Config.DATAPACK_ROOT, "data/string/itemname/" + lang.getShortName() + ".txt");
				if(!file.exists())
				{
					if(lang == Language.ENGLISH || lang == Language.RUSSIAN)
                        warn("Not find file: " + file.getAbsolutePath());
				}
				else
				{
					try(LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))))
					{
						String line = null;
						while((line = reader.readLine()) != null)
						{
							StringTokenizer token = new StringTokenizer(line, "\t");
							if(token.countTokens() < 2)
								error("Error on line: " + line + "; file: " + file.getName());
							else
							{
								int id = Integer.parseInt(token.nextToken());
								String value = token.nextToken();
								_itemNames.get(lang).put(id, value);
							}
						}
					}
					catch(Exception e)
					{
						error("Exception: " + e, e);
					}
				}
			}
		}
		log();
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, TIntObjectMap<String>> entry : _itemNames.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
            info("load item names: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _itemNames.size();
	}

	@Override
	public void clear()
	{
		_itemNames.clear();
	}

	static
	{
		_instance = new ItemNameHolder();
	}
}
