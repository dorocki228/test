package l2s.gameserver.config.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.config.templates.HostInfo;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;

public final class HostsConfigParser extends AbstractParser<HostsConfigHolder>
{
	private static final Logger _log = LoggerFactory.getLogger(HostsConfigParser.class);
	private static final HostsConfigParser _instance = new HostsConfigParser();

	public static HostsConfigParser getInstance()
	{
		return _instance;
	}

	protected HostsConfigParser()
	{
		super(HostsConfigHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File("config/hostsconfig.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "hostsconfig.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			if("authserver".equalsIgnoreCase(element.getName()))
			{
				String ip = element.attributeValue("ip");
				int port = Integer.parseInt(element.attributeValue("port"));
				getHolder().setAuthServerHost(new HostInfo(ip, port));
			}
			else
			{
				if(!"gameserver".equalsIgnoreCase(element.getName()))
					continue;
				Iterator<Element> subIterator = element.elementIterator("host");
				while(subIterator.hasNext())
				{
					Element subElement = subIterator.next();
					int id = Integer.parseInt(subElement.attributeValue("id"));
					String ip = subElement.attributeValue("ip");
					String inner_ip = subElement.attributeValue("inner_ip");
					int port2 = Integer.parseInt(subElement.attributeValue("port"));
					String key = subElement.attributeValue("key");
					getHolder().addGameServerHost(new HostInfo(id, ip, inner_ip, port2, key));
				}
			}
		}
	}

	@Override
	protected void afterParseActions()
	{
		if(getHolder().getAuthServerHost() == null)
		{
			_log.error("Could not load authserver host config. Configure your hostsconfig.xml!");
			Runtime.getRuntime().exit(0);
		}
		if(getHolder().getGameServerHosts().length == 0)
		{
			_log.error("Could not load gameserver host config. Configure your hostsconfig.xml!");
			Runtime.getRuntime().exit(0);
		}
	}
}
