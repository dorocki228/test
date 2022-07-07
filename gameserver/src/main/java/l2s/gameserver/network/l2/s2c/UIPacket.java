package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.PrivateBuffer;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.updatetype.UserInfoType;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class UIPacket extends AbstractMaskPacket<UserInfoType>
{
	private boolean can_writeImpl;
	private final boolean partyRoom;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd;
	private final int _flWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private int _relation;
	private final double move_speed;
	private final double attack_speed;
	private final double col_radius;
	private final double col_height;
	private final Location _loc;
	private final int obj_id;
	private final int vehicle_obj_id;
	private final int _race;
	private final int sex;
	private final int base_class;
	private final int level;
	private final int curCp;
	private final int maxCp;
	private final int _weaponEnchant;
	private final int _armorSetEnchant;
	private final int _weaponFlag;
	private final long _exp;
	private final long _sp;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private final int curLoad;
	private final int maxLoad;
	private final int rec_left;
	private final int rec_have;
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _wit;
	private final int _men;
	private final int ClanPrivs;
	private final int InventoryLimit;
	private final int _patk;
	private final int _patkspd;
	private final int _pdef;
	private final int _matk;
	private final int _matkspd;
	private final int _pEvasion;
	private final int _pAccuracy;
	private final int _pCrit;
	private final int _mEvasion;
	private final int _mAccuracy;
	private final int _mCrit;
	private final int _mdef;
	private final int pvp_flag;
	private final int karma;
	private final int hair_style;
	private final int hair_color;
	private final int face;
	private final int gm_commands;
	private final int fame;
	private int clan_id;
	private int _isClanLeader;
	private int clan_crest_id;
	private int ally_id;
	private int ally_crest_id;
	private int large_clan_crest_id;
	private final int private_store;
	private final int can_crystalize;
	private final int pk_kills;
	private final int pvp_kills;
	private final int class_id;
	private final int agathion;
	private final int _partySubstitute;
	private final int hero;
	private final int mount_id;
	private final int name_color;
	private final int running;
	private final int pledge_class;
	private final int pledge_type;
	private final int title_color;
	private final int transformation;
	private final int defenceFire;
	private final int defenceWater;
	private final int defenceWind;
	private final int defenceEarth;
	private final int defenceHoly;
	private final int defenceUnholy;
	private final int mount_type;
	private final String _name;
	private String _title;
	private final EffectCubic[] cubics;
	private final Element attackElement;
	private final int attackElementValue;
	private final int _moveType;
	private final double _expPercent;
	private final TeamType _team;
	private final boolean _hideHeadAccessories;
	private final byte[] _masks;
	private int _initSize;

	public UIPacket(Player player)
	{
		this(player, true);
	}

	public UIPacket(Player player, boolean addAll)
	{
		can_writeImpl = false;
		_masks = new byte[] { 0, 0, 0 };
		_initSize = 5;
		_name = player.getVisibleName(player);
		name_color = player.getVisibleNameColor(player);
		_title = player.getVisibleTitle(player);
		title_color = player.isPrivateBuffer() ? PrivateBuffer.TITLE_COLOR : player.getVisibleTitleColor(player);
		if(player.isPledgeVisible(player))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			_isClanLeader = player.isClanLeader() ? 1 : 0;
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}
		if(player.isGMInvisible())
			_title += "[I]";
		if(player.isPolymorphed())
			if(NpcHolder.getInstance().getTemplate(player.getPolyId()) != null)
				_title = _title + " - " + NpcHolder.getInstance().getTemplate(player.getPolyId()).name;
			else
				_title += " - Polymorphed";

		_title += "[" + player.getReward(player) + "]";
		_title += player.isMercenary() ? " [Merc]" : "";


		if(_title.length() > 24)
			_title = _title.substring(_title.length() - 24);

		if (player.isMounted()) {
			_weaponEnchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType().ordinal();
		} else {
			_weaponEnchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}
		_weaponFlag = player.getActiveWeaponInstance() == null ? 20 : 40;
		double multiplier = player.getMovementSpeedMultiplier();
		move_speed = Util.scaleValue(multiplier);
		_runSpd = (int) (player.getRunSpeed() / multiplier);
		_walkSpd = (int) (player.getWalkSpeed() / multiplier);
		_flRunSpd = 0;
		_flWalkSpd = 0;
		if (player.isFlying()) {
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		} else {
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}
		_swimRunSpd = (int) (player.getSwimRunSpeed() / multiplier);
		_swimWalkSpd = (int) (player.getSwimWalkSpeed() / multiplier);
		if (player.getClan() != null) {
			_relation |= 0x20;
			if (player.isClanLeader())
				_relation |= 0x40;
		}
		for (Event e : player.getEvents())
			_relation = e.getUserRelation(player, _relation);
		_loc = player.getLoc();
		obj_id = player.getObjectId();
		vehicle_obj_id = player.isInBoat() ? player.getBoat().getBoatId() : 0;
		_race = player.getRace().ordinal();
		sex = player.getSex().ordinal();
		base_class = ClassId.VALUES[player.getBaseClassId()].getId();
		level = player.getLevel();
		_exp = player.getExp();
		_expPercent = Experience.getExpPercent(player.getLevel(), player.getExp());
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curMp = (int) player.getCurrentMp();
		maxMp = player.getMaxMp();
		curLoad = player.getCurrentLoad();
		maxLoad = player.getMaxLoad();
		_sp = player.getSp();
		_patk = player.getPAtk(null);
		_patkspd = player.getPAtkSpd();
		_pdef = player.getPDef(null);
		_pEvasion = player.getPEvasionRate(null);
		_pAccuracy = player.getPAccuracy();
		_pCrit = player.getPCriticalHit(null);
		_mEvasion = player.getMEvasionRate(null);
		_mAccuracy = player.getMAccuracy();
		_mCrit = player.getMCriticalHit(null, null);
		_matk = player.getMAtk(null, null);
		_matkspd = player.getMAtkSpd();
		_mdef = player.getMDef(null, null);
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();
		attack_speed = player.getAttackSpeedMultiplier();
		col_radius = player.getColRadius();
		col_height = player.getColHeight();
		hair_style = player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle();
		hair_color = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		gm_commands = player.getPlayerAccess().CanUseAltG ? 1 : 0;
		clan_id = player.getClanId();
		ally_id = player.getAllyId();
		private_store = player.isPrivateBuffer() ? 2 : player.getPrivateStoreType();
		can_crystalize = player.getSkillLevel(248) > 0 ? 1 : 0;
		pk_kills = player.getPkKills();
		pvp_kills = player.getPvpKills();
		cubics = player.getCubics().toArray(new EffectCubic[0]);
		ClanPrivs = player.getClanPrivileges();
		rec_left = player.getRecomLeft();
		rec_have = player.getRecomHave();
		InventoryLimit = player.getInventoryLimit();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCp = (int) player.getCurrentCp();
		_team = player.getTeam();
		hero = player.isHero() || player.isCustomHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0;
		running = player.isRunning() ? 1 : 0;
		pledge_class = player.getPledgeRank().ordinal();
		pledge_type = player.getPledgeType();
		transformation = player.getVisualTransformId();
		attackElement = player.getAttackElement();
		attackElementValue = player.getAttack(attackElement);
		defenceFire = player.getDefence(Element.FIRE);
		defenceWater = player.getDefence(Element.WATER);
		defenceWind = player.getDefence(Element.WIND);
		defenceEarth = player.getDefence(Element.EARTH);
		defenceHoly = player.getDefence(Element.HOLY);
		defenceUnholy = player.getDefence(Element.UNHOLY);
		agathion = player.getAgathionId();
		fame = player.getFame();
		partyRoom = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_moveType = player.isInFlyingTransform() ? 2 : player.isInWater() ? 1 : 0;
		_partySubstitute = player.isPartySubstituteStarted() ? 1 : 0;
		_hideHeadAccessories = player.hideHeadAccessories();
		_armorSetEnchant = player.getArmorSetEnchant();
		can_writeImpl = true;
		if(addAll)
			addComponentType(UserInfoType.values());
	}

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		calcBlockSize(component);
	}

	private void calcBlockSize(UserInfoType type)
	{
		switch(type)
		{
			case BASIC_INFO:
			{
				_initSize += type.getBlockLength() + _name.length() * 2;
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + _title.length() * 2;
				break;
			}
			default:
			{
				_initSize += type.getBlockLength();
				break;
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
			return;
        writeD(obj_id);
        writeD(_initSize);
        writeH(23);
		writeB(_masks);
		if(containsMask(UserInfoType.RELATION))
            writeD(_relation);
		if(containsMask(UserInfoType.BASIC_INFO))
		{
            writeH(UserInfoType.BASIC_INFO.getBlockLength() + _name.length() * 2);
			writeString(_name);
            writeC(gm_commands);
            writeC(_race);
            writeC(sex);
            writeD(base_class);
            writeD(class_id);
            writeC(level);
		}
		if(containsMask(UserInfoType.BASE_STATS))
		{
            writeH(UserInfoType.BASE_STATS.getBlockLength());
            writeH(_str);
            writeH(_dex);
            writeH(_con);
            writeH(_int);
            writeH(_wit);
            writeH(_men);
            writeH(0);
            writeH(0);
		}
		if(containsMask(UserInfoType.MAX_HPCPMP))
		{
            writeH(UserInfoType.MAX_HPCPMP.getBlockLength());
            writeD(maxHp);
            writeD(maxMp);
            writeD(maxCp);
		}
		if(containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
		{
            writeH(UserInfoType.CURRENT_HPMPCP_EXP_SP.getBlockLength());
            writeD(curHp);
            writeD(curMp);
            writeD(curCp);
			writeQ(_sp);
			writeQ(_exp);
			writeF(_expPercent);
		}
		if(containsMask(UserInfoType.ENCHANTLEVEL))
		{
            writeH(UserInfoType.ENCHANTLEVEL.getBlockLength());
            writeC(_weaponEnchant);
            writeC(_armorSetEnchant);
		}
		if(containsMask(UserInfoType.APPAREANCE))
		{
            writeH(UserInfoType.APPAREANCE.getBlockLength());
            writeD(hair_style);
            writeD(hair_color);
            writeD(face);
            writeC(!_hideHeadAccessories);
		}
		if(containsMask(UserInfoType.STATUS))
		{
            writeH(UserInfoType.STATUS.getBlockLength());
            writeC(mount_type);
            writeC(private_store);
            writeC(can_crystalize);
            writeC(0);
		}
		if(containsMask(UserInfoType.STATS))
		{
            writeH(UserInfoType.STATS.getBlockLength());
            writeH(_weaponFlag);
            writeD(_patk);
            writeD(_patkspd);
            writeD(_pdef);
            writeD(_pEvasion);
            writeD(_pAccuracy);
            writeD(_pCrit);
            writeD(_matk);
            writeD(_matkspd);
            writeD(_patkspd);
            writeD(_mEvasion);
            writeD(_mdef);
            writeD(_mAccuracy);
            writeD(_mCrit);
		}
		if(containsMask(UserInfoType.ELEMENTALS))
		{
            writeH(UserInfoType.ELEMENTALS.getBlockLength());
            writeH(defenceFire);
            writeH(defenceWater);
            writeH(defenceWind);
            writeH(defenceEarth);
            writeH(defenceHoly);
            writeH(defenceUnholy);
		}
		if(containsMask(UserInfoType.POSITION))
		{
            writeH(UserInfoType.POSITION.getBlockLength());
            writeD(_loc.x);
            writeD(_loc.y);
            writeD(_loc.z + Config.CLIENT_Z_SHIFT);
            writeD(vehicle_obj_id);
		}
		if(containsMask(UserInfoType.SPEED))
		{
            writeH(UserInfoType.SPEED.getBlockLength());
            writeH(_runSpd);
            writeH(_walkSpd);
            writeH(_swimRunSpd);
            writeH(_swimWalkSpd);
            writeH(_flRunSpd);
            writeH(_flWalkSpd);
            writeH(_flyRunSpd);
            writeH(_flyWalkSpd);
		}
		if(containsMask(UserInfoType.MULTIPLIER))
		{
            writeH(UserInfoType.MULTIPLIER.getBlockLength());
			writeF(move_speed);
			writeF(attack_speed);
		}
		if(containsMask(UserInfoType.COL_RADIUS_HEIGHT))
		{
            writeH(UserInfoType.COL_RADIUS_HEIGHT.getBlockLength());
			writeF(col_radius);
			writeF(col_height);
		}
		if(containsMask(UserInfoType.ATK_ELEMENTAL))
		{
            writeH(UserInfoType.ATK_ELEMENTAL.getBlockLength());
            writeC(attackElement.getId());
            writeH(attackElementValue);
		}
		if(containsMask(UserInfoType.CLAN))
		{
            writeH(UserInfoType.CLAN.getBlockLength() + _title.length() * 2);
			writeString(_title);
            writeH(pledge_type);
            writeD(clan_id);
            writeD(large_clan_crest_id);
            writeD(clan_crest_id);
            writeD(ClanPrivs);
            writeC(_isClanLeader);
            writeD(ally_id);
            writeD(ally_crest_id);
            writeC(partyRoom ? 1 : 0);
		}
		if(containsMask(UserInfoType.SOCIAL))
		{
            writeH(UserInfoType.SOCIAL.getBlockLength());
            writeC(pvp_flag);
            writeD(karma);
            writeC(0);
            writeC(hero);
            writeC(pledge_class);
            writeD(pk_kills);
            writeD(pvp_kills);
            writeH(rec_left);
            writeH(rec_have);
		}
		if(containsMask(UserInfoType.VITA_FAME))
		{
            writeH(UserInfoType.VITA_FAME.getBlockLength());
            writeD(0);
            writeC(0);
            writeD(fame);
            writeD(0);
		}
		if(containsMask(UserInfoType.SLOTS))
		{
            writeH(UserInfoType.SLOTS.getBlockLength());
            writeC(0);
            writeC(0);
            writeC(_team.ordinal());
            writeC(0);
            writeC(0);
            writeC(0);
            writeC(0);
		}
		if(containsMask(UserInfoType.MOVEMENTS))
		{
            writeH(UserInfoType.MOVEMENTS.getBlockLength());
            writeC(_moveType);
            writeC(running);
		}
		if(containsMask(UserInfoType.COLOR))
		{
            writeH(UserInfoType.COLOR.getBlockLength());
            writeD(name_color);
            writeD(title_color);
		}
		if(containsMask(UserInfoType.INVENTORY_LIMIT))
		{
            writeH(UserInfoType.INVENTORY_LIMIT.getBlockLength());
            writeH(0);
            writeH(0);
            writeH(InventoryLimit);
            writeC(0);
		}
		if(containsMask(UserInfoType.UNK_3))
		{
            writeH(UserInfoType.UNK_3.getBlockLength());
            writeC(1);
            writeH(0);
            writeD(0);
		}
	}
}
