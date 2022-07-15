package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingPackets;

public class GMViewCharacterInfoPacket implements IClientOutgoingPacket
{
	private Location _loc;
	private int[][] _inv;
	private int obj_id, _race, _sex, class_id, pvp_flag, karma, level, mount_type;
	private int _str, _con, _dex, _int, _wit, _men;
	private int curHp, maxHp, curMp, maxMp, curCp, maxCp, curLoad, maxLoad, rec_left, rec_have;
	private int _patk, _patkspd, _pdef, evasion, accuracy, crit, _matk, _matkspd;
	private int _mdef, _mEvasion, _mAccuracy, _mCrit, hair_style, hair_color, face, gm_commands;
	private int clan_id, clan_crest_id, ally_id, title_color;
	private int hero, private_store, name_color, pk_kills, pvp_kills;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, DwarvenCraftLevel, running, pledge_class;
	private String _name, title;
	private long _exp, _sp;
	private double move_speed, attack_speed, col_radius, col_height;
	private AttributeType attackAttributeType;
	private int attackElementValue;
	private int defenceFire, defenceWater, defenceWind, defenceEarth, defenceHoly, defenceUnholy;
	private int fame;
	private int talismans;
	private int _jewelsLimit;
	private double _expPercent;

	public GMViewCharacterInfoPacket(final Player cha)
	{
		_loc = cha.getLoc();
		obj_id = cha.getObjectId();
		_name = cha.getName();
		_race = cha.getRace().ordinal();
		_sex = cha.getSex().ordinal();
		class_id = cha.getClassId().getId();
		level = cha.getLevel();
		_exp = cha.getExp();
		_str = cha.getSTR();
		_dex = cha.getDEX();
		_con = cha.getCON();
		_int = cha.getINT();
		_wit = cha.getWIT();
		_men = cha.getMEN();
		curHp = (int) cha.getCurrentHp();
		maxHp = cha.getMaxHp();
		curMp = (int) cha.getCurrentMp();
		maxMp = cha.getMaxMp();
		_sp = cha.getSp();
		curLoad = cha.getCurrentLoad();
		maxLoad = cha.getMaxLoad();
		_patk = cha.getPAtk(null);
		_patkspd = cha.getPAtkSpd();
		_pdef = cha.getPDef(null);
		evasion = cha.getPEvasionRate(null);
		accuracy = cha.getPAccuracy();
		crit = cha.getStat().getCriticalHit();
		_matk = cha.getMAtk(null, null);
		_matkspd = cha.getMAtkSpd();
		_mdef = cha.getMDef(null, null);
		_mEvasion = cha.getMEvasionRate(null);
		_mAccuracy = cha.getMAccuracy();
		_mCrit = cha.getStat().getMCriticalHit();
		pvp_flag = cha.getPvpFlag();
		karma = cha.getKarma();
		_runSpd = (int) cha.getRunSpeed();
		_walkSpd = (int) cha.getWalkSpeed();
		_swimRunSpd = (int) cha.getSwimRunSpeed();
		_swimWalkSpd = (int) cha.getSwimWalkSpeed();
		move_speed = cha.getMovementSpeedMultiplier();
		attack_speed = cha.getAttackSpeedMultiplier();
		mount_type = cha.getMountType().ordinal();
		col_radius = cha.getCurrentCollisionRadius();
		col_height = cha.getCurrentCollisionHeight();
		hair_style = cha.getBeautyHairStyle() > 0 ? cha.getBeautyHairStyle() : cha.getHairStyle();
		hair_color = cha.getBeautyHairColor() > 0 ? cha.getBeautyHairColor() : cha.getHairColor();
		face = cha.getBeautyFace() > 0 ? cha.getBeautyFace() : cha.getFace();
		gm_commands = cha.isGM() ? 1 : 0;
		title = cha.getTitle();
		_expPercent = Experience.getExpPercent(cha.getLevel(), cha.getExp());
		//
		Clan clan = cha.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		//
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		//ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

		private_store = cha.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : cha.getPrivateStoreType();
		DwarvenCraftLevel = Math.max(cha.getSkillLevel(1320), 0);
		pk_kills = cha.getPkKills();
		pvp_kills = cha.getPvpKills();
		rec_left = cha.getRecomLeft(); //c2 recommendations remaining
		rec_have = cha.getRecomHave(); //c2 recommendations received
		curCp = (int) cha.getCurrentCp();
		maxCp = cha.getMaxCp();
		running = cha.isRunning() ? 0x01 : 0x00;
		pledge_class = cha.getPledgeRank().ordinal();
		hero = cha.isHero() ? 1 : 0; //0x01: Hero Aura and symbol
		name_color = cha.getNameColor();
		title_color = cha.getTitleColor();
		attackAttributeType = cha.getStat().getAttackElement();
		attackElementValue = cha.getStat().getAttackElementValue(attackAttributeType);
		defenceFire = cha.getStat().getDefenseElementValue(AttributeType.FIRE);
		defenceWater = cha.getStat().getDefenseElementValue(AttributeType.WATER);
		defenceWind = cha.getStat().getDefenseElementValue(AttributeType.WIND);
		defenceEarth = cha.getStat().getDefenseElementValue(AttributeType.EARTH);
		defenceHoly = cha.getStat().getDefenseElementValue(AttributeType.HOLY);
		defenceUnholy = cha.getStat().getDefenseElementValue(AttributeType.UNHOLY);
		fame = cha.getFame();
		talismans = cha.getTalismanCount();
		_jewelsLimit = cha.getBroochJewelSlots();
		_inv = new int[Inventory.PAPERDOLL_MAX][4];
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = cha.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = cha.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = cha.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = cha.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GM_VIEW_CHARACTER_INFO.writeId(packetWriter);
		//dddddSddddQfddddddddddddQddd
		//dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
		//ccdhdddddddddddddddddddddddffffddddSdddcccddhhddddccdccddhhhhhhhhdd
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeD(_loc.h);
		packetWriter.writeD(obj_id);
		packetWriter.writeS(_name);
		packetWriter.writeD(_race);
		packetWriter.writeD(_sex);
		packetWriter.writeD(class_id);
		packetWriter.writeD(level);
		packetWriter.writeQ(_exp);
		packetWriter.writeF(_expPercent);
		packetWriter.writeD(_str);
		packetWriter.writeD(_dex);
		packetWriter.writeD(_con);
		packetWriter.writeD(_int);
		packetWriter.writeD(_wit);
		packetWriter.writeD(_men);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(maxHp);
		packetWriter.writeD(curHp);
		packetWriter.writeD(maxMp);
		packetWriter.writeD(curMp);
		packetWriter.writeQ(_sp);
		packetWriter.writeD(curLoad);
		packetWriter.writeD(maxLoad);
		packetWriter.writeD(pk_kills);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			if(PAPERDOLL_ID == Inventory.PAPERDOLL_ARTIFACT_BOOK)
				break;

			packetWriter.writeD(_inv[PAPERDOLL_ID][0]);
		}

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			if(PAPERDOLL_ID == Inventory.PAPERDOLL_ARTIFACT_BOOK)
				break;

			packetWriter.writeD(_inv[PAPERDOLL_ID][1]);
		}

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			if(PAPERDOLL_ID == Inventory.PAPERDOLL_AGATHION_MAIN)
				break;

			packetWriter.writeD(_inv[PAPERDOLL_ID][2]);
			packetWriter.writeD(_inv[PAPERDOLL_ID][3]);
		}

		packetWriter.writeC(talismans);
		packetWriter.writeC(_jewelsLimit);
		packetWriter.writeD(0x00);
		packetWriter.writeH(0x00);
		packetWriter.writeD(_patk);
		packetWriter.writeD(_patkspd);
		packetWriter.writeD(_pdef);
		packetWriter.writeD(evasion);
		packetWriter.writeD(accuracy);
		packetWriter.writeD(crit);
		packetWriter.writeD(_matk);

		packetWriter.writeD(_matkspd);
		packetWriter.writeD(_patkspd);

		packetWriter.writeD(_mdef);
		packetWriter.writeD(_mEvasion);
		packetWriter.writeD(_mAccuracy);
		packetWriter.writeD(_mCrit);

		packetWriter.writeD(pvp_flag);
		packetWriter.writeD(karma);

		packetWriter.writeD(_runSpd);
		packetWriter.writeD(_walkSpd);
		packetWriter.writeD(_swimRunSpd); // swimspeed
		packetWriter.writeD(_swimWalkSpd); // swimspeed
		packetWriter.writeD(_runSpd);
		packetWriter.writeD(_walkSpd);
		packetWriter.writeD(_runSpd);
		packetWriter.writeD(_walkSpd);
		packetWriter.writeF(move_speed);
		packetWriter.writeF(attack_speed);
		packetWriter.writeF(col_radius);
		packetWriter.writeF(col_height);
		packetWriter.writeD(hair_style);
		packetWriter.writeD(hair_color);
		packetWriter.writeD(face);
		packetWriter.writeD(gm_commands);

		packetWriter.writeS(title);
		packetWriter.writeD(clan_id);
		packetWriter.writeD(clan_crest_id);
		packetWriter.writeD(ally_id);
		packetWriter.writeC(mount_type);
		packetWriter.writeC(private_store);
		packetWriter.writeC(DwarvenCraftLevel); //_cha.getDwarvenCraftLevel() > 0 ? 1 : 0
		packetWriter.writeD(pk_kills);
		packetWriter.writeD(pvp_kills);

		packetWriter.writeH(rec_left);
		packetWriter.writeH(rec_have); //Blue value for name (0 = white, 255 = pure blue)
		packetWriter.writeD(class_id);
		packetWriter.writeD(0x00); // special effects? circles around player...
		packetWriter.writeD(maxCp);
		packetWriter.writeD(curCp);

		packetWriter.writeC(running); //changes the Speed display on Status Window

		packetWriter.writeC(321);

		packetWriter.writeD(pledge_class); //changes the text above CP on Status Window

		packetWriter.writeC(0x00); // noble
		packetWriter.writeC(hero);

		packetWriter.writeD(name_color);
		packetWriter.writeD(title_color);

		packetWriter.writeH(attackAttributeType.getId());
		packetWriter.writeH(attackElementValue);
		packetWriter.writeH(defenceFire);
		packetWriter.writeH(defenceWater);
		packetWriter.writeH(defenceWind);
		packetWriter.writeH(defenceEarth);
		packetWriter.writeH(defenceHoly);
		packetWriter.writeH(defenceUnholy);

		packetWriter.writeD(fame);
		packetWriter.writeD(0x00);

		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);

		return true;
	}
}