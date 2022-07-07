package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.BypassStorage.BypassType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowBoardPacket extends L2GameServerPacket
{
	private static final String[] DIRECT_BYPASS = {
			"bypass _bbshome",
			"bypass _bbsgetfav",
			"bypass _bbsloc",
			"bypass _bbsclan",
			"bypass _bbsmemo",
			"bypass _maillist_0_1_0_",
			"bypass _friendlist_0_" };

	private String _html;
	private String _fav;
	private final boolean _show;

	public static L2GameServerPacket CLOSE = new ShowBoardPacket();

	public static void separateAndSend(String html, Player player)
	{
		html = html.replace("\t", "");

		// %object(htmlpath)%
		String path_file_community = Config.BBS_PATH + "/";
		Pattern p = Pattern.compile("\\%include\\(([^\\)]+)\\)\\%");
		Matcher m = p.matcher(html);
		while(m.find())
		{
			html = html.replace(m.group(0), HtmCache.getInstance().getHtml(path_file_community + m.group(1), player));
		}

		// %object(Config,BBS_FOLDER)%
		p = Pattern.compile("\\%object\\(([^\\)]+),([^\\)]+)\\)\\%");
		m = p.matcher(html);
		while(m.find())
		{
			if("Config".equals(m.group(1)))
				html = html.replace(m.group(0), Config.getField(m.group(2)));
			else
			{
				try
				{
					Object c = Class.forName(m.group(1)).getDeclaredConstructor().newInstance();
					Field field = c.getClass().getField(m.group(2));
					html = html.replace(m.group(0), field.get(c).toString());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		send(html, player);
	}

	public static void send(String html, Player player)
	{
		String fav = "";
		if(player.getSessionVarS("add_fav") != null)
			fav = "bypass _bbsaddfav_List";

		player.getBypassStorage().parseHtml(html, BypassType.BBS);

		Matcher m = ImagesCache.HTML_PATTERN.matcher(html);
		while(m.find())
		{
			String imageName = m.group(1);
			int imageId = ImagesCache.getInstance().getImageId(imageName);
			html = html.replaceAll("%image:" + imageName + "%", "Crest.crest_" + Config.REQUEST_ID + "_" + imageId);
			byte[] image = ImagesCache.getInstance().getImage(imageId);
			if(image != null)
			{
				player.sendPacket(new PledgeCrestPacket(imageId, image));
			}
		}

		if(html.length() < 8180)
		{
			player.sendPacket(new ShowBoardPacket("101", html, fav));
			player.sendPacket(new ShowBoardPacket("102", "", fav));
			player.sendPacket(new ShowBoardPacket("103", "", fav));
		}
		else if(html.length() < 8180 * 2)
		{
			player.sendPacket(new ShowBoardPacket("101", html.substring(0, 8180), fav));
			player.sendPacket(new ShowBoardPacket("102", html.substring(8180, html.length()), fav));
			player.sendPacket(new ShowBoardPacket("103", "", fav));
		}
		else if(html.length() < 8180 * 3)
		{
			player.sendPacket(new ShowBoardPacket("101", html.substring(0, 8180), fav));
			player.sendPacket(new ShowBoardPacket("102", html.substring(8180, 8180 * 2), fav));
			player.sendPacket(new ShowBoardPacket("103", html.substring(8180 * 2, html.length()), fav));
		}
		else
			throw new IllegalArgumentException("Html is too long!");
	}

	public static void separateAndSend(String html, List<String> arg, Player player)
	{
		html = html.replace("\t", "");
		String fav = "";
		if(player.getSessionVarS("add_fav") != null)
			fav = "bypass _bbsaddfav_List";

		player.setLastNpc(null);
		player.getBypassStorage().parseHtml(html, BypassType.BBS);

		Matcher m = ImagesCache.HTML_PATTERN.matcher(html);
		while(m.find())
		{
			String imageName = m.group(1);
			int imageId = ImagesCache.getInstance().getImageId(imageName);
			html = html.replaceAll("%image:" + imageName + "%", "Crest.crest_" + Config.REQUEST_ID + "_" + imageId);
			byte[] image = ImagesCache.getInstance().getImage(imageId);
			if(image != null)
			{
				player.sendPacket(new PledgeCrestPacket(imageId, image));
			}
		}

		if(html.length() < 8180)
		{
			player.sendPacket(new ShowBoardPacket("1001", html, fav));
			player.sendPacket(new ShowBoardPacket("1002", arg, fav));
		}
		else
			throw new IllegalArgumentException("Html is too long!");
	}

	private ShowBoardPacket(String id, String html, String fav)
	{
		_show = true;

		_html = id + "\u0008";
		if(html != null)
			_html += html;

		_fav = fav;
	}

	private ShowBoardPacket()
	{
		_show = false;
	}

	private ShowBoardPacket(String id, List<String> arg, String fav)
	{
		_show = true;

		_html = id + "\u0008";
		for(String a : arg)
			_html += a + " \u0008";
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_show);
		if(_show)
		{
			for(String bbsBypass : DIRECT_BYPASS)
				writeS(bbsBypass);
			writeS(_fav);
			writeS(_html);
		}
	}
}