package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.s2c.updatetype.UserInfoType;
import l2s.gameserver.templates.item.ItemGrade;

/**
 * @reworked by Bonux
 */
public class UIPacket extends AbstractMaskPacket<UserInfoType>
{
	public static final int USER_RELATION_PARTY_MEMBER = 0x08;
	public static final int USER_RELATION_PARTY_LEADER = 0x10;
	public static final int USER_RELATION_CLAN_MEMBER = 0x20;
	public static final int USER_RELATION_CLAN_LEADER = 0x40;
	public static final int USER_RELATION_IN_SIEGE = 0x80;
	public static final int USER_RELATION_ATTACKER = 0x100;
	public static final int USER_RELATION_IN_DOMINION_WAR = 0x1000;

	// Params
	private boolean _canWrite = false, partyRoom;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, mountRunSpd, mountWalkSpd, _flyRunSpd, _flyWalkSpd, _relation;
	private double move_speed, attack_speed, col_radius, col_height;
	private Location _loc;
	private int obj_id, vehicle_obj_id, _race, sex, base_class, level, curCp, maxCp, _weaponEnchant, _armorSetEnchant, _weaponFlag;
	private long _exp, _sp;
	private int curHp, maxHp, curMp, maxMp, curLoad, maxLoad, rec_left, rec_have;
	private int _str, _con, _dex, _int, _wit, _men, ClanPrivs, InventoryLimit;
	private int _patk, _patkspd, _pdef, _matk, _matkspd;
	private int _pEvasion, _pAccuracy, _pCrit, _mEvasion, _mAccuracy, _mCrit;
	private int _mdef, pvp_flag, karma, hair_style, hair_color, face, gm_commands, fame;
	private int clan_id, _isClanLeader, clan_crest_id, ally_id, ally_crest_id, large_clan_crest_id;
	private int private_store, can_crystalize, pk_kills, pvp_kills, class_id, agathion, _partySubstitute;
	private int hero, mount_id;
	private int name_color, running, pledge_class, pledge_type, title_color;
	private int defenceFire, defenceWater, defenceWind, defenceEarth, defenceHoly, defenceUnholy;
	private int mount_type;
	private String _name, _title;
	private Cubic[] cubics;
	private AttributeType attackAttributeType;
	private int attackElementValue;
	private int _moveType;
	//private boolean _allowMap;
	private int talismans;
	private int _jewelsLimit;
	private boolean _activeMainAgathionSlot;
	private int _subAgathionsLimit;
	private double _expPercent;
	private TeamType _team;
	private final boolean _hideHeadAccessories;
	private final int _elementalAttackPower, _elementalFireDefence, _elementalWaterDefence, _elementalWindDefence,_elementalEarthDefence, _activeElementId;
	private final boolean _noble;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	private int _initSize = 5;

	public UIPacket(Player player)
	{
		this(player, true);
	}

	public UIPacket(Player player, boolean addAll)
	{
		_name = player.getVisibleName(player);
		name_color = player.getVisibleNameColor(player);
		_title = player.getVisibleTitle(player);
		title_color = player.getVisibleTitleColor(player);

		if(player.isPledgeVisible(player))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			_isClanLeader = player.isClanLeader() ? 1 : 0;
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		if(player.isGMInvisible())
			_title += "[I]";
		if(player.isPolymorphed())
		{
			if(NpcHolder.getInstance().getTemplate(player.getPolyId()) != null)
				_title += "[" + NpcHolder.getInstance().getTemplate(player.getPolyId()).name + "]";
			else
				_title += "[Polymorphed]";
		}

		if(player.isMounted())
		{
			_weaponEnchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType().ordinal();
		}
		else
		{
			_weaponEnchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		_weaponFlag = player.getActiveWeaponInstance() == null ? 0x14 : 0x28;

		move_speed = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / move_speed);
		_walkSpd = (int) (player.getWalkSpeed() / move_speed);

		mountRunSpd = 0; // TODO
		mountWalkSpd = 0; // TODO

		if(player.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}

		_swimRunSpd = (int) (player.getSwimRunSpeed() / move_speed);
		_swimWalkSpd = (int) (player.getSwimWalkSpeed() / move_speed);

		Party party = player.getParty();
		if(party != null)
		{
			_relation |= USER_RELATION_PARTY_MEMBER;

			if(party.isLeader(player))
				_relation |= USER_RELATION_PARTY_LEADER;
		}

		if(player.getClan() != null)
		{
			_relation |= USER_RELATION_CLAN_MEMBER;
			if(player.isClanLeader())
				_relation |= USER_RELATION_CLAN_LEADER;
		}

		for(Event e : player.getEvents())
			_relation = e.getUserRelation(player, _relation);

		_loc = player.getLoc();
		obj_id = player.getObjectId();
		vehicle_obj_id = player.isInBoat() ? player.getBoat().getBoatId() : 0x00;
		_race = player.getRace().ordinal();
		sex = player.getSex().ordinal();
		base_class = ClassId.valueOf(player.getBaseClassId()).getFirstParent(sex).getId();
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
		_pCrit = player.getStat().getCriticalHit();
		_mEvasion = player.getMEvasionRate(null);
		_mAccuracy = player.getMAccuracy();
		_mCrit = player.getStat().getMCriticalHit();
		_matk = player.getMAtk(null, null);
		_matkspd = player.getMAtkSpd();
		_mdef = player.getMDef(null, null);
		pvp_flag = player.getPvpFlag(); // 0=white, 1=purple, 2=purpleblink
		karma = player.getKarma();
		attack_speed = player.getAttackSpeedMultiplier();
		col_radius = player.getCurrentCollisionRadius();
		col_height = player.getCurrentCollisionHeight();
		hair_style = player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? sex : (player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle());
		hair_color = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		gm_commands = player.isGM() || player.getPlayerAccess().CanUseAltG ? 1 : 0;
		// builder level активирует в клиенте админские команды
		clan_id = player.getClanId();
		ally_id = player.getAllyId();
		private_store = (player.isInBuffStore() ? 0 : player.getPrivateStoreType());
		can_crystalize = player.getStat().getCrystallizeGrade() != ItemGrade.NONE ? 1 : 0;
		pk_kills = player.getPkKills();
		pvp_kills = player.getPvpKills();
		cubics = player.getCubics().toArray(new Cubic[player.getCubics().size()]);
		ClanPrivs = player.getClanPrivileges();
		rec_left = player.getRecomLeft(); //c2 recommendations remaining
		rec_have = player.getRecomHave(); //c2 recommendations received
		InventoryLimit = player.getInventoryLimit();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCp = (int) player.getCurrentCp();
		_team = player.getTeam();
		if (_team == TeamType.NONE) {
			if (player.isInParty()) {
				_team = TeamType.BLUE;
			}
		}
		hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 2 : 0; //0x01: Hero Aura and symbol
		if(hero == 0)
			hero = Hero.getInstance().isInactiveHero(obj_id) ? 1 : 0;
		_noble = hero > 0;
		running = player.isRunning() ? 0x01 : 0x00; //changes the Speed display on Status Window
		pledge_class = player.getPledgeRank().ordinal();
		pledge_type = player.getPledgeType();
		attackAttributeType = player.getStat().getAttackElement();
		attackElementValue = player.getStat().getAttackElementValue(attackAttributeType);
		defenceFire = player.getStat().getDefenseElementValue(AttributeType.FIRE);
		defenceWater = player.getStat().getDefenseElementValue(AttributeType.WATER);
		defenceWind = player.getStat().getDefenseElementValue(AttributeType.WIND);
		defenceEarth = player.getStat().getDefenseElementValue(AttributeType.EARTH);
		defenceHoly = player.getStat().getDefenseElementValue(AttributeType.HOLY);
		defenceUnholy = player.getStat().getDefenseElementValue(AttributeType.UNHOLY);
		agathion = player.getAgathionNpcId();
		fame = player.getFame();
		partyRoom = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_moveType = player.isInFlyingTransform() ? 0x02 : (player.isInWater() ? 0x01 : 0x00);
		talismans = player.getTalismanCount();
		_jewelsLimit = player.getBroochJewelSlots();
		_activeMainAgathionSlot = player.isActiveMainAgathionSlot();
		_subAgathionsLimit = player.getSubAgathionsLimit();
		//_allowMap = player.isActionBlocked(Zone.BLOCKED_ACTION_MINIMAP);
		_partySubstitute = player.isPartySubstituteStarted()  ? 1 : 0;
		_hideHeadAccessories = player.hideHeadAccessories();
		_armorSetEnchant = player.getArmorSetEnchant();

		_elementalAttackPower = (int) Math.round(player.getStat().getElementalAttackPower(player.getActiveElement()));
		_elementalFireDefence = (int) Math.round(player.getStat().getElementalDefence(ElementalElement.FIRE));
		_elementalWaterDefence = (int) Math.round(player.getStat().getElementalDefence(ElementalElement.WATER));
		_elementalWindDefence = (int) Math.round(player.getStat().getElementalDefence(ElementalElement.WIND));
		_elementalEarthDefence = (int) Math.round(player.getStat().getElementalDefence(ElementalElement.EARTH));
		_activeElementId = player.getActiveElement().getId();

		_canWrite = true;

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
				_initSize += type.getBlockLength() + (_name.length() * 2);
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + (_title.length() * 2);
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
	public boolean canBeWritten()
	{
		return _canWrite;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.USER_INFO.writeId(packetWriter);
		packetWriter.writeD(obj_id);

		packetWriter.writeD(_initSize);
		packetWriter.writeH(24);
		packetWriter.writeB(_masks);

		if(containsMask(UserInfoType.RELATION))
			packetWriter.writeD(_relation);

		if(containsMask(UserInfoType.BASIC_INFO))
		{
			packetWriter.writeH(UserInfoType.BASIC_INFO.getBlockLength() + (_name.length() * 2));
			packetWriter.writeSizedString(_name);
			packetWriter.writeC(gm_commands);
			packetWriter.writeC(_race);
			packetWriter.writeC(sex);
			packetWriter.writeD(base_class);
			packetWriter.writeD(class_id);
			packetWriter.writeC(level);
		}

		if(containsMask(UserInfoType.BASE_STATS))
		{
			packetWriter.writeH(UserInfoType.BASE_STATS.getBlockLength());
			packetWriter.writeH(_str);
			packetWriter.writeH(_dex);
			packetWriter.writeH(_con);
			packetWriter.writeH(_int);
			packetWriter.writeH(_wit);
			packetWriter.writeH(_men);
			packetWriter.writeH(0x00); // LUC
			packetWriter.writeH(0x00); // CHA
		}

		if(containsMask(UserInfoType.MAX_HPCPMP))
		{
			packetWriter.writeH(UserInfoType.MAX_HPCPMP.getBlockLength());
			packetWriter.writeD(maxHp);
			packetWriter.writeD(maxMp);
			packetWriter.writeD(maxCp);
		}

		if(containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
		{
			packetWriter.writeH(UserInfoType.CURRENT_HPMPCP_EXP_SP.getBlockLength());
			packetWriter.writeD(curHp);
			packetWriter.writeD(curMp);
			packetWriter.writeD(curCp);
			packetWriter.writeQ(_sp);
			packetWriter.writeQ(_exp);
			packetWriter.writeF(_expPercent);
		}

		if(containsMask(UserInfoType.ENCHANTLEVEL))
		{
			packetWriter.writeH(UserInfoType.ENCHANTLEVEL.getBlockLength());
			packetWriter.writeC(_weaponEnchant);
			packetWriter.writeC(_armorSetEnchant);
		}

		if(containsMask(UserInfoType.APPAREANCE))
		{
			packetWriter.writeH(UserInfoType.APPAREANCE.getBlockLength());
			packetWriter.writeD(hair_style);
			packetWriter.writeD(hair_color);
			packetWriter.writeD(face);
			packetWriter.writeC(!_hideHeadAccessories);  //переключения прически/головного убора
		}

		if(containsMask(UserInfoType.STATUS))
		{
			packetWriter.writeH(UserInfoType.STATUS.getBlockLength());
			packetWriter.writeC(mount_type);
			packetWriter.writeC(private_store);
			packetWriter.writeC(can_crystalize);
			packetWriter.writeC(0x00); // TODO Used Abilities Points
		}

		if(containsMask(UserInfoType.STATS))
		{
			packetWriter.writeH(UserInfoType.STATS.getBlockLength());
			packetWriter.writeH(_weaponFlag);
			packetWriter.writeD(_patk);
			packetWriter.writeD(_patkspd);
			packetWriter.writeD(_pdef);
			packetWriter.writeD(_pEvasion);
			packetWriter.writeD(_pAccuracy);
			packetWriter.writeD(_pCrit);
			packetWriter.writeD(_matk);
			packetWriter.writeD(_matkspd);
			packetWriter.writeD(_patkspd);
			packetWriter.writeD(_mEvasion);
			packetWriter.writeD(_mdef);
			packetWriter.writeD(_mAccuracy);
			packetWriter.writeD(_mCrit);
		}

		if(containsMask(UserInfoType.ELEMENTALS))
		{
			packetWriter.writeH(UserInfoType.ELEMENTALS.getBlockLength());
			packetWriter.writeH(defenceFire);
			packetWriter.writeH(defenceWater);
			packetWriter.writeH(defenceWind);
			packetWriter.writeH(defenceEarth);
			packetWriter.writeH(defenceHoly);
			packetWriter.writeH(defenceUnholy);
		}

		if(containsMask(UserInfoType.POSITION))
		{
			packetWriter.writeH(UserInfoType.POSITION.getBlockLength());
			packetWriter.writeD(_loc.x);
			packetWriter.writeD(_loc.y);
			packetWriter.writeD(_loc.z);
			packetWriter.writeD(vehicle_obj_id);
		}

		if(containsMask(UserInfoType.SPEED))
		{
			packetWriter.writeH(UserInfoType.SPEED.getBlockLength());
			packetWriter.writeH(_runSpd);
			packetWriter.writeH(_walkSpd);
			packetWriter.writeH(_swimRunSpd);
			packetWriter.writeH(_swimWalkSpd);
			packetWriter.writeH(mountRunSpd);
			packetWriter.writeH(mountWalkSpd);
			packetWriter.writeH(_flyRunSpd);
			packetWriter.writeH(_flyWalkSpd);
		}

		if(containsMask(UserInfoType.MULTIPLIER))
		{
			packetWriter.writeH(UserInfoType.MULTIPLIER.getBlockLength());
			packetWriter.writeF(move_speed);
			packetWriter.writeF(attack_speed);
		}

		if(containsMask(UserInfoType.COL_RADIUS_HEIGHT))
		{
			packetWriter.writeH(UserInfoType.COL_RADIUS_HEIGHT.getBlockLength());
			packetWriter.writeF(col_radius);
			packetWriter.writeF(col_height);
		}

		if(containsMask(UserInfoType.ATK_ELEMENTAL))
		{
			packetWriter.writeH(UserInfoType.ATK_ELEMENTAL.getBlockLength());
			packetWriter.writeC(attackAttributeType.getId());
			packetWriter.writeH(attackElementValue);
		}

		if(containsMask(UserInfoType.CLAN))
		{
			packetWriter.writeH(UserInfoType.CLAN.getBlockLength() + (_title.length() * 2));
			packetWriter.writeSizedString(_title);
			packetWriter.writeH(pledge_type);
			packetWriter.writeD(clan_id);
			packetWriter.writeD(large_clan_crest_id);
			packetWriter.writeD(clan_crest_id);
			packetWriter.writeD(ClanPrivs);
			packetWriter.writeC(_isClanLeader);
			packetWriter.writeD(ally_id);
			packetWriter.writeD(ally_crest_id);
			packetWriter.writeC(partyRoom ? 0x01 : 0x00);
		}

		if(containsMask(UserInfoType.SOCIAL))
		{
			packetWriter.writeH(UserInfoType.SOCIAL.getBlockLength());
			packetWriter.writeC(pvp_flag);
			packetWriter.writeD(karma);
			packetWriter.writeC(_noble); // Is Noble
			packetWriter.writeC(hero);
			packetWriter.writeC(pledge_class);
			packetWriter.writeD(pk_kills);
			packetWriter.writeD(pvp_kills);
			packetWriter.writeH(rec_left);
			packetWriter.writeH(rec_have);
			packetWriter.writeD(0x00); // 196, social action that character will spam every minute
		}

		if(containsMask(UserInfoType.VITA_FAME))
		{
			packetWriter.writeH(UserInfoType.VITA_FAME.getBlockLength());
			packetWriter.writeD(0x00);
			packetWriter.writeC(0x00); // Vita Bonus
			packetWriter.writeD(fame);
			packetWriter.writeD(0x00); // Рейдовые Очки
			packetWriter.writeD(0x00); // unk 196
		}

		if(containsMask(UserInfoType.SLOTS))
		{
			packetWriter.writeH(UserInfoType.SLOTS.getBlockLength());
			packetWriter.writeC(talismans);
			packetWriter.writeC(_jewelsLimit);
			packetWriter.writeC(_team.ordinal());
			packetWriter.writeC(0x00); // (1 = Red, 2 = White, 3 = White Pink) dotted ring on the floor
			packetWriter.writeC(0x00);
			packetWriter.writeC(0x00);
			packetWriter.writeC(0x00);
			packetWriter.writeC(_activeMainAgathionSlot);	// 140 PROTOCOL
			packetWriter.writeC(_subAgathionsLimit);	// 140 PROTOCOL
			packetWriter.writeC(0x00);	// SEVEN SIGNS
		}

		if(containsMask(UserInfoType.MOVEMENTS))
		{
			packetWriter.writeH(UserInfoType.MOVEMENTS.getBlockLength());
			packetWriter.writeC(_moveType);
			packetWriter.writeC(running);
		}

		if(containsMask(UserInfoType.COLOR))
		{
			packetWriter.writeH(UserInfoType.COLOR.getBlockLength());
			packetWriter.writeD(name_color);
			packetWriter.writeD(title_color);
		}

		if(containsMask(UserInfoType.INVENTORY_LIMIT))
		{
			packetWriter.writeH(UserInfoType.INVENTORY_LIMIT.getBlockLength());
			packetWriter.writeD(0x00); // mount ??
			packetWriter.writeH(InventoryLimit);
			packetWriter.writeC(0); //при 1 не показывает титул
			packetWriter.writeD(0x00); // unk 196
		}

		if(containsMask(UserInfoType.TRUE_HERO))
		{
			packetWriter.writeH(UserInfoType.TRUE_HERO.getBlockLength());
			packetWriter.writeC(0x01);
			packetWriter.writeD(0x00);
			packetWriter.writeC(0x00);
			packetWriter.writeC(0x00); // Chaos Festival Winner
		}

		if(containsMask(UserInfoType.ATT_SPIRITS))
		{
			packetWriter.writeH(UserInfoType.ATT_SPIRITS.getBlockLength());
			packetWriter.writeD(_elementalAttackPower);	// Сила атаки стихией
			packetWriter.writeD(_elementalFireDefence);	// Защита Огня
			packetWriter.writeD(_elementalWaterDefence);	// Защита Воды
			packetWriter.writeD(_elementalWindDefence);	// Защита Ветра
			packetWriter.writeD(_elementalEarthDefence);	// Защита Земли
			packetWriter.writeD(_activeElementId);	// Active AttributeType
		}

		if (containsMask(UserInfoType.RANK_TYPE)) // 196
		{
			packetWriter.writeH(UserInfoType.RANK_TYPE.getBlockLength()); // 196
			packetWriter.writeD(0x00); // 196
		}

		return true;
	}
}