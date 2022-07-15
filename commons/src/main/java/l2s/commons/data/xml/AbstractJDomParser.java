package l2s.commons.data.xml;

import l2s.commons.data.xml.helpers.JDomErrorHandlerImpl;
import l2s.commons.data.xml.helpers.SimpleDTDEntityResolver;
import l2s.commons.logging.LoggerObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

/**
 * Author: VISTALL
 * Date:  18:35/30.11.2010
 */
public abstract class AbstractJDomParser<H extends AbstractHolder> extends LoggerObject
{
	protected final H _holder;

	protected String _currentFile;
	protected SAXBuilder _reader;

	protected AbstractJDomParser(H holder)
	{
		_holder = holder;
		_reader = new SAXBuilder(XMLReaders.DTDVALIDATING);
		_reader.setErrorHandler(new JDomErrorHandlerImpl(this));
	}

	public abstract File getXMLPath();

	public File getCustomXMLPath()
	{
		return null;
	}

	public abstract String getDTDFileName();

	public boolean isIgnored(File f)
	{
		return false;
	}

	public boolean isDisabled()
	{
		return false;
	}

	protected void initDTD(File f)
	{
		_reader.setEntityResolver(new SimpleDTDEntityResolver(f));
	}

	protected void parseDocument(InputStream f, String name, boolean custom) throws Exception
	{
		_currentFile = name;

		Document document = _reader.build(f);

		readData(document.getRootElement(), custom);
	}

	protected abstract void readData(Element rootElement, boolean custom) throws Exception;

	protected final void parse()
	{
		File path = getXMLPath();
		if(!path.exists())
		{
			warn("directory or file " + path.getAbsolutePath() + " not exists");
			return;
		}

		if(path.isDirectory())
		{
			File dtd = new File(path, getDTDFileName());
			if(!dtd.exists())
			{
				error("DTD file: " + dtd.getName() + " not exists.");
				return;
			}

			initDTD(dtd);

			parseDir(path, false);
			parseDir(getCustomXMLPath(), true);
		}
		else
		{
			File dtd = new File(path.getParent(), getDTDFileName());
			if(!dtd.exists())
			{
				info("DTD file: " + dtd.getName() + " not exists.");
				return;
			}

			initDTD(dtd);

			try
			{
				parseDocument(new FileInputStream(path), path.getName(), false);
			}
			catch(Exception e)
			{
				warn("Exception: " + e, e);
			}

			File customPath = getCustomXMLPath();
			if(customPath != null && customPath.exists())
			{
				try
				{
					parseDocument(new FileInputStream(customPath), customPath.getName(), true);
				}
				catch(Exception e)
				{
					warn("Exception: " + e, e);
				}
			}
		}
		onParsed();
	}

	protected void onParsed()
	{
		//
	}

	protected H getHolder()
	{
		return _holder;
	}

	public String getCurrentFileName()
	{
		return _currentFile;
	}

	public void load()
	{
		if(isDisabled())
		{
			info("disabled.");
			return;
		}

		parse();
		_holder.process();
		_holder.log();
	}
	
	public void reload()
	{
		info("reload start...");
		_holder.clear();
		load();
	}

	private void parseDir(File dir, boolean custom)
	{
		if(dir == null)
			return;

		if(!dir.exists())
		{
			warn("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}

		try
		{
			Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());

			for(File f : files)
			{
				if(!f.isHidden())
				{
					if(!isIgnored(f))
					{
						try
						{
							parseDocument(new FileInputStream(f), f.getName(), custom);
						}
						catch(Exception e)
						{
							info("Exception: " + e + " in file: " + f.getName(), e);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			warn("Exception: " + e, e);
		}
	}

	public String parseString(Element element, String param, String defaultValue) throws Exception {
		String value = element.getAttributeValue(param);
		if(value == null)
			return defaultValue;
		return value;
	}

	public String parseString(Element element, String param) throws Exception {
		return element.getAttributeValue(param);
	}

	public int parseInt(Element element, String param, int defaultValue) throws Exception {
		String value = element.getAttributeValue(param);
		if(value == null)
			return defaultValue;
		return Integer.parseInt(value);
	}

	public int parseInt(Element element, String param) throws Exception {
		return Integer.parseInt(element.getAttributeValue(param));
	}

	public long parseLong(Element element, String param, long defaultValue) throws Exception {
		String value = element.getAttributeValue(param);
		if(value == null)
			return defaultValue;
		return Long.parseLong(value);
	}

	public long parseLong(Element element, String param) throws Exception {
		return Long.parseLong(element.getAttributeValue(param));
	}

	public double parseDouble(Element element, String param, double defaultValue) throws Exception {
		String value = element.getAttributeValue(param);
		if(value == null)
			return defaultValue;
		return Double.parseDouble(value);
	}

	public double parseDouble(Element element, String param) throws Exception {
		return Double.parseDouble(element.getAttributeValue(param));
	}

	public boolean parseBoolean(Element element, String param, boolean defaultValue) throws Exception {
		String value = element.getAttributeValue(param);
		if(value == null)
			return defaultValue;
		return Boolean.parseBoolean(value);
	}

	public boolean parseBoolean(Element element, String param) throws Exception {
		return Boolean.parseBoolean(element.getAttributeValue(param));
	}
}
