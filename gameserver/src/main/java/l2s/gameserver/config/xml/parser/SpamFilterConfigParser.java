package l2s.gameserver.config.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.config.templates.SpamRule;
import l2s.gameserver.config.xml.holder.SpamFilterConfigHolder;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Pattern;

public final class SpamFilterConfigParser extends AbstractParser<SpamFilterConfigHolder>
{
	private static final SpamFilterConfigParser _instance = new SpamFilterConfigParser();

	public static SpamFilterConfigParser getInstance()
	{
		return _instance;
	}

	protected SpamFilterConfigParser()
	{
		super(SpamFilterConfigHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File("config/spamfilter.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "spamfilter.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			if("config".equals(element.getName()))
			{
				Iterator<Element> setIterator = element.elementIterator("set");
				while(setIterator.hasNext())
				{
					Element setElement = setIterator.next();

					if("enable".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_ENABLED = Boolean.parseBoolean(setElement.attributeValue("value"));
					else if("block_spam".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_BLOC_SPAM = Boolean.parseBoolean(setElement.attributeValue("value"));
					else if("dummy_spam".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_DUMMY_SPAM = Boolean.parseBoolean(setElement.attributeValue("value"));
					else if("notify_gm".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_NOTIFY_GM = Boolean.parseBoolean(setElement.attributeValue("value"));
					else if("logging".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_LOGGING = Boolean.parseBoolean(setElement.attributeValue("value"));
					else if("messages_to_spam".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_MESSAGES_TO_SPAM = Integer.parseInt(setElement.attributeValue("value"));
					else if("penalties_to_spam".equals(setElement.attributeValue("name")))
						Config.SPAM_FILTER_PENALTIES_TO_SPAM = Integer.parseInt(setElement.attributeValue("value"));

				}
			}
			else if("rule".equals(element.getName()))
			{
				Pattern pattern = Pattern.compile(element.attributeValue("pattern"));
				int penalty = Integer.parseInt(element.attributeValue("penalty"));

				getHolder().addRule(new SpamRule(pattern, penalty));
			}
		}
	}
}
