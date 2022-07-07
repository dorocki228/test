package l2s.gameserver.data.string;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.SkillName;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.SkillUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class SkillNameHolder extends AbstractHolder
{
	private static final SkillNameHolder _instance;
	private final Map<Language, TIntObjectMap<SkillName>> _skillNames;

	public static SkillNameHolder getInstance()
	{
		return _instance;
	}

	private SkillNameHolder()
	{
		_skillNames = new HashMap<>();
	}

	public SkillName getSkillName(Language lang, int hashCode)
	{
		TIntObjectMap<SkillName> skillNames = _skillNames.get(lang);
		SkillName skillName = skillNames.get(hashCode);
		if(skillName == null)
			if(lang == Language.ENGLISH)
			{
				skillNames = _skillNames.get(Language.RUSSIAN);
				skillName = skillNames.get(hashCode);
			}
			else
			{
				skillNames = _skillNames.get(Language.ENGLISH);
				skillName = skillNames.get(hashCode);
			}
		return skillName;
	}

	public SkillName getSkillName(Player player, int hashCode)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getSkillName(lang, hashCode);
	}

	public SkillName getSkillName(Language lang, Skill skill)
	{
		return getSkillName(lang, skill.hashCode());
	}

	public SkillName getSkillName(Player player, Skill skill)
	{
		return getSkillName(player, skill.hashCode());
	}

	public SkillName getSkillName(Language lang, int id, int level)
	{
		return getSkillName(lang, SkillUtils.generateSkillHashCode(id, level));
	}

	public SkillName getSkillName(Player player, int id, int level)
	{
		return getSkillName(player, SkillUtils.generateSkillHashCode(id, level));
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_skillNames.put(lang, new TIntObjectHashMap<>());
			if(Config.AVAILABLE_LANGUAGES.contains(lang))
			{
				File file = new File(Config.DATAPACK_ROOT, "data/string/skillname/" + lang.getShortName() + ".txt");
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
								int level = Integer.parseInt(token.nextToken());
								int hashCode = SkillUtils.generateSkillHashCode(id, level);
								String value = token.hasMoreTokens() ? token.nextToken() : "";
								String desc = token.hasMoreTokens() ? token.nextToken() : "";
								_skillNames.get(lang).put(hashCode, new SkillName(id, level, value, desc));
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
		for(Map.Entry<Language, TIntObjectMap<SkillName>> entry : _skillNames.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
            info("load skill names: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _skillNames.size();
	}

	@Override
	public void clear()
	{
		_skillNames.clear();
	}

	static
	{
		_instance = new SkillNameHolder();
	}
}
