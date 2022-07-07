package  l2s.Phantoms.ai.chat;

import java.io.File;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import  l2s.commons.util.Rnd;
import  l2s.gameserver.tables.GmListTable;

public class ChatHelper
{
	private static final Logger _log = LoggerFactory.getLogger(ChatHelper.class);
	
	private static ChatHelper _instance;
	public static ChatHelper getInstance()
	{
		if (_instance == null)
			_instance = new ChatHelper();
		return _instance;
	}
	
	private Map<String, String> PATTERNS_FOR_ANALYSIS = new HashMap<String, String>();
	private Map<String, List<String>> ANSWERS_BY_PATTERNS = new HashMap<String, List<String>>();

	public String sayInReturn(String msg)
	{
		GmListTable.broadcastMessageToGMs(msg);
			String message = String.join(" ", msg.toLowerCase().split("[ {,|.}?]+"));
			for (Map.Entry<String, String> o : PATTERNS_FOR_ANALYSIS.entrySet())
			{
				Pattern pattern = Pattern.compile(o.getKey());
				if (pattern.matcher(message).find())
						return Rnd.get(ANSWERS_BY_PATTERNS.get(o.getValue()));
			}
			return "";
	}
	
	
	public void load()
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			final File file = new File("config/Phantom/Chat/ChatHelper.xml");
			if (!file.exists())
			{
				_log.error("Not found file ChatHelper.xml");
			}
			final Document document = factory.newDocumentBuilder().parse(file);
			for(Node firstNode = document.getFirstChild(); firstNode != null; firstNode = firstNode.getNextSibling())
			{
				if ("list".equalsIgnoreCase(firstNode.getNodeName()))
				{
					for(Node secondNode = firstNode.getFirstChild(); secondNode != null; secondNode = secondNode.getNextSibling())
					{
						if ("patterns".equalsIgnoreCase(secondNode.getNodeName()))
						{
							for(Node thirdNode  = secondNode.getFirstChild(); thirdNode != null; thirdNode = thirdNode.getNextSibling())
							{
								if ("item".equalsIgnoreCase(thirdNode.getNodeName()))
								{
									for(Node answerNode  = thirdNode.getFirstChild(); answerNode != null; answerNode = answerNode.getNextSibling())
									{
										if ("pattern".equalsIgnoreCase(answerNode.getNodeName()))
											PATTERNS_FOR_ANALYSIS.put(answerNode.getAttributes().getNamedItem("RegEx").getNodeValue(),thirdNode.getAttributes().getNamedItem("group").getNodeValue());
									}
								}
							}
						}
						if ("AnswersByPatterns".equalsIgnoreCase(secondNode.getNodeName()))
						{
							for(Node thirdNode  = secondNode.getFirstChild(); thirdNode != null; thirdNode = thirdNode.getNextSibling())
							{
								if ("group".equalsIgnoreCase(thirdNode.getNodeName()))
								{
									List<String> answerlist = new ArrayList<String>();
									for(Node answerNode  = thirdNode.getFirstChild(); answerNode != null; answerNode = answerNode.getNextSibling())
									{
										if ("text".equalsIgnoreCase(answerNode.getNodeName()))
											answerlist.add( answerNode.getAttributes().getNamedItem("answer").getNodeValue());
									}
									ANSWERS_BY_PATTERNS.put(thirdNode.getAttributes().getNamedItem("name").getNodeValue(), answerlist);
								}
							}
						}
					}
				}
			}
			if (PATTERNS_FOR_ANALYSIS.size() == 0|| ANSWERS_BY_PATTERNS.size() == 0)
				_log.error("Error loading ChatHelper.xml");
			else
				_log.info("PATTERNS_FOR_ANALYSIS:" + PATTERNS_FOR_ANALYSIS.size() + " ANSWERS_BY_PATTERNS:"+  ANSWERS_BY_PATTERNS.size());

		}catch(Exception e)
		{
			_log.error("Error loading ChatHelper.xml {}", e.getLocalizedMessage());
		}
	}
	
}