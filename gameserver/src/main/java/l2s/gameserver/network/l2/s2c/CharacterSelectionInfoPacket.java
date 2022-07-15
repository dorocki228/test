package l2s.gameserver.network.l2.s2c;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.CharSelectInfoPackage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.utils.AutoBan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectionInfoPacket implements IClientOutgoingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final String _loginName;
	private final int _sessionId;
	private final CharSelectInfoPackage[] _characterPackages;
	private final boolean _hasPremiumAccount;

	public CharacterSelectionInfoPacket(GameClient client)
	{
		_loginName = client.getLogin();
		_sessionId = client.getSessionKey().getPlayOkID1();
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_hasPremiumAccount = client.getPremiumAccountType() > 0 && client.getPremiumAccountExpire() > (System.currentTimeMillis() / 1000L);
	}

	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHARACTER_SELECTION_INFO.writeId(packetWriter);
		int size = _characterPackages != null ? _characterPackages.length : 0;

		packetWriter.writeD(size);
		packetWriter.writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT); // Максимальное количество персонажей на сервере
		packetWriter.writeC(size >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT); // 0x00 - Разрешить, 0x01 - запретить. Разрешает или запрещает создание игроков
		packetWriter.writeC(0x00);
		packetWriter.writeD(0x02); // 0x01 - Выводит окно, что нужно купить игру, что создавать более 2х чаров. 0х02 - обычное лобби.
		packetWriter.writeC(0x00); // 0x01 - Предлогает купить ПА.
		packetWriter.writeC(0x00); // Balthus Knights

		long lastAccess = -1L;
		int lastUsed = -1;
		for(int i = 0; i < size; i++)
			if(lastAccess < _characterPackages[i].getLastAccess())
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}

		for(int i = 0; i < size; i++)
		{
			CharSelectInfoPackage charInfoPackage = _characterPackages[i];

			packetWriter.writeS(charInfoPackage.getName(false));
			packetWriter.writeD(charInfoPackage.getCharId()); // ?
			packetWriter.writeS(_loginName);
			packetWriter.writeD(_sessionId);
			packetWriter.writeD(charInfoPackage.getClanId());
			packetWriter.writeD(0x00); // ??

			packetWriter.writeD(charInfoPackage.getSex());
			packetWriter.writeD(charInfoPackage.getRace());
			packetWriter.writeD(charInfoPackage.getBaseClassId());

			packetWriter.writeD(Config.REQUEST_ID);

			packetWriter.writeD(charInfoPackage.getX());
			packetWriter.writeD(charInfoPackage.getY());
			packetWriter.writeD(charInfoPackage.getZ());

			packetWriter.writeF(charInfoPackage.getCurrentHp());
			packetWriter.writeF(charInfoPackage.getCurrentMp());

			packetWriter.writeQ(charInfoPackage.getSp());
			packetWriter.writeQ(charInfoPackage.getExp());
			int lvl = Experience.getLevel(charInfoPackage.getExp());
			packetWriter.writeF(Experience.getExpPercent(lvl, charInfoPackage.getExp()));
			packetWriter.writeD(lvl);

			packetWriter.writeD(charInfoPackage.getKarma());
			packetWriter.writeD(charInfoPackage.getPk());
			packetWriter.writeD(charInfoPackage.getPvP());

			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);

			packetWriter.writeD(0x00); // unk Ertheia
			packetWriter.writeD(0x00); // unk Ertheia

			for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
				packetWriter.writeD(charInfoPackage.getPaperdollItemId(PAPERDOLL_ID));

			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_RHAND)); //Внешний вид оружия (ИД Итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_LHAND)); //Внешний вид щита (ИД Итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_GLOVES)); //Внешний вид перчаток (ИД Итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_CHEST)); //Внешний вид верха (ИД Итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_LEGS)); //Внешний вид низа (ИД Итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_FEET)); //Внешний вид ботинок (ИД Итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_LRHAND)); //???
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_HAIR)); //Внешний вид шляпы (ИД итема).
			packetWriter.writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_DHAIR)); //Внешний вид маски (ИД итема).

			packetWriter.writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_CHEST));
			packetWriter.writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_LEGS));
			packetWriter.writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_HEAD));
			packetWriter.writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_GLOVES));
			packetWriter.writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_FEET));

			packetWriter.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? charInfoPackage.getSex() : charInfoPackage.getHairStyle());
			packetWriter.writeD(charInfoPackage.getHairColor());
			packetWriter.writeD(charInfoPackage.getFace());

			packetWriter.writeF(charInfoPackage.getMaxHp()); // hp max
			packetWriter.writeF(charInfoPackage.getMaxMp()); // mp max

			packetWriter.writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
			packetWriter.writeD(charInfoPackage.getClassId());
			packetWriter.writeD(i == lastUsed ? 1 : 0);

			packetWriter.writeC(Math.min(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_RHAND), 127));
			packetWriter.writeD(charInfoPackage.getPaperdollVariation1Id(Inventory.PAPERDOLL_RHAND));
			packetWriter.writeD(charInfoPackage.getPaperdollVariation2Id(Inventory.PAPERDOLL_RHAND));
			packetWriter.writeD(0x00); // Transform ID

			//TODO: Pet info?
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
			packetWriter.writeF(0x00);
			packetWriter.writeF(0x00);

			packetWriter.writeD(0x00); // Vitality Points
			packetWriter.writeD(0x00); // Vitality Rate
			packetWriter.writeD(0x00); // Use Vitality Potions Left

			packetWriter.writeD(charInfoPackage.isAvailable());
			packetWriter.writeC(0x00); // Chaos Festival Winner
			packetWriter.writeC(charInfoPackage.isHero()); // hero glow
			packetWriter.writeC(charInfoPackage.isHairAccessoryEnabled() ? 0x01 : 0x00); // show hair accessory if enabled
		}
		return true;
	}

	public static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
	{
		CharSelectInfoPackage charInfopackage;
		List<CharSelectInfoPackage> characterList = new ArrayList<CharSelectInfoPackage>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? ORDER BY createtime LIMIT " + Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
			statement.setString(1, loginName);
			rset = statement.executeQuery();
			while(rset.next()) // fills the package
			{
				charInfopackage = restoreChar(rset);
				if(charInfopackage != null)
					characterList.add(charInfopackage);
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "could not restore charinfo:" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
	}

	private static int restoreBaseClassId(int objId)
	{
		int classId = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND type=?");
			statement.setInt(1, objId);
			statement.setInt(2, SubClassType.BASE_CLASS.ordinal());
			rset = statement.executeQuery();
			while(rset.next())
			{
				classId = rset.getInt("class_id");
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "could not restore base class id:" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return classId;
	}

	private static String restoreChangedOldName(int objId)
	{
		String suffix = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT value FROM character_variables WHERE obj_id=? AND name=? AND (expire_time=-1 OR expire_time>=?)");
			statement.setInt(1, objId);
			statement.setString(2, Player.CHANGED_OLD_NAME);
			statement.setLong(3, System.currentTimeMillis());
			rset = statement.executeQuery();
			if(rset.next())
			{
				suffix = rset.getString("value");
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "could not restore changed old name:" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return suffix;
	}

	private static CharSelectInfoPackage restoreChar(ResultSet chardata)
	{
		CharSelectInfoPackage charInfopackage = null;
		try
		{
			int objectId = chardata.getInt("obj_Id");
			int classid = chardata.getInt("class_id");
			int baseClassId = classid;
			boolean useBaseClass = chardata.getInt("type") == SubClassType.BASE_CLASS.ordinal();
			if(!useBaseClass)
				baseClassId = restoreBaseClassId(objectId);

			Race race = ClassId.valueOf(baseClassId).getRace();
			if(race == null)
			{
				_log.atWarning().log( "%s: Race was not found for the class id: %s", CharacterSelectionInfoPacket.class.getSimpleName(), baseClassId );
				return null;
			}

			String name = chardata.getString("char_name");
			charInfopackage = new CharSelectInfoPackage(objectId, name);
			charInfopackage.setMaxHp(chardata.getInt("maxHp"));
			charInfopackage.setCurrentHp(chardata.getDouble("curHp"));
			charInfopackage.setMaxMp(chardata.getInt("maxMp"));
			charInfopackage.setCurrentMp(chardata.getDouble("curMp"));

			charInfopackage.setX(chardata.getInt("x"));
			charInfopackage.setY(chardata.getInt("y"));
			charInfopackage.setZ(chardata.getInt("z"));
			charInfopackage.setPk(chardata.getInt("pkkills"));
			charInfopackage.setPvP(chardata.getInt("pvpkills"));

			int face = chardata.getInt("beautyFace");
			charInfopackage.setFace(face > 0 ? face : chardata.getInt("face"));

			int hairstyle = chardata.getInt("beautyHairstyle");
			charInfopackage.setHairStyle(hairstyle > 0 ? hairstyle : chardata.getInt("hairstyle"));

			int haircolor = chardata.getInt("beautyHaircolor");
			charInfopackage.setHairColor(haircolor > 0 ? haircolor : chardata.getInt("haircolor"));

			charInfopackage.setSex(chardata.getInt("sex"));

			charInfopackage.setExp(chardata.getLong("exp"));
			charInfopackage.setSp(chardata.getLong("sp"));
			charInfopackage.setClanId(chardata.getInt("clanid"));

			charInfopackage.setKarma(chardata.getInt("karma"));
			charInfopackage.setRace(race.ordinal());
			charInfopackage.setClassId(classid);
			charInfopackage.setBaseClassId(baseClassId);
			long deletetime = chardata.getLong("deletetime");
			int deletehours = 0;
			if(Config.CHARACTER_DELETE_AFTER_HOURS > 0)
				if(deletetime > 0)
				{
					deletetime = (int) (System.currentTimeMillis() / 1000 - deletetime);
					deletehours = (int) (deletetime / 3600);
					if(deletehours >= Config.CHARACTER_DELETE_AFTER_HOURS)
					{
						CharacterDAO.getInstance().deleteCharByObjId(objectId);
						return null;
					}
					deletetime = Config.CHARACTER_DELETE_AFTER_HOURS * 3600 - deletetime;
				}
				else
					deletetime = 0;
			charInfopackage.setDeleteTimer((int) deletetime);
			charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
			charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));

			charInfopackage.setHairAccessoryEnabled(chardata.getInt("hide_head_accessories") == 0);

			if(charInfopackage.getAccessLevel() < 0 && !AutoBan.isBanned(objectId))
				charInfopackage.setAccessLevel(0);

			charInfopackage.setChangedOldName(restoreChangedOldName(objectId));
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}

		return charInfopackage;
	}
}