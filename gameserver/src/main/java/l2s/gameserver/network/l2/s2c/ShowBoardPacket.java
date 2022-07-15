package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.tables.FakePlayersTable;
import l2s.gameserver.utils.BypassStorage.BypassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;

public class ShowBoardPacket implements IClientOutgoingPacket
{
	public static final int HTML_BUCKET_SIZE = 16250;

	private static final Logger _log = LoggerFactory.getLogger(ShowBoardPacket.class);

	public static IClientOutgoingPacket CLOSE = new ShowBoardPacket();

	private static final String[] DIRECT_BYPASS = new String[] {
			"bypass _bbshome",
			"bypass _cbbsstatistic_pk",
			"bypass _bbspage:shop",
			"bypass _cbbsbuffer 0",
			"bypass _cbbsteleport",
			"bypass _bbspage:services",
			"bypass _cbbsservices_cabinet" };

	private boolean _show;
	private String _html;
	private String _fav;

	public static void separateAndSend(String html, Player player)
	{
		if(html == null || html.isEmpty())
			return;

		String fav = "";
		if(player.getSessionVar("add_fav") != null)
			fav = "bypass _bbsaddfav_List";

		html = player.getBypassStorage().parseHtml(html, BypassType.BBS, true);

		html = html.replace("<?copyright?>", Config.BBS_COPYRIGHT);
		html = html.replace("<?total_online?>", String.valueOf(GameObjectsStorage.getPlayers(true, true).size() + FakePlayersTable.getActiveFakePlayersCount()));

		Matcher m = ImagesCache.HTML_PATTERN.matcher(html);
		while(m.find())
		{
			String imageName = m.group(1);
			int imageId = ImagesCache.getInstance().getImageId(imageName);
			html = html.replaceAll("%image:" + imageName + "%", "Crest.pledge_crest_" + Config.REQUEST_ID + "_" + imageId);
			byte[] image = ImagesCache.getInstance().getImage(imageId);
			if(image != null)
				player.sendPacket(new PledgeCrestPacket(imageId, image));
		}

		if(html.length() < HTML_BUCKET_SIZE)
		{
			player.sendPacket(new ShowBoardPacket("101", html, fav));
			player.sendPacket(new ShowBoardPacket("102", "", fav));
			player.sendPacket(new ShowBoardPacket("103", "", fav));
		}
		else if(html.length() < HTML_BUCKET_SIZE * 2)
		{
			player.sendPacket(new ShowBoardPacket("101", html.substring(0, HTML_BUCKET_SIZE), fav));
			player.sendPacket(new ShowBoardPacket("102", html.substring(HTML_BUCKET_SIZE, html.length()), fav));
			player.sendPacket(new ShowBoardPacket("103", "", fav));
		}
		else if(html.length() < HTML_BUCKET_SIZE * 3)
		{
			player.sendPacket(new ShowBoardPacket("101", html.substring(0, HTML_BUCKET_SIZE), fav));
			player.sendPacket(new ShowBoardPacket("102", html.substring(HTML_BUCKET_SIZE, HTML_BUCKET_SIZE * 2), fav));
			player.sendPacket(new ShowBoardPacket("103", html.substring(HTML_BUCKET_SIZE * 2, html.length()), fav));
		}
		else
			throw new IllegalArgumentException("Html is too long!");
	}

	public static void separateAndSend(String html, List<String> arg, Player player)
	{
		String fav = "";
		if(player.getSessionVar("add_fav") != null)
			fav = "bypass _bbsaddfav_List";

		html = player.getBypassStorage().parseHtml(html, BypassType.BBS, true);

		html = html.replace("<?copyright?>", Config.BBS_COPYRIGHT);
		html = html.replace("<?total_online?>", String.valueOf(GameObjectsStorage.getPlayers(true, true).size() + FakePlayersTable.getActiveFakePlayersCount()));

		Matcher m = ImagesCache.HTML_PATTERN.matcher(html);
		while(m.find())
		{
			String imageName = m.group(1);
			int imageId = ImagesCache.getInstance().getImageId(imageName);
			html = html.replaceAll("%image:" + imageName + "%", "Crest.pledge_crest_" + Config.REQUEST_ID + "_" + imageId);
			byte[] image = ImagesCache.getInstance().getImage(imageId);
			if(image != null)
				player.sendPacket(new PledgeCrestPacket(imageId, image));
		}

		if(html.length() < HTML_BUCKET_SIZE)
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

	private ShowBoardPacket(String id, List<String> arg, String fav)
	{
		_show = true;
		_html = id + "\u0008";
		for(String a : arg)
			_html += a + " \u0008";
	}

	private ShowBoardPacket()
	{
		_show = false;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOW_BOARD.writeId(packetWriter);
		packetWriter.writeC(_show); //c4 1 to show community 00 to hide
		if(_show)
		{
			for(String bbsBypass : DIRECT_BYPASS)
				packetWriter.writeS(bbsBypass);
			packetWriter.writeS(_fav);
			packetWriter.writeS(_html);
		}

		return true;
	}
}