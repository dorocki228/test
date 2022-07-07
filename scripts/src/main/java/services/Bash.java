package services;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис трансляции цитат с сайта bash.org.ru в игру
 * @Author: SYS
 */
public class Bash implements IAdminCommandHandler, OnInitScriptListener
{
	private static final Logger _log = LoggerFactory.getLogger(Bash.class);

	private enum Commands
	{
		admin_bashreload
	}

	private static final String wrongPage = "scripts/services/Bash-wrongPage.htm";
	private static final String notPage = "scripts/services/Bash-notPage.htm";
	private static final String readPage = "scripts/services/Bash-readPage.htm";

	private static final Path xmlData = Paths.get(Config.DATAPACK_ROOT + "/data/bash.xml");
	private static final List<String> quotes = new ArrayList<>();

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		if(!activeChar.getPlayerAccess().IsEventGm)
			return false;

		loadData();
		activeChar.sendMessage("Bash service reloaded.");

		return true;
	}

	@Bypass("services.Bash:showQuote")
	public void showQuote(Player player, NpcInstance npc, String[] param)
	{
		if(player == null || npc == null)
			return;

		if(!npc.canBypassCheck(player))
			return;

		int page;
		int totalPages = quotes.size();

		try
		{
			page = Integer.parseInt(param[0]);
		}
		catch(NumberFormatException e)
		{
			Functions.show(HtmCache.getInstance().getHtml(wrongPage, player) + navBar(1, totalPages), player, npc);
			return;
		}

		if(page > totalPages && page == 1)
		{
			Functions.show(notPage, player, npc);
			return;
		}

		if(page > totalPages || page < 1)
		{
			Functions.show(HtmCache.getInstance().getHtml(wrongPage, player) + navBar(1, totalPages), player, npc);
			return;
		}

		String html = HtmCache.getInstance().getHtml(readPage, player);
		html = html.replaceFirst("%quote%", quotes.get(page - 1));
		html = html.replaceFirst("%page%", String.valueOf(page));
		html = html.replaceFirst("%total_pages%", String.valueOf(totalPages));
		html += navBar(page, totalPages);
		Functions.show(html, player, npc);
	}

	private int parseRSS()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		Document doc = null;
		try
		{
			doc = factory.newDocumentBuilder().parse(xmlData.toFile());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(doc == null)
			return 0;

		quotes.clear();

		int quotesCounter = 0;
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if("rss".equalsIgnoreCase(n.getNodeName()))
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if("channel".equalsIgnoreCase(d.getNodeName()))
						for(Node i = d.getFirstChild(); i != null; i = i.getNextSibling())
							if("item".equalsIgnoreCase(i.getNodeName()))
								for(Node z = i.getFirstChild(); z != null; z = z.getNextSibling())
									if("description".equalsIgnoreCase(z.getNodeName()))
									{
										//Убираем лишние обратные слэши и знаки $
										quotes.add(z.getTextContent().replaceAll("\\\\", "").replaceAll("\\$", ""));
										quotesCounter++;
									}
		return quotesCounter;
	}

	public String getPage(String url_server, String url_document)
	{
		StringBuilder buf = new StringBuilder();
        try
		{
            Socket s;
            try
			{
				s = new Socket(url_server, 80);
			}
			catch(Exception e)
			{
				return null;
			}

			s.setSoTimeout(30000); //Таймут 30 секунд
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "Cp1251"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));

			out.print("GET http://" + url_server + "/" + url_document + " HTTP/1.1\r\n" + //
					"User-Agent: MMoCore\r\n" + //
					"Host: " + url_server + "\r\n" + //
					"Accept: */*\r\n" + //
					"Connection: close\r\n" + //
					"\r\n");
			out.flush();

			boolean header = true;
			for(String line = in.readLine(); line != null; line = in.readLine())
			{
				if(header && line.startsWith("<?xml "))
					header = false;
				if(!header)
					buf.append(line).append("\r\n");
				if(!header && line.startsWith("</rss>"))
					break;
			}

			s.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return buf.toString();
	}

	private String navBar(int curPage, int totalPages)
	{
        String html = "<br><center><table border=0 width=240><tr><td widht=30>";
        if(curPage > 1)
			html += "<a action=\"bypass -h htmbypass_services.Bash:showQuote " + (curPage - 1) + "\">";
		html += "&lt;&lt;&lt; Назад";
		if(curPage > 1)
			html += "</a>";
		html += "</td><td widht=160>&nbsp;[" + curPage + "]&nbsp;</td><td widht=40>";
		if(curPage < totalPages)
			html += "<a action=\"bypass -h htmbypass_services.Bash:showQuote " + (curPage + 1) + "\">";
		html += "Вперед &gt;&gt;&gt;";
		if(curPage < totalPages)
			html += "</a>";
		html += "</td></tr></table></center>";
		html += "<table border=0 width=240><tr><td width=150>";
		html += "Перейти на страницу:</td><td><edit var=\"page\" width=40></td><td>";
		html += "<button value=\"перейти\" action=\"bypass -h htmbypass_services.Bash:showQuote $page\" width=60 height=20>";
		html += "</td></tr></table>";
		return html;
	}

	public void loadData()
	{
		if(Config.SERVICES_BASH_RELOAD_TIME > 0)
			ThreadPoolManager.getInstance().schedule(() -> loadData(), Config.SERVICES_BASH_RELOAD_TIME * 60 * 60 * 1000L);

		// Скачиваем файл и сохраняем его на диске
		String data;
		try
		{
			data = getPage("bash.org.ru", "rss/");
		}
		catch(Exception E)
		{
			data = null;
		}
		if(data == null)
		{
			_log.info("Service: Bash - RSS data download failed.");
			return;
		}
		data = data.replaceFirst("windows-1251", "utf-8");

		if(!Config.SERVICES_BASH_SKIP_DOWNLOAD)
		{
			try
			{
				Files.write(xmlData, data.getBytes(StandardCharsets.UTF_8));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			_log.info("Service: Bash - RSS data download completed.");
		}

		int parse = parseRSS();
		if(parse == 0)
		{
			_log.warn("Service: Bash - RSS data parse error.");
			return;
		}
		_log.info("Service: Bash - RSS data parsed: loaded " + parse + " quotes.");
	}

	@Override
	public void onInit()
	{
		_log.info("Loaded Service: Bash [" + (Config.SERVICES_BASH_ENABLED ? "enabled]" : "disabled]"));
		if(Config.SERVICES_BASH_ENABLED)
		{
			AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
			loadData();
		}
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}