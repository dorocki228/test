package l2s.commons.data.xml;

import l2s.commons.data.xml.helpers.ErrorHandlerImpl;
import l2s.commons.data.xml.helpers.SimpleDTDEntityResolver;
import l2s.commons.logging.LoggerObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public abstract class AbstractParser<H extends AbstractHolder> extends LoggerObject
{
	protected final H _holder;
	protected String _currentFile;
	protected SAXReader _reader;

	protected AbstractParser(H holder)
	{
		_holder = holder;
		(_reader = new SAXReader()).setValidation(true);
		_reader.setErrorHandler(new ErrorHandlerImpl(this));
	}

	public abstract File getXMLPath();

	public abstract String getDTDFileName();

	public boolean isIgnored(Path path)
	{
		return false;
	}

	protected void initDTD(File f)
	{
		_reader.setEntityResolver(new SimpleDTDEntityResolver(f));
	}

	protected void parseDocument(InputStream f, String name) throws Exception
	{
		_currentFile = name;
		Document document = _reader.read(f);
		readData(document.getRootElement());
	}

	protected abstract void readData(Element p0) throws Exception;

	protected void parse()
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
			parseDir(path);
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
				parseDocument(new FileInputStream(path), path.getName());
			}
			catch(Exception e)
			{
				warn("Exception: " + e, e);
			}
		}
		afterParseActions();
	}

	protected void afterParseActions()
	{}

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

	private void parseDir(File dir)
	{
		if(dir == null)
			return;
		if(!dir.exists())
		{
			warn("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}

        var dirPath = dir.toPath();
        try(Stream<Path> pathStream = Files.walk(dirPath))
        {
            PathMatcher matcher =
                    FileSystems.getDefault().getPathMatcher("glob:**/*.xml");

            pathStream
                    .filter(matcher::matches)
                    .filter(path ->
                    {
                        try
                        {
                            return !Files.isHidden(path) && !isIgnored(path);
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .forEach(path ->
                    {
                        try
                        {
                            parseDocument(Files.newInputStream(path), path.getFileName().toString());
                        }
                        catch(Exception e)
                        {
                            info("Exception: " + e + " in file: " + path.getFileName(), e);
                        }
                    });
        }
        catch(IOException e)
        {
			warn("Exception: " + e, e);
		}
	}
}
