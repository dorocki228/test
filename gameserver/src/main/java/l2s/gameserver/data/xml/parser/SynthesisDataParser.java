package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SynthesisDataHolder;
import l2s.gameserver.templates.item.support.SynthesisData;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class SynthesisDataParser extends AbstractParser<SynthesisDataHolder>
{
	private static final SynthesisDataParser _instance = new SynthesisDataParser();

	private SynthesisDataParser()
	{
		super(SynthesisDataHolder.getInstance());
	}

	public static SynthesisDataParser getInstance()
	{
		return _instance;
	}

	@Override
	public String getDTDFileName()
	{
		return "synthesis_data.dtd";
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/synthesis_data.xml");
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("synthesis");
		while(iterator.hasNext())
		{
			Element synthesisElement = iterator.next();
			int slotone = Integer.parseInt(synthesisElement.attributeValue("slotone"));
			int slottwo = Integer.parseInt(synthesisElement.attributeValue("slottwo"));
			double chance = Double.parseDouble(synthesisElement.attributeValue("chance"));
			int successId = Integer.parseInt(synthesisElement.attributeValue("successId"));
			int failId = Integer.parseInt(synthesisElement.attributeValue("failId"));
			int failCount = Integer.parseInt(synthesisElement.attributeValue("failCount"));
			getHolder().addData(new SynthesisData(slotone, slottwo, chance, successId, failId, failCount));
		}
	}
}
