package l2s.gameserver.utils;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SysString;

public class HtmlUtils
{
	public static final String PREV_BUTTON = "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	public static final String NEXT_BUTTON = "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";

	private static final HtmlCompressor HTML_COMPRESSOR = createHtmlCompressor();

	public static String htmlResidenceName(int id)
	{
		return "&%" + id + ";";
	}

	public static String htmlNpcName(int npcId)
	{
		return "&@" + npcId + ";";
	}

	public static String htmlSysString(SysString sysString)
	{
		return htmlSysString(sysString.getId());
	}

	public static String htmlSysString(int id)
	{
		return "&$" + id + ";";
	}

	public static String htmlItemName(int itemId)
	{
		return "&#" + itemId + ";";
	}

	public static String htmlClassName(int classId)
	{
		return "<ClassId>" + classId + "</ClassId>";
	}

	public static String htmlNpcString(NpcString id, Object... params)
	{
		return htmlNpcString(id.getId(), params);
	}

	public static String htmlNpcString(int id, Object... params)
	{
		String replace = "<fstring";
		if(params.length > 0)
			for(int i = 0; i < params.length; ++i)
				replace = replace + " p" + (i + 1) + "=\"" + params[i] + "\"";
		replace = replace + ">" + id + "</fstring>";
		return replace;
	}

	public static String htmlCombobox(String var, String list, int width)
	{
		return String.format("<combobox var=\"%s\" list=\"%s\" width=%s>", var, list, width);
	}

	public static String htmlButton(String value, String action, int width)
	{
		return htmlButton(value, action, width, 22);
	}

	public static String htmlButton(String value, String action, int width, int height)
	{
		return String.format("<button value=\"%s\" action=\"%s\" back=\"L2UI_CT1.Button_DF_Small_Down\" width=%d height=%d fore=\"L2UI_CT1.Button_DF_Small\">", value, action, width, height);
	}

	public static String iconImg(String icon)
	{
		return "<img src=icon." + icon + " width=32 height=32>";
	}

	public static String getGauge(int width, long current, long max, boolean displayAsPercentage, String backgroundImage, String image, int imageHeight, long top) {
		current = Math.min(current, max);
		StringBuilder sb = new StringBuilder();
		sb.append("<table width=");
		sb.append(width);
		sb.append(" cellpadding=0 cellspacing=0>");
		sb.append("<tr>");
		sb.append("<td background=\"");
		sb.append(backgroundImage);
		sb.append("\">");
		sb.append("<img src=\"");
		sb.append(image);
		sb.append("\" width=");
		sb.append((long) (((double) current / max) * width));
		sb.append(" height=");
		sb.append(imageHeight);
		sb.append(">");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td align=center>");
		sb.append("<table cellpadding=0 cellspacing=");
		sb.append(top);
		sb.append(">");
		sb.append("<tr>");
		sb.append("<td>");
		if (displayAsPercentage)
		{
			sb.append("<table cellpadding=0 cellspacing=2>");
			sb.append("<tr><td>&nbsp;");
			sb.append(String.format("%.0f%%", ((double) current / max) * 100));
			sb.append("</td></tr>");
			sb.append("</table>");
		}
		else
		{
			final int tdWidth = (width - 10) / 2;
			sb.append("<table cellpadding=0 cellspacing=0>");
			sb.append("<tr>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(" align=right>");
			sb.append(current);
			sb.append("</td>");
			sb.append("<td width=10 align=center>/</td>");
			sb.append("<td width=");
			sb.append(tdWidth);
			sb.append(">");
			sb.append(max);
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}

	public static String bbParse(String s)
	{
		if(s == null)
			return null;
		s = s.replace("\r", "");
		s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"LEVEL\">$2</font>$3");
		s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\$(.*?)\\$(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"00FFFF\">$2</font>$3");
		s = Strings.replace(s, "^!(.*?)$", 8, "<font color=\"FFFFFF\">$1</font>\n\n");
		s = s.replaceAll("%%\\s*\n", "<br1>");
		s = s.replaceAll("\n\n+", "<br>");
		s = Strings.replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", 32, "<a action=\"bypass -h $1\">$2</a>");
		s = s.replaceAll(" @", "\" msg=\"");
		s = s.replaceAll("\n", "");
		return s;
	}

	public static void sendHtm(Player player, String htm)
	{
		player.sendPacket(new HtmlMessage(5).setHtml(htm));
	}

	public static void sendHtmFile(Player player, String fileName)
	{
		player.sendPacket(new HtmlMessage(5).setFile(fileName));
	}

	public static String compress(String content)
	{
		return HTML_COMPRESSOR.compress(content);
	}

	private static HtmlCompressor createHtmlCompressor()
	{
		final HtmlCompressor htmlCompressor = new HtmlCompressor();

		htmlCompressor.setEnabled(true);                   //if false all compression is off (default is true)
		htmlCompressor.setRemoveComments(false);            //if false keeps HTML comments (default is true)
		htmlCompressor.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
		htmlCompressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
		htmlCompressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
		htmlCompressor.setSimpleDoctype(false);             //simplify existing doctype
		htmlCompressor.setRemoveScriptAttributes(false);    //remove optional attributes from script tags
		htmlCompressor.setRemoveStyleAttributes(false);     //remove optional attributes from style tags
		htmlCompressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
		htmlCompressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
		htmlCompressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
		htmlCompressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
		htmlCompressor.setRemoveJavaScriptProtocol(false);  //remove "javascript:" from inline event handlers
		htmlCompressor.setRemoveHttpProtocol(false);        //replace "http://" with "//" inside tag attributes
		htmlCompressor.setRemoveHttpsProtocol(false);       //replace "https://" with "//" inside tag attributes
		htmlCompressor.setPreserveLineBreaks(false);        //preserves original line breaks
		htmlCompressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX); //remove spaces around provided tags

		htmlCompressor.setCompressCss(false);               //compress inline css
		htmlCompressor.setCompressJavaScript(false);        //compress inline javascript

		htmlCompressor.setGenerateStatistics(false);

		return htmlCompressor;
	}
}
