package l2s.gameserver.tables;

import static com.google.common.flogger.LazyArgs.lazy;

import com.google.common.flogger.FluentLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.xml.holder.FakePlayersHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.network.l2.c2s.CharacterCreate;
import l2s.gameserver.network.l2.c2s.EnterWorld;
import l2s.gameserver.utils.TradeHelper;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public class FakePlayersTable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static class Task implements Runnable
	{
		public void run()
		{
			try
			{
				if(_activeFakePlayers.size() < GameObjectsStorage.getPlayers(true, false).size() * Config.FAKE_PLAYERS_PERCENT / 100 && _activeFakePlayers.size() < _fakePlayerNames.size())
				{
					if(Rnd.chance(10))
					{
						String player = Rnd.get(_fakePlayerNames);
						if(player != null && !_activeFakePlayers.contains(player))
							_activeFakePlayers.add(player);
					}
				}
				else if(_activeFakePlayers.size() > 0)
					_activeFakePlayers.remove(Rnd.get(_activeFakePlayers.size()));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	

	private static final List<String> _fakePlayerNames = new ArrayList<String>();
	private static final List<String> _activeFakePlayers = new ArrayList<String>();

	private static FakePlayersTable _instance;

	public static FakePlayersTable getInstance()
	{
		if(_instance == null)
			new FakePlayersTable();
		return _instance;
	}

	public FakePlayersTable()
	{
		_instance = this;
		if(Config.ALLOW_FAKE_PLAYERS || Config.FAKE_PLAYERS_COUNT > 0)
		{
			parseData();
			if(Config.FAKE_PLAYERS_COUNT <= 0)
				ThreadPoolManager.getInstance().scheduleAtFixedRate(new Task(), 180000, 1000);
			else
			{
				final int fakePlayersCount = Config.FAKE_PLAYERS_COUNT;

				if(fakePlayersCount <= 0)
				{
					if(Config.ALLOW_FAKE_PLAYERS)
						ThreadPoolManager.getInstance().scheduleAtFixedRate(new Task(), 180000, 1000);
					return;
				}

				ThreadPoolManager.getInstance().schedule(() -> {
					final IntSet restoredPlayers = new HashIntSet();

					Connection connection = null;
					PreparedStatement statement = null;
					ResultSet set = null;
					int count = 0;
					try
					{
						connection = DatabaseFactory.getInstance().getConnection();
						statement = connection.prepareStatement("SELECT obj_Id FROM characters WHERE account_name=?");
						statement.setString(1, "#fake_account");
						set = statement.executeQuery();
						while(set.next())
						{
							restoredPlayers.add(set.getInt(1));
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						DbUtils.closeQuietly(connection, statement, set);
					}

					final List<ClassId> classes = new ArrayList<ClassId>();
					for(ClassId c : ClassId.values())
					{
						if(c.isOfLevel(ClassLevel.NONE) && FakePlayersHolder.getInstance().getAITemplate(c.getRace(), c.getType()) != null)
							classes.add(c);
					}

					final List<String> names = new ArrayList<String>(getFakePlayerNames());

					if(!restoredPlayers.isEmpty() || !classes.isEmpty() && !names.isEmpty())
					{
						loop: for(; count < fakePlayersCount; count++)
						{
							if(!restoredPlayers.isEmpty() && Rnd.chance(95))
							{
								int objectId = Rnd.get(restoredPlayers.toArray());
								if(restoredPlayers.remove(objectId))
								{
									Player player = Player.restore(objectId, true);
									if(player != null)
									{
										EnterWorld.onEnterWorld(player);

										try
										{
											Thread.sleep(Rnd.get(30000, 90000));
										}
										catch(InterruptedException e)
										{
											e.printStackTrace();
										}
										continue;
									}
								}
							}

							if(names.isEmpty())
								break;

							String name = Rnd.get(names);
							while(CharacterDAO.getInstance().getObjectIdByName(name) > 0)
							{
								names.remove(name);

								if(names.isEmpty())
									break loop;

								name = Rnd.get(names);
							}
							names.remove(name);

							ClassId classId = Rnd.get(classes);
							Player player = Player.create(classId.getId(), Rnd.get(0, 1), "#fake_account", name, Rnd.get(3), Rnd.get(3), Rnd.get(3));
							CharacterCreate.initNewChar(player);

							player = Player.restore(player.getObjectId(), true);
							if(player == null)
								continue;

							EnterWorld.onEnterWorld(player);

							try
							{
								Thread.sleep(Rnd.get(60000, 180000));
							}
							catch(InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
				}, 30000);
			}
		}
	}

	public static void spawnNewFakePlayer()
	{
		List<ClassId> classes = new ArrayList<ClassId>();
		for(ClassId c : ClassId.values())
		{
			if(c.isOfLevel(ClassLevel.NONE) && FakePlayersHolder.getInstance().getAITemplate(c.getRace(), c.getType()) != null)
				classes.add(c);
		}

		List<String> names = new ArrayList<String>(getFakePlayerNames());
		if(!classes.isEmpty() && !names.isEmpty())
		{
			String name = Rnd.get(names);
			while(CharacterDAO.getInstance().getObjectIdByName(name) > 0)
			{
				names.remove(name);

				if(names.isEmpty())
					return;

				name = Rnd.get(names);
			}
			names.remove(name);

			ClassId classId = Rnd.get(classes);
			Player player = Player.create(classId.getId(), Rnd.get(0, 1), "#fake_account", name, Rnd.get(3), Rnd.get(3), Rnd.get(3));
			CharacterCreate.initNewChar(player);

			player = Player.restore(player.getObjectId(), true);
			if(player == null)
				return;

			EnterWorld.onEnterWorld(player);
		}
	}

	private static void parseData()
	{
		LineNumberReader lnr = null;
		try
		{
			File doorData = new File(Config.FAKE_PLAYERS_LIST);
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));
			String line;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;

				_fakePlayerNames.add(line);
			}
			_log.atInfo().log( "FakePlayersTable: Loaded %s fake player names.", lazy(() -> _fakePlayerNames.size()) );
		}
		catch(Exception e)
		{
			_log.atWarning().log( "FakePlayersTable: Lists could not be initialized." );
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{
				//
			}
		}
	}

	public static List<String> getFakePlayerNames()
	{
		return _fakePlayerNames;
	}

	public static int getActiveFakePlayersCount()
	{
		return _activeFakePlayers.size();
	}

	public static List<String> getActiveFakePlayers()
	{
		return _activeFakePlayers;
	}
}