package l2s.gameserver.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BypassStorage
{
	private static final Pattern htmlBypass;
	private static final Pattern htmlLink;
	private static final Pattern bbsWrite;
	private static final Pattern directHtmlBypass;
	private static final Pattern directBbsBypass;
	private final List<ValidBypass> bypasses;

	public BypassStorage()
	{
		bypasses = new CopyOnWriteArrayList<>();
	}

	public void parseHtml(CharSequence html, BypassType type)
	{
		clear(type);
		if(StringUtils.isEmpty(html))
			return;
		Matcher m = htmlBypass.matcher(html);
		while(m.find())
		{
			String bypass = m.group(1);
			int i = bypass.indexOf(" $");
			if(i > 0)
				bypass = bypass.substring(0, i);
			addBypass(new ValidBypass(bypass, i >= 0, type));
		}
		if(type == BypassType.BBS)
		{
			m = bbsWrite.matcher(html);
			while(m.find())
			{
				String bypass = m.group(1);
				addBypass(new ValidBypass(bypass, true, type));
			}
		}
		m = htmlLink.matcher(html);
		while(m.find())
		{
			String bypass = m.group(1);
			addBypass(new ValidBypass(bypass, false, type));
		}
	}

	public ValidBypass validate(String bypass)
	{
		ValidBypass ret = null;
		if(directHtmlBypass.matcher(bypass).matches())
			ret = new ValidBypass(bypass, false, BypassType.DEFAULT);
		else if(directBbsBypass.matcher(bypass).matches())
			ret = new ValidBypass(bypass, false, BypassType.BBS);
		else
		{
			boolean args = bypass.indexOf(" ") > 0;
			for(ValidBypass bp : bypasses)
				if(bp.bypass.equals(bypass) || args == bp.args && bypass.startsWith(bp.bypass + " "))
				{
					ret = bp;
					break;
				}
		}
		if(ret != null)
			clear(ret.type);
		return ret;
	}

	private void addBypass(ValidBypass bypass)
	{
		bypasses.add(bypass);
	}

	private void clear(BypassType type)
	{
		for(ValidBypass bp : bypasses)
			if(bp.type == type)
				bypasses.remove(bp);
	}

	static
	{
		htmlBypass = Pattern.compile("<(?:button|a)[^>]+?action=\"bypass +(?:-h +)?([^\"]+?)\"[^>]*?>", 2);
		htmlLink = Pattern.compile("<(?:button|a)[^>]+?action=\"link +([^\"]+?)\"[^>]*?>", 2);
		bbsWrite = Pattern.compile("<(?:button|a)[^>]+?action=\"write +(\\S+) +\\S+ +\\S+ +\\S+ +\\S+ +\\S+\"[^>]*?>", 2);
		directHtmlBypass = Pattern.compile("^(admin|_mrsl|_diary|_match|manor_menu_select|_olympiad|menu_select?|talk_select|teleport_request|deposit|withdraw|deposit_pledge|withdraw_pledge|class_change?).*", 32);
		directBbsBypass = Pattern.compile("^(admin|_bbshome|_bbsgetfav|_bbsaddfav|_bbslink|_bbsloc|_bbsclan|_bbsmemo|_maillist|_friendlist).*", 32);
	}

	public enum BypassType
	{
		DEFAULT,
		BBS,
		ITEM;

		public static final BypassType[] VALUES;

		static
		{
			VALUES = values();
		}
	}

	public static class ValidBypass
	{
		public String bypass;
		public boolean args;
		public BypassType type;

		public ValidBypass(String bypass, boolean args, BypassType type)
		{
			this.bypass = bypass;
			this.args = args;
			this.type = type;
		}
	}
}
