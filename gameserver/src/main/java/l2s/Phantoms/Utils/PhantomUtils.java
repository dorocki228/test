package  l2s.Phantoms.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gve.zones.model.GveZone;
import  l2s.Phantoms.PhantomVariables;
import  l2s.Phantoms.enums.PhantomType;
import  l2s.Phantoms.objects.ConfigLevelGroup;
import l2s.Phantoms.objects.GveZoneParam;
import  l2s.Phantoms.objects.PhantomClassAI;
import  l2s.Phantoms.objects.Clan.PhantomClan;
import  l2s.Phantoms.objects.sets.AccessorySet;
import  l2s.Phantoms.objects.sets.ArmorSet;
import  l2s.Phantoms.objects.sets.JewelSet;
import  l2s.Phantoms.objects.sets.Weapons;
import  l2s.Phantoms.parsers.Clan.PhantomClanHolder;
import  l2s.Phantoms.parsers.Items.holder.PhantomAccessoryHolder;
import  l2s.Phantoms.parsers.Items.holder.PhantomArmorHolder;
import  l2s.Phantoms.parsers.Items.holder.PhantomJewelHolder;
import  l2s.Phantoms.parsers.Items.holder.PhantomWeaponHolder;
import  l2s.Phantoms.parsers.ai.ClassAIParser;
import  l2s.Phantoms.templates.PhantomSkill;
import  l2s.Phantoms.templates.SkillsGroup;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.commons.dbutils.DbUtils;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.Config;
import  l2s.gameserver.database.DatabaseFactory;
import  l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.handler.items.IItemHandler;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Territory;
import  l2s.gameserver.model.World;
import  l2s.gameserver.model.base.ClassId;
import  l2s.gameserver.model.base.ClassType2;
import l2s.gameserver.model.base.SoulShotType;
import  l2s.gameserver.model.items.ItemInstance;
import  l2s.gameserver.model.pledge.Alliance;
import  l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.SkillEntry;
import  l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.item.ItemGrade;
import  l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import  l2s.gameserver.utils.ItemFunctions;
import  l2s.gameserver.utils.Location;
import  l2s.gameserver.utils.SiegeUtils;

/**
 * @author 4ipolino
 */
public class PhantomUtils
{
	private static final Logger _log = LoggerFactory.getLogger(PhantomUtils.class);
	
	
	public static void delete4(int account)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, account);
			statement.execute();
		}catch(Exception e)
		{
			_log.info("AccountBonusDAO.delete(String): "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void delete3(int account)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, account);
			statement.execute();
		}catch(Exception e)
		{
			_log.info("AccountBonusDAO.delete(String): "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void delete2(int account)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, account);
			statement.execute();
		}catch(Exception e)
		{
			_log.info("AccountBonusDAO.delete(String): "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void delete(String account)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE name=?");
			statement.setString(1, account);
			statement.execute();
		}catch(Exception e)
		{
			_log.info("AccountBonusDAO.delete(String): "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	
	public static void delete6(String account)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_name=?");
			statement.setString(1, account);
			statement.execute();
		}catch(Exception e)
		{
			_log.info("AccountBonusDAO.delete(String): "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	
	private static Map<String, Integer> getSubPledges()
	{
		Map<String,Integer> tmp = new HashMap<>();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_subpledges");
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				int clan_id = rset.getInt("clan_id");
				String name = rset.getString("name");
				tmp.put(name,clan_id);
			}
		}catch(Exception e)
		{
			_log.warn("Could not restore clan SubPledges: "+e, e);
		}finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		 return tmp; 
	}
	
	public static void deleteClanP()
	{
		Map<String, Integer> tmp = getSubPledges();
		//удалим кланы 
		for(PhantomClan clan : PhantomClanHolder.getInstance().getAllClans().values())
		{
			if (!clan.getAllyName().isEmpty())
			{
				delete6(clan.getAllyName());
			}
			if (tmp.containsKey(clan.getClanName()))
			{
				delete(clan.getClanName());
				delete2(tmp.get(clan.getClanName()));
				delete3(tmp.get(clan.getClanName()));
				delete4(tmp.get(clan.getClanName()));
			}
			}

	}
	public static void deleteClanP2()
	{
		for(PhantomClan clan : PhantomClanHolder.getInstance().getAllClans().values())
		{
			for (Clan clan1:ClanTable.getInstance().getClans())
			{
				if (equals(clan.getClanName(), clan1.getName()))
					ClanTable.getInstance().dissolveClan(clan1);
			}
		}
		for(PhantomClan clan : PhantomClanHolder.getInstance().getAllClans().values())
		{
			for (Alliance ally : ClanTable.getInstance().getAlliances())
			{
			if (equals(ally.getAllyName(), clan.getAllyName()))
				ClanTable.getInstance().deleteAllyFromDb(ally.getAllyId());
			}

		}
		
	}

	//TODO
	public static boolean availabilityCheck(Player phantom, Location target_loc)
	{
		List<List<Location>> moveList = GeoMove.findMovePath(phantom.getX(), phantom.getY(), phantom.getZ(), target_loc.getX(), target_loc.getY(), target_loc.getZ(), true, phantom.getGeoIndex());
		if (moveList != null && moveList.size() > 0)
			return true;
		return false;
	}
	
	
	public static void checkLevelAndSetFarmLoc(Player phantom, Creature target, boolean forcibly)
	{

	}
	
	public static boolean initializePhantom(Player phantom, int phantomClass, GveZone zone)
	{
		PhantomClassAI class_ai = ClassAIParser.getInstance().getClassAI(phantomClass);
		if (class_ai == null)
		{
			_log.error("Error in class: id_" + phantom.getClassId().getId() + " name_" + ClassesDictionary.getNameById(phantom.getClassId().getId()) + " . Null AI.");
			return false;
		}
		phantom.phantom_params.setPhantomClassAI(class_ai);
		ResetSkills(phantom);
		if (!giveClothes(phantom,zone)) // выдача шмоток
			return false;
		phantom.rewardSkills(false);

		// Restore clan skills
		if (phantom.getClan() != null)
		{
			phantom.getClan().addSkillsQuietly(phantom);
			// Restore clan leader siege skills
			if (phantom.getClan().getLeaderId() == phantom.getObjectId() && phantom.getClan().getLevel() >= 5)
				SiegeUtils.addSiegeSkills(phantom);
		}
		// Give dwarven craft skill
		if (phantom.getActiveClassId() >= 53 && phantom.getActiveClassId() <= 57 || phantom.getActiveClassId() == 117 || phantom.getActiveClassId() == 118)
			phantom.addSkill(SkillHolder.getInstance().getSkillEntry(1321, 1));
		phantom.addSkill(SkillHolder.getInstance().getSkillEntry(1322, 1));

		if (phantom.getPhantomType() == PhantomType.PHANTOM_HARD || phantom.getPhantomType() == PhantomType.PHANTOM_BOT_HUNTER || (Config.ADD_SUB_SKILL_PHANTOM_LEVEL <= phantom.getLevel() && Rnd.get(100) < Config.CHANCE_ADD_SUB_SKILL_PHANTOM))
			PhantomUtils.addSubSkill(phantom);
		
		return true;
	}
	
	private static void ResetSkills(Player player)
	{
		Collection<SkillEntry> skills = player.getAllSkills();
		for(SkillEntry skillEntry : skills)
			if(skillEntry != null)
				player.removeSkill(skillEntry, true);
		
		player.checkSkills();
	}
	
	/**
	 * проверка, выдача и автоиспользование сосок
	 * 
	 * @param fantom
	 */
	//TODO проверить ид шотов
	public static void phantomShotActivate(Player fantom, boolean summon)
	{
		ItemInstance weapon = fantom.getPhantomWeapon();
		
		int shot_id = getSoulShotIdByWeaponGrade(weapon);
		fantom.getInventory().addItem(shot_id, 5);
		shotActivate(fantom, shot_id, SoulShotType.SOULSHOT);

		int shot_id_Mage = getBlessedSpiritShotIdByWeaponGrade(weapon);
		fantom.getInventory().addItem(shot_id_Mage, 5);
		shotActivate(fantom, shot_id_Mage, SoulShotType.SPIRITSHOT);
		
		if (summon)
		{
			// Beast_Soulshot
			fantom.getInventory().addItem(6645, 5);
			shotActivate(fantom, 6645, SoulShotType.BEAST_SOULSHOT);
			// Beast_Soulshot
			fantom.getInventory().addItem(6647, 5);
			shotActivate(fantom, 6647, SoulShotType.BEAST_SPIRITSHOT);
		}
	}
	
	private static void shotActivate(Player fantom, int shot_id, SoulShotType shot_type)
	{
		IItemHandler handler;
		ItemInstance item = fantom.getInventory().getItemByItemId(shot_id);
		if(item != null && (handler = item.getTemplate().getHandler()) != null && handler.isAutoUse() && fantom.addAutoShot(shot_id, true, shot_type))
			ItemFunctions.useItem(fantom, item, false, false);
	}
	/**
	 * проверка и выдача стрел
	 * 
	 * @param weaponPhantom
	 * @param fantom
	 */
	public static void phantomArrowsAdd(Player phantom, ItemInstance weaponPhantom)
	{
		if (weaponPhantom.getItemType() == WeaponType.BOW && weaponPhantom != null)
		{
			int Arrows_id = getArrowsIdByWeaponGrade(weaponPhantom);
			if (ItemFunctions.getItemCount(phantom, Arrows_id) == 0)
			{
				phantom.getInventory().addItem(Arrows_id, 1);
			}
		}
	}
	
	/**
	 * определяем грейд физ сосок возвращает ид соски по грейду оружия
	 */
	public static int getSoulShotIdByWeaponGrade(ItemInstance item)
	{
		if (item==null)
			return 0;
		
		int grade = item.getGrade().extOrdinal();
		if (grade == 0)
			return 1835; // NG
		if (grade == 1)
			return 1463; // D
		if (grade == 2)
			return 1464; // C
		if (grade == 3)
			return 1465; // B
		if (grade == 4)
			return 1466; // A
		if (grade == 5)
			return 1467; // S
		return 0;
	}
	
	/**
	 * определяем грейд маг сосок возвращает ид соски по грейду оружия
	 */
	public static int getBlessedSpiritShotIdByWeaponGrade(ItemInstance item)
	{
		if (item==null)
			return 0;
		int grade = item.getGrade().extOrdinal();
		if (grade == 0)
			return 3947; // NG
		if (grade == 1)
			return 3948; // D
		if (grade == 2)
			return 3949; // C
		if (grade == 3)
			return 3950; // B
		if (grade == 4)
			return 3951; // A
		if (grade == 5)
			return 3952; // S
		return 0;
	}
	
	/**
	 * определяем грейд стрел возвращает ид стрел по грейду оружия
	 */
	public static int getArrowsIdByWeaponGrade(ItemInstance item)
	{
		if (item==null)
			return 0;
		int grade = item.getGrade().extOrdinal();
		if (grade == 0)
			return 32249; // NG
		if (grade == 1)
			return 32250; // D
		if (grade == 2)
			return 32251; // C
		if (grade == 3)
			return 32252; // B
		if (grade == 4)
			return 32253; // A
		if (grade == 5)
			return 1345; // S TODO Aniver добавить в сервер
		return 0;
	}
	
	public static void phantomOlyBuffs(Player _phantom)
	{
		// баф воинов
	/*	if (!_phantom.getClassId().isMage() || _phantom.getClassId().getRace() == PlayerRace.orc)
		{
			for (int[] buff : Config.PHANTOM_OLY_BUFF)
			{
				Skill skill = SkillHolder.getInstance().getSkillEntry(buff[0], buff[1]);
				for (EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(_phantom, _phantom, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(et.getPeriod());
					_phantom.getAbnormalList().addEffect(effect);
				}
			}
		}
		// баф магов
		if (_phantom.getClassId().isMage() && _phantom.getClassId().getRace() != Race.ORC)
		{
			for (int[] buff : Config.PHANTOM_OLY_BUFF_MAGE)
			{
				Skill skill = SkillHolder.getInstance().getSkillEntry(buff[0], buff[1]);
				for (final EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(_phantom, _phantom, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(et.getPeriod());
					_phantom.getAbnormalList().addEffect(effect);
				}
			}
		}*/
	}

	
	public static void equipArmorItem(int item, Player phantom)
	{
		try
		{
			if (item != 0)
			{
				ItemInstance armor = activateArmor(phantom, item);
				phantom.getInventory().addItem(armor);
				phantom.getInventory().equipItem(armor);
			}
		}
		catch (Exception e)
		{
			_log.error("Error armor id: " + item + " for class id: " + phantom.getClassId().getId());
			e.printStackTrace();
		}
	}
	
	/*
	 * создание и заточка шмотки
	 */
	public static ItemInstance activateArmor(Player phantom, int armor)
	{
		ItemInstance item = ItemFunctions.createItem(armor);
		int grade = item.getGrade().extOrdinal();
		if (grade != 0) // нг вещи нельзя точить
		{
			item.setEnchantLevel(getConfigLevelGroup(phantom.getLevel(), Config.PHANTOM_PLAYERS_ENCHANT_ARMOR));
		}
		return item;
	}
	
	/*
	 * прочие итемы (с заточкой) (щит сигиль и тд)
	 */
	public static void equipItemEnchant(int item, Player phantom)
	{
		try
		{
			if (item != 0)
			{
				ItemInstance Shield = ItemFunctions.createItem(item);
				Shield.setEnchantLevel(getConfigLevelGroup(phantom.getLevel(), Config.PHANTOM_PLAYERS_ENCHANT_UNDERWEAR));
				if (Shield.getItemId() == 21580)
					Shield.setEnchantLevel(9);

				phantom.getInventory().addItem(Shield);
				phantom.getInventory().equipItem(Shield);
			}
		}
		catch (Exception e)
		{
			_log.error("Error Shield id: " + item + " for class id: " + phantom.getClassId().getId());
			e.printStackTrace();
		}
	}
	
	/*
	 * прочие итемы без заточки
	 */
	public static void equipItemEct(int item, Player phantom)
	{
		try
		{
			if (item != 0)
			{
				ItemInstance Shield = ItemFunctions.createItem(item);
				phantom.getInventory().addItem(Shield);
				phantom.getInventory().equipItem(Shield);
			}
		}
		catch (Exception e)
		{
			_log.error("Error Ect item id: " + item + " for class id: " + phantom.getClassId().getId());
			e.printStackTrace();
		}
	}
	
	/*
	 * Создание, заточка, атрибут, аугментация и выдача оружия фантому
	 */
	public static boolean equipWeaponItem(int weapon, Player phantom)
	{
		try
		{
			if (weapon != 0)
			{
				ItemInstance weaponPhantom = ItemFunctions.createItem(weapon);
				if (weaponPhantom == null)
					return false;
				// узнаем грейд оружия
				int grade = weaponPhantom.getGrade().extOrdinal();
				if (grade != 0) // нг вещи нельзя точить
				{
				 weaponPhantom.setEnchantLevel(getConfigLevelGroup(phantom.getLevel(), Config.PHANTOM_PLAYERS_ENCHANT_WEAPON));
				}
				phantom.getInventory().addItem(weaponPhantom);
				phantom.setPhantomWeapon(weaponPhantom);
				phantomArrowsAdd(phantom, weaponPhantom); // проверка и выдача стрел при использовании лука
				phantom.getInventory().equipItem(weaponPhantom);
				ItemInstance ActiveWeaponPhantom = phantom.getActiveWeaponInstance();
				if (ActiveWeaponPhantom == null)
					return false;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			_log.error("Error weapon id: " + weapon + " for class id: " + phantom.getClassId().getId());
			e.printStackTrace();
		}
		return true;
	}
	
	/*
	 * Создание, заточка и выдача бижи фантому
	 */
	public static void equipAccessory(int item, Player phantom)
	{
		try
		{
			if (item != 0)
			{
				ItemInstance accessory = ItemFunctions.createItem(item);
				// узнаем грейд оружия
				int grade = accessory.getGrade().extOrdinal();
				if (grade != 0) // нг вещи нельзя точить
				{
				 accessory.setEnchantLevel(getConfigLevelGroup(phantom.getLevel(), Config.PHANTOM_PLAYERS_ENCHANT_accessory));
				}
				phantom.getInventory().addItem(accessory);
				phantom.getInventory().equipItem(accessory);
			}
		}
		catch (Exception e)
		{
			_log.error("Error accessory id: " + item + " for class id: " + phantom.getClassId().getId());
			e.printStackTrace();
		}
	}
	
	/*
	 * Выдача клана фантомам
	 */
	/*public static void setClan(Player phantom)
	{
		if (phantom.getClan() != null) // удалим старый
		{
			Clan clanold = phantom.getClan();
			int subUnitType = phantom.getPledgeType();
			clanold.removeClanMember(subUnitType, phantom.getObjectId());
			phantom.setClan(null);
		}
		Clan clan = Rnd.get(ClanManager.getInstance().getAllClans());
		if (clan==null)
			return;
		// даем клан
		SubUnit subUnit = clan.getSubUnit(0);
		if (subUnit == null)
			return;
		UnitMember u_member = new UnitMember(clan, phantom.getName(), phantom.getTitle(), phantom.getLevel(), phantom.getClassId().getId(), phantom.getObjectId(), 0, phantom.getPowerGrade(), phantom.getApprentice(), phantom.getSex(), Clan.SUBUNIT_NONE);
		subUnit.addUnitMember(u_member);
		phantom.setPledgeType(0);
		phantom.setClan(clan);
		phantom.setPowerGrade(6);
		phantom.setLvlJoinedAcademy(0);
		phantom.setApprentice(0);
	}*/
	
	public static void addSubSkill(Player phantom)
	{
		SkillsGroup castom_skill = phantom.phantom_params.getClassAI().getCastomSkills();	
		for (PhantomSkill skill : castom_skill.getAllSkills())
			phantom.addSkill(SkillHolder.getInstance().getSkillEntry(skill.getId(), skill.getLvl()), false);
		
		for (List<PhantomSkill> skill : castom_skill.getRndSkill().values())
		{
			if (!skill.isEmpty())
			{
				PhantomSkill rnd_skill = Rnd.get(skill);
				phantom.addSkill(SkillHolder.getInstance().getSkillEntry(rnd_skill.getId(), rnd_skill.getLvl()), false);
			}
		}
		
		}
	

	
	/*
	 * рекурсивный поиск свободной точки для респа торговца
	 */
	public static Location getFreePoint(Territory territory)
	{
		Location _loc = Territory.getRandomLoc(territory);
		if (CheckFreePoint(_loc))
			return _loc;
		return getFreePoint(territory);
	}
	
	/*
	 * Проверка точки для торговой лавки
	 */
	public static boolean CheckFreePoint(Location object)
	{
		boolean tradenear = true;
		for (Player p : World.getAroundPlayers(object, Config.SERVICES_TRADE_RADIUS + Rnd.get(10), 200))
		{
			if (p.isInStoreMode() || p.getPhantomType() == PhantomType.PHANTOM_CRAFTER || p.getPhantomType() == PhantomType.PHANTOM_TRADER)
			{
				tradenear = false;
				break;
			}
		}
		if (World.getAroundNpc(object, Config.SERVICES_TRADE_RADIUS, 200).size() > 0)
		{
			tradenear = false;
		}
		return tradenear;
	}
	
	public static GveZoneParam getLocType(GveZone zone)
	{
		GveZoneParam z = new GveZoneParam();
		if(zone.getMaxGrade()!=ItemGrade.R99)
			z.setMaxGrade(zone.getMaxGrade());
		switch(zone.getZone().getType())
		{
		case gve_static_high:
		case gve_high:
			z.setMinLvl(76);
			z.setMaxLvl(80);
			break;
		case gve_low:
			z.setMinLvl(60);
			z.setMaxLvl(70);
			break;
		case gve_static_mid:
		case gve_mid:
			z.setMinLvl(70);
			z.setMaxLvl(80);
			break;
		default:
			break;
			
		}

		return z;
	}
	
	public static ItemGrade getGrade(int level)
	{
		if (level >= 1 && level <= 51)
		{
			return ItemGrade.C;
		}
		else if (level >= 52 && level <= 60)
		{
			return ItemGrade.B;
		}
		else if (level >= 61 && level <= 75)
		{
			return ItemGrade.A;
		}
		else if (level >= 76)
		{
			return ItemGrade.S;
		}
		return null;
	}
	
	public static boolean giveClothes(Player fantom, GveZone zone)
	{
		// чистим старые итемы
		for (ItemInstance item : fantom.getInventory().getItems())
			ItemFunctions.deleteItem(fantom, item.getItemId(), item.getCount(), true);
		ItemGrade _grade = PhantomUtils.getGrade(fantom.getLevel());
		if(zone!=null)
		{
			GveZoneParam param = getLocType(zone);
			if (param.getMaxGrade()!=null)
				_grade=param.getMaxGrade();
		}
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		if (db_grade.ordinal() < _grade.ordinal())
			_grade = db_grade;
		
		ArmorSet armor_set = PhantomArmorHolder.getInstance().getArmorRndSet(fantom, _grade); // рандомный сет
		JewelSet Jewel_set = PhantomJewelHolder.getInstance().getJewelRndSet(fantom, _grade); // рандомный сет бижи
		Weapons _weapons = PhantomWeaponHolder.getInstance().getRndWeapon(fantom, _grade); // рандомная пушка
		//UnderwearSet Underwear_set = PhantomUnderwearHolder.getInstance().getUnderwearSet(_grade); // рандомный сет
		AccessorySet Accessory_set = PhantomAccessoryHolder.getInstance().getAccessorySet(fantom.getClassId().getId()); // рандомный набор аксесуаров
		if (armor_set == null || Jewel_set == null || _weapons == null) // основной набор
			return false;

		// Надеваем сет
		PhantomUtils.equipArmorItem(armor_set.getHelm(), fantom);
		PhantomUtils.equipArmorItem(armor_set.getChest(), fantom);
		PhantomUtils.equipArmorItem(armor_set.getGaiter(), fantom);
		PhantomUtils.equipArmorItem(armor_set.getGloves(), fantom);
		PhantomUtils.equipArmorItem(armor_set.getBoots(), fantom);

		PhantomUtils.equipItemEnchant(armor_set.getShield(), fantom);// Надеваем щит (если есть)
		// оружие
		if (!PhantomUtils.equipWeaponItem(_weapons.getWeapon(), fantom))
			return false;
		// бижа
		PhantomUtils.equipAccessory(Jewel_set.getEarringL(), fantom);
		PhantomUtils.equipAccessory(Jewel_set.getEarringR(), fantom);
		PhantomUtils.equipAccessory(Jewel_set.getNecklace(), fantom);
		PhantomUtils.equipAccessory(Jewel_set.getRingL(), fantom);
		PhantomUtils.equipAccessory(Jewel_set.getRingR(), fantom);
		// украшения
		if (Accessory_set != null)
		{
			if (fantom.getSex().ordinal() == 1)
			{
				PhantomUtils.equipItemEct(Accessory_set.getAccessory(true), fantom);
			}
			else
				PhantomUtils.equipItemEct(Accessory_set.getAccessory(false), fantom);
			// Надеваем плащ (если есть)
			PhantomUtils.equipItemEct(Accessory_set.getCloak(fantom.getFraction()), fantom);
		}
		//if (Underwear_set != null)
	//	{
		//	PhantomUtils.equipItemEct(Underwear_set.getBelt(), fantom);
		//	PhantomUtils.equipItemEnchant(Underwear_set.getShirt(), fantom, isHard);
	//	}
		for (int[] reward : Config.PHANTOMS_ITEMS_ID)
		{
			ItemInstance Item = ItemFunctions.createItem(reward[0]);
			if (Item.isStackable())
			{
				Item.setCount(reward[1]);
				fantom.getInventory().addItem(Item);
			}
			else
			{
				for (int i = 0; i < reward[1]; ++i)
				{
					Item = ItemFunctions.createItem(reward[0]);
					fantom.getInventory().addItem(Item);
				}
			}
		}
		if ((fantom.getClassId().getType2() == ClassType2.SUMMONER) // выдать соски самонам
																																|| (fantom.getClassId().getId() == 91) // мститель
																																|| (fantom.getClassId().getId() == 6) // Рыцарь ада
																																|| (fantom.getClassId().getId() == 90) // Рыцарь феникса
																																) 
		{
			fantom.getInventory().addItem(1459, 200); // выдать кристаллы для вызова самонов
			fantom.getInventory().addItem(1461, 200);
			PhantomUtils.phantomShotActivate(fantom, true);
		}
		else
			PhantomUtils.phantomShotActivate(fantom, false);
		return true;
	}
	
	
	public static int getConfigLevelGroup(int level, List<ConfigLevelGroup> _config)
	{
		for (ConfigLevelGroup el : _config)
		{
			if (level >= el._level_min && level <= el._level_max)
			{
				if (el._max==0)
					return 0;
				return Rnd.get(el._min, el._max);
			}
		}
		return 0;
	}
	
	public static int[] getPhantomClass(GveZone zone)
	{
		if(zone!=null)
		{
		GveZoneParam param = getLocType(zone);
		
		if(param.getMinLvl()>=76)
			return Config.Profa3;

		if(param.getMaxLvl()<=76)
			return Config.Profa2;
		}
		return  combineInt(Config.Profa2,Config.Profa3);
	}
	
	public static int[] combineInt(int[] a, int[] b)
	{
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
	
	private static long PhantomLimit_refresh = 0;
	private static int PhantomLimit_count = 0;
	
	public static int getPhantomLimit()
	{
		int max_limit = PhantomVariables.getInt("CfgPhantomLimit", 0);
		if (max_limit == 0)
			return 0;
		long now = System.currentTimeMillis();
		if (now > PhantomLimit_refresh)
		{
			PhantomLimit_refresh = now + 600000;
			LocalDateTime time = LocalDateTime.now();
			int hour = time.getHour();
			switch (hour)
			{
				case 0:
					PhantomLimit_count = (max_limit * Rnd.get(70, 80)) / 100;
				break;
				case 1:
				case 2:
					PhantomLimit_count = (max_limit * Rnd.get(60, 70)) / 100;
				break;
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
					PhantomLimit_count = (max_limit * Rnd.get(40, 50)) / 100;
				break;
				case 9:
				case 10:
				case 11:
				case 12:
					PhantomLimit_count = (max_limit * Rnd.get(60, 70)) / 100;
				break;
				case 13:
				case 14:
				case 15:
				case 16:
					PhantomLimit_count = (max_limit * Rnd.get(70, 80)) / 100;
				break;
				case 17:
					PhantomLimit_count = (max_limit * Rnd.get(90, 95)) / 100;
				break;
				case 18:
				case 19:
				case 20:
					PhantomLimit_count = (max_limit * Rnd.get(95, 105)) / 100;
				break;
				case 21:
				case 22:
				case 23:
					PhantomLimit_count = (max_limit * Rnd.get(80, 90)) / 100;
				break;
				case 24:
					PhantomLimit_count = (max_limit * Rnd.get(70, 80)) / 100;
				break;
			}
		}
		return PhantomLimit_count;
	}
	
	public static boolean IsActive(String name)
	{
		return PhantomVariables.getString(name, "off").equalsIgnoreCase("on");
	}
	
	public static boolean SetActive(String name, boolean active)
	{
		if (active == IsActive(name))
			return false;
		if (active)
			PhantomVariables.set(name, "on");
		else
			PhantomVariables.unset(name);
		return true;
	}
	
	public static int nearest(int of, List<Integer> in)
	{
	int min = Integer.MAX_VALUE;
	int closest = of;

	for (int v : in) 
	{
	    final int diff = Math.abs(v - of);

	    if (diff < min) 
	    {
	        min = diff;
	        closest = v;
	    }
	}
	return closest;
	}
	
	public static int FindNum(int[] array, int x)
	{
		int result = array[0];
		int min = Math.abs(x-array[0]);
		for(int i = 0; i < array.length; i++)
		{
			if (Math.abs(x-array[i]) < min)
			{
				min = Math.abs(x-array[i]);
				result = array[i];
			}
		}
		return result;
	}
	
	public static boolean equals(String str1, String str2)
	{
		return str1 == null ? str2 == null : str1.equals(str2);
	}
	public static boolean equalsIgnoreCase(String str1, String str2)
	{
		return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
	}
	
	public static String getFullClassName(int classId)
	{
		switch (classId)
		{
			case 0:
				return "Human Fighter";
			case 1:
				return "Warrior";
			case 2:
				return "Gladiator";
			case 3:
				return "Warlord";
			case 4:
				return "Human Knight";
			case 5:
				return "Paladin";
			case 6:
				return "Dark Avenger";
			case 7:
				return "Rogue";
			case 8:
				return "Treasure Hunter";
			case 9:
				return "Hawkeye";
			case 10:
				return "Human Mystic";
			case 11:
				return "Human Wizard";
			case 12:
				return "Sorcerer";
			case 13:
				return "Necromancer";
			case 14:
				return "Warlock";
			case 15:
				return "Cleric";
			case 16:
				return "Bishop";
			case 17:
				return "Prophet";
			case 18:
				return "Elven Fighter";
			case 19:
				return "Elven Knight";
			case 20:
				return "Temple Knight";
			case 21:
				return "Sword Singer";
			case 22:
				return "Elven Scout";
			case 23:
				return "Plains Walker";
			case 24:
				return "Silver Ranger";
			case 25:
				return "Elven Mystic";
			case 26:
				return "Elven Wizard";
			case 27:
				return "Spellsinger";
			case 28:
				return "Elemental Summoner";
			case 29:
				return "Elven Oracle";
			case 30:
				return "Elven Elder";
			case 31:
				return "Dark Fighter";
			case 32:
				return "Palus Knight";
			case 33:
				return "Shillien Knight";
			case 34:
				return "Bladedancer";
			case 35:
				return "Assassin";
			case 36:
				return "Abyss Walker";
			case 37:
				return "Phantom Ranger";
			case 38:
				return "Dark Mystic";
			case 39:
				return "Dark Wizard";
			case 40:
				return "Spellhowler";
			case 41:
				return "Phantom Summoner";
			case 42:
				return "Shillien Oracle";
			case 43:
				return "Shillien Elder";
			case 44:
				return "Orc Fighter";
			case 45:
				return "Orc Raider";
			case 46:
				return "Destroyer";
			case 47:
				return "Monk";
			case 48:
				return "Tyrant";
			case 49:
				return "Orc Mystic";
			case 50:
				return "Orc Shaman";
			case 51:
				return "Overlord";
			case 52:
				return "Warcryer";
			case 53:
				return "Dwarven Fighter";
			case 54:
				return "Scavenger";
			case 55:
				return "Bounty Hunter";
			case 56:
				return "Artisan";
			case 57:
				return "Warsmith";
			case 88:
				return "Duelist";
			case 89:
				return "Dreadnought";
			case 90:
				return "Phoenix Knight";
			case 91:
				return "Hell Knight";
			case 92:
				return "Sagittarius";
			case 93:
				return "Adventurer";
			case 94:
				return "Archmage";
			case 95:
				return "Soultaker";
			case 96:
				return "Arcana Lord";
			case 97:
				return "Cardinal";
			case 98:
				return "Hierophant";
			case 99:
				return "Eva's Templar";
			case 100:
				return "Sword Muse";
			case 101:
				return "Wind Rider";
			case 102:
				return "Moonlight Sentinel";
			case 103:
				return "Mystic Muse";
			case 104:
				return "Elemental Master";
			case 105:
				return "Eva's Saint";
			case 106:
				return "Shillien Templar";
			case 107:
				return "Spectral Dancer";
			case 108:
				return "Ghost Hunter";
			case 109:
				return "Ghost Sentinel";
			case 110:
				return "Storm Screamer";
			case 111:
				return "Spectral Master";
			case 112:
				return "Shillien Saint";
			case 113:
				return "Titan";
			case 114:
				return "Grand Khavatari";
			case 115:
				return "Dominator";
			case 116:
				return "Doom Cryer";
			case 117:
				return "Fortune Seeker";
			case 118:
				return "Maestro";
			case 123:
				return "Kamael Soldier";
			case 124:
				return "Kamael Soldier";
			case 125:
				return "Trooper";
			case 126:
				return "Warder";
			case 127:
				return "Berserker";
			case 128:
				return "Soul Breaker";
			case 129:
				return "Soul Breaker";
			case 130:
				return "Arbalester";
			case 131:
				return "Doombringer";
			case 132:
				return "Soul Hound";
			case 133:
				return "Soul Hound";
			case 134:
				return "Trickster";
			case 135:
				return "Inspector";
			case 136:
				return "Judicator";
			default:
				return "None";
		}
	}
	
}
