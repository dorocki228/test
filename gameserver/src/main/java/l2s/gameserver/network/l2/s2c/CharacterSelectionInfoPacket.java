package l2s.gameserver.network.l2.s2c;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.CharSelectInfoPackage;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectionInfoPacket extends L2GameServerPacket
{
	private static final int[] PAPERDOLL_ORDER_VISUAL_ID = {
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR,
	};

	private static final Logger _log;
	private final String _loginName;
	private final int _sessionId;
	private final CharSelectInfoPackage[] _characterPackages;
	private final boolean _hasPremiumAccount;

	public CharacterSelectionInfoPacket(GameClient client)
	{
		_loginName = client.getLogin();
		_sessionId = client.getSessionKey().playOkID1;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_hasPremiumAccount = client.getPremiumAccountType() > 0 && client.getPremiumAccountExpire() > System.currentTimeMillis() / 1000L;
	}

	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}

	@Override
	protected final void writeImpl()
	{
		int size = _characterPackages != null ? _characterPackages.length : 0;
		writeD(size);
		writeD(7);
		writeC(0);
		writeC(0);
		writeD(2);
		writeC(0);
		long lastAccess = -1L;
		int lastUsed = -1;
		for(int i = 0; i < size; ++i)
			if(lastAccess < _characterPackages[i].getLastAccess())
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}
		for(int i = 0; i < size; ++i)
		{
			CharSelectInfoPackage csip = _characterPackages[i];
			writeS(csip.getName());
			writeD(csip.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(csip.getClanId());
			writeD(0);
			writeD(csip.getSex());
			writeD(csip.getRace());
			writeD(csip.getBaseClassId());
			writeD(Config.REQUEST_ID);
			writeD(csip.getX());
			writeD(csip.getY());
			writeD(csip.getZ());
			writeF(csip.getCurrentHp());
			writeF(csip.getCurrentMp());
			writeQ(csip.getSp());
			writeQ(csip.getExp());
			int lvl = csip.getLevel();
			writeF(Experience.getExpPercent(lvl, csip.getExp()));
			writeD(lvl);
			writeD(csip.getKarma());
			writeD(csip.getPk());
			writeD(csip.getPvP());
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);

			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_PENDANT));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));

			int hair = Math.max(csip.getPaperdollItemId(Inventory.PAPERDOLL_HAIR), csip.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
			writeD(hair);
			writeD(hair);

			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
            writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_BELT));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_BROOCH));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_JEWEL1));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_JEWEL2));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_JEWEL3));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_JEWEL4));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_JEWEL5));
			writeD(csip.getPaperdollItemId(Inventory.PAPERDOLL_JEWEL6));

			for (int slot : PAPERDOLL_ORDER_VISUAL_ID)
			{
				writeD(csip.getPaperdollVisualId(slot));
			}

			writeH(csip.getPaperdollEnchantEffect(10));
			writeH(csip.getPaperdollEnchantEffect(11));
			writeH(csip.getPaperdollEnchantEffect(6));
			writeH(csip.getPaperdollEnchantEffect(9));
			writeH(csip.getPaperdollEnchantEffect(12));

			writeD(csip.getHairStyle());
			writeD(csip.getHairColor());
			writeD(csip.getFace());
			writeF(csip.getMaxHp());
			writeF(csip.getMaxMp());
			writeD(csip.isAvailable() ? csip.getDeleteTimer() : -1);
			writeD(csip.getClassId());
			writeD(i == lastUsed ? 1 : 0);

			writeC(Math.min(csip.getPaperdollEnchantEffect(Inventory.PAPERDOLL_RHAND), 127));
			writeD(csip.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND)[0]);
			writeD(csip.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND)[1]);

			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeF(0.0);
			writeF(0.0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(csip.isAvailable());
			writeC(0);
			writeC(0);
			writeC(csip.isHairAccessoryEnabled() ? 1 : 0);
		}
	}

	public static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
	{
		List<CharSelectInfoPackage> characterList = new ArrayList<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? LIMIT 7");
			statement.setString(1, loginName);
			rset = statement.executeQuery();
			while(rset.next())
			{
				CharSelectInfoPackage charInfopackage = restoreChar(rset);
				if(charInfopackage != null)
					characterList.add(charInfopackage);
			}
		}
		catch(Exception e)
		{
			_log.error("could not restore charinfo:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return characterList.toArray(new CharSelectInfoPackage[0]);
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
			statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND type=" + SubClassType.BASE_CLASS.ordinal());
			statement.setInt(1, objId);
			rset = statement.executeQuery();
			while(rset.next())
				classId = rset.getInt("class_id");
		}
		catch(Exception e)
		{
			_log.error("could not restore base class id:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return classId;
	}

	private static CharSelectInfoPackage restoreChar(ResultSet chardata)
	{
		CharSelectInfoPackage charInfopackage = null;
		try
		{
			int objectId = chardata.getInt("obj_Id");
			int baseClassId;
			int classid = baseClassId = chardata.getInt("class_id");
			boolean useBaseClass = chardata.getInt("type") == SubClassType.BASE_CLASS.ordinal();
			if(!useBaseClass)
				baseClassId = restoreBaseClassId(objectId);
			Race race = ClassId.VALUES[baseClassId].getRace();
			if(race == null)
			{
				_log.warn(CharacterSelectionInfoPacket.class.getSimpleName() + ": Race was not found for the class id: " + baseClassId);
				return null;
			}
			String name = chardata.getString("char_name");
			charInfopackage = new CharSelectInfoPackage(objectId, name);
			charInfopackage.setLevel(chardata.getInt("level"));
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
            if(Config.CHARACTER_DELETE_AFTER_HOURS > 0)
				if(deletetime > 0L)
				{
					deletetime = (int) (System.currentTimeMillis() / 1000L - deletetime);
                    int deletehours = (int) (deletetime / 3600L);
                    if(deletehours >= Config.CHARACTER_DELETE_AFTER_HOURS)
					{
						CharacterDAO.getInstance().deleteCharByObjId(objectId);
						return null;
					}
					deletetime = Config.CHARACTER_DELETE_AFTER_HOURS * 3600 - deletetime;
				}
				else
					deletetime = 0L;
			charInfopackage.setDeleteTimer((int) deletetime);
			charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
			boolean banned = PunishmentService.INSTANCE.isPunished(PunishmentType.CHARACTER, String.valueOf(objectId));
			charInfopackage.setAvailable(!banned);
			charInfopackage.setHairAccessoryEnabled(chardata.getInt("hide_head_accessories") == 0);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		return charInfopackage;
	}

	static
	{
		_log = LoggerFactory.getLogger(CharacterSelectionInfoPacket.class);
	}
}
