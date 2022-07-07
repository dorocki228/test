package l2s.Phantoms.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;

public class PhantomParser
{
	private final Logger _log = LoggerFactory.getLogger(PhantomParser.class);
	private static final PhantomParser _instance = new PhantomParser();

	public static PhantomParser getInstance()
	{
		return _instance;
	}

	public List<String> _Phantomsdeadtalk = new ArrayList<String>();// фразы после смерти
	public List<String> _PhantomssayPhantoms = new ArrayList<String>();// фразы в чат
	public List<String> _PhantomsTitlePhantoms = new ArrayList<String>();// титулы

	public PhantomParser()
	{
		cachedeadtalk();
		cachesayPhantoms();
		cacheTitlePhantoms();
	}

	// кеш фраз фантомов
	public void cachesayPhantoms()
	{
		_PhantomssayPhantoms.clear();
		LineNumberReader lnr = null;
		BufferedReader br = null;
		FileReader fr = null;
		try
		{
			File Data = new File("./config/Phantom/Chat/sayPhantoms.talk");
			if(!Data.exists())
				return;
			fr = new FileReader(Data);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			String line;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				_PhantomssayPhantoms.add(line);
			}
			_log.info("Load " + (_PhantomssayPhantoms.size() - 1) + " phantom say");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(fr != null)
					fr.close();
				if(br != null)
					br.close();
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
	}

	// кеш титулов фантомов
	public void cacheTitlePhantoms()
	{
		_PhantomsTitlePhantoms.clear();
		LineNumberReader lnr = null;
		BufferedReader br = null;
		FileReader fr = null;
		try
		{
			File Data = new File("./config/Phantom/title.properties");
			if(!Data.exists())
				return;
			fr = new FileReader(Data);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			String line;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				_PhantomsTitlePhantoms.add(line);
			}
			_log.info("Load " + (_PhantomsTitlePhantoms.size() - 1) + " phantom Title");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(fr != null)
					fr.close();
				if(br != null)
					br.close();
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
	}

	public void cachedeadtalk()
	{
		_Phantomsdeadtalk.clear();
		LineNumberReader lnr = null;
		BufferedReader br = null;
		FileReader fr = null;
		try
		{
			File Data = new File("./config/Phantom/Chat/dead.talk");
			if(!Data.exists())
				return;
			fr = new FileReader(Data);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			String line;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				_Phantomsdeadtalk.add(line);
			}
			_log.info("Load " + (_Phantomsdeadtalk.size() - 1) + " phantom Last");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(fr != null)
					fr.close();
				if(br != null)
					br.close();
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
	}

	public String getRandomdeadtalk()
	{
		return Rnd.get(_Phantomsdeadtalk);
	}

	public String getRandomTitleColor()
	{
		return Rnd.get(Config.PHANTOM_PLAYERS_TITLE_CLOLORS);
	}

	public String getRandomNameColor()
	{
		return Rnd.get(Config.PHANTOM_PLAYERS_NAME_CLOLORS);
	}

	public String getRandomTitlePhantoms()
	{
		return Rnd.get(_PhantomsTitlePhantoms);
	}

	public String getRandomSayPhantoms()
	{
		return Rnd.get(_PhantomssayPhantoms);
	}

}
