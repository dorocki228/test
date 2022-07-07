package l2s.Phantoms.parsers.Nickname;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import l2s.Phantoms.PhantomVariables;
import l2s.Phantoms.objects.Nickname;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.utils.Util;

public class NicknameParser
{
	private final Logger _log = LoggerFactory.getLogger(NicknameParser.class);

	private static NicknameParser _instance = new NicknameParser();
	public List<Nickname> _Nick = new ArrayList<Nickname>();

	public static NicknameParser getInstance()
	{
		if (_instance == null)
		{
			_instance = new NicknameParser();
		}
		return _instance;
	}

	// в момент первого запуска создадим записи в бд, "забронируем ники" ботам
	public void createRecords()
	{
		if (!PhantomVariables.getBool("createPhantomRecords",false))
		{
			for(Nickname n :_Nick)
			{
				if (!Util.isMatchingRegexp(n.getName(), Config.CNAME_TEMPLATE)) // не проходит проверку
				{
					_log.info("createRecords err №1");
					continue;
				}
				if (CharacterDAO.getInstance().getObjectIdByName(n.getName()) > 0)// уже создан
				{
					//_log.info("createRecords err №2");
					continue;
				}
				//создаем записи в бд
				insert(n.getName(), IdFactory.getInstance().getNextId(), 1);

			}
			PhantomVariables.set("createPhantomRecords", true);
		}
	}
	public boolean insert(String name, int objId, final int classId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name) VALUES (?,?,?)");
			statement.setString(1, "rebellion");
			statement.setInt(2, objId);
			statement.setString(3, name);
			statement.executeUpdate();
			statement.close();

			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, level, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp,  active, type) VALUES (?,?,1,10,10,10,10,10,10,10,1,1,0)");
			statement.setInt(1, objId);
			statement.setInt(2, classId);
			statement.executeUpdate();

		}catch(final Exception e)
		{
			_log.error("", e);
			return false;
		}finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public List<Nickname> getNicknames()
	{
		return _Nick;
	}

	public Stream<Nickname> getNicknames(int class_id)
	{
		return _Nick.stream().filter(n -> n.containsClassId(class_id));
	}

	public void RemoveNick(String name)
	{
		Iterator<Nickname> nameIterator = _Nick.iterator();
		while (nameIterator.hasNext())
		{
			Nickname nextCat = nameIterator.next();
			if (nextCat.getName().equals(name))
				nameIterator.remove();
		}
	}

	public void loadNickAndTitle()
	{
		Collection<File> files = FileUtils.listFiles(new File(Config.DATAPACK_ROOT, "config/Phantom/PhantomNick/"), FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());
		for (File f : files)
			if (!f.isHidden())
				try
				{
					_Nick.addAll(unmarshalling(f));
				}
				catch (Exception e)
				{
					_log.info("Exception: " + e + " in file: " + f.getName(), e);
				}
		_log.info("Load " + _Nick.size() + " phantom nickname");
	}

	// чтение
	@SuppressWarnings("unchecked")
	public List<Nickname> unmarshalling(File file) throws IOException, ClassNotFoundException
	{
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		xStream.allowTypesByRegExp(new String[] { ".*" });
		xStream.alias("list", List.class);
		xStream.alias("phantom", Nickname.class);
		xStream.aliasAttribute(Nickname.class, "name", "name");
		xStream.registerLocalConverter(Nickname.class, "classId", new ClassListConverter());
		xStream.aliasAttribute(Nickname.class, "classId", "classId");
		xStream.aliasAttribute(Nickname.class, "sex", "sex");
		xStream.registerConverter((Converter) new EncodedByteArrayConverter());
		return (ArrayList<Nickname>) xStream.fromXML(file);
	}

}
