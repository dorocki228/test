package l2s.gameserver.utils;

import com.google.common.collect.Range;
import com.google.common.primitives.Longs;
import l2s.gameserver.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Strings
{
	private static final Logger _log = LoggerFactory.getLogger(Strings.class);

	private static final Pattern COMPILE = Pattern.compile(" +");

	private static String[] tr;
	private static String[] trb;
	private static String[] trcode;

	public static String stripSlashes(String s)
	{
		if(s == null)
			return "";
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}

	public static Boolean parseBoolean(Object x)
	{
		if(x == null)
			return false;
		if(x instanceof Number)
			return ((Number) x).intValue() > 0;
		if(x instanceof Boolean)
			return (Boolean) x;
		if(x instanceof Double)
			return Math.abs((double) x) < 1.0E-5;
		return !String.valueOf(x).isEmpty();
	}

	public static void reload()
	{
		try
		{
			List<String> pairs = Files.readAllLines(Config.DATAPACK_ROOT_PATH.resolve("data/translit.txt"));

			tr = new String[pairs.size() * 2];
			for(int i = 0; i < pairs.size(); ++i)
			{
				String[] ss = COMPILE.split(pairs.get(i));
				tr[i * 2] = ss[0];
				tr[i * 2 + 1] = ss[1];
			}

			pairs = Files.readAllLines(Config.DATAPACK_ROOT_PATH.resolve("data/translit_back.txt"));

			trb = new String[pairs.size() * 2];
			for(int i = 0; i < pairs.size(); ++i)
			{
				String[] ss = COMPILE.split(pairs.get(i));
				trb[i * 2] = ss[0];
				trb[i * 2 + 1] = ss[1];
			}

			pairs = Files.readAllLines(Config.DATAPACK_ROOT_PATH.resolve("data/transcode.txt"));

			trcode = new String[pairs.size() * 2];
			for(int i = 0; i < pairs.size(); ++i)
			{
				String[] ss = COMPILE.split(pairs.get(i));
				trcode[i * 2] = ss[0];
				trcode[i * 2 + 1] = ss[1];
			}
		}
		catch(IOException e)
		{
			_log.error("", e);
		}

		_log.info("Loaded {} translit entries.", tr.length + trb.length + trcode.length);
	}

	public static String translit(String s)
	{
		for(int i = 0; i < tr.length; i += 2)
			s = s.replace(tr[i], tr[i + 1]);
		return s;
	}

	public static String fromTranslit(String s, int type)
	{
		if(type == 1)
			for(int i = 0; i < trb.length; i += 2)
				s = s.replace(trb[i], trb[i + 1]);
		else if(type == 2)
			for(int i = 0; i < trcode.length; i += 2)
				s = s.replace(trcode[i], trcode[i + 1]);
		return s;
	}

	public static String replace(String str, String regex, int flags, String replace)
	{
		return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
	}

	public static boolean matches(String str, String regex, int flags)
	{
		return Pattern.compile(regex, flags).matcher(str).matches();
	}

	public static void replaceString(StringBuilder sb, String toReplace, String replacement)
	{
		int index;
		while ((index = sb.lastIndexOf(toReplace)) != -1)
			sb.replace(index, index + toReplace.length(), replacement);
	}

	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		String result = "";
		if(startIdx < 0)
		{
			startIdx += strings.length;
			if(startIdx < 0)
				return result;
		}
		while(startIdx < strings.length && maxCount != 0)
		{
			if(!result.isEmpty() && glueStr != null && !glueStr.isEmpty())
				result += glueStr;
			result += strings[startIdx++];
			--maxCount;
		}
		return result;
	}

	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return joinStrings(glueStr, strings, startIdx, -1);
	}

	public static String joinStrings(String glueStr, String[] strings)
	{
		return joinStrings(glueStr, strings, 0);
	}

	public static String stripToSingleLine(String s)
	{
		if(s.isEmpty())
			return s;
		s = s.replaceAll("\\\\n", "\n");
		int i = s.indexOf("\n");
		if(i > -1)
			s = s.substring(0, i);
		return s;
	}

	public static boolean isDigit(String text)
	{
		return text != null && text.matches("[0-9]+");
	}

	public static Optional<Range<Long>> getRange(String item)
	{
		var parts = item.split("-");
		Long min = Longs.tryParse(parts[0]);
		if(min == null)
			return Optional.empty();
		if(parts.length == 1)
			return Optional.of(Range.closed(min, min));
		Long max = Longs.tryParse(parts[1]);
		if(max == null)
			return Optional.of(Range.closed(min, min));

		return Optional.of(Range.closed(min, max));
	}
}
