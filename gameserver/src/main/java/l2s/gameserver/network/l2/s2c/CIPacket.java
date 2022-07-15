package l2s.gameserver.network.l2.s2c;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Config;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.skills.AbnormalVisualEffect;

import java.util.Set;

public class CIPacket implements IClientOutgoingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private boolean _canWrite = false;
	private int[][] _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private double speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face;
	private int clan_id, clan_crest_id, large_clan_crest_id, ally_id, ally_crest_id, class_id;
	private int _sit, _run, _combat, _dead, private_store, _enchant;
	private int _hero, _fishing, mount_type;
	private int plg_class, pledge_type, clan_rep_score, cw_level, mount_id;
	private int _nameColor, _title_color, _transform, _agathion;
	private Cubic[] cubics;
	private boolean _isPartyRoomLeader, _isFlying;
	private int _curHp, _maxHp, _curMp, _maxMp, _curCp;
	private TeamType _team;
	private Set<AbnormalVisualEffect> abnormalVisualEffects;
	private boolean _showHeadAccessories;
	private int _armorSetEnchant;
	private boolean _noble;

	public CIPacket(Player cha, Player receiver)
	{
		this((Creature) cha, receiver);
	}

	public CIPacket(DecoyInstance cha, Player receiver)
	{
		this((Creature) cha, receiver);
	}

	public CIPacket(Creature cha, Player receiver)
	{
		if(cha == null)
		{
			System.out.println("CIPacket: cha is null!");
			Thread.dumpStack();
			return;
		}

		if(receiver == null)
			return;

		if(cha.isInvisible(receiver))
			return;

		if(cha.isDeleted())
			return;

		_objId = cha.getObjectId();
		if(_objId == 0)
			return;

		if(receiver.getObjectId() == _objId)
		{
			_log.atSevere().log( "You cant send CIPacket about his character to active user!!!" );
			return;
		}

		Player player = cha.getPlayer();
		if(player == null)
			return;

		if(player.isInBoat())
			_loc = player.getInBoatPosition();

		if(_loc == null)
			_loc = cha.getLoc();

		if(_loc == null)
			return;

		_name = player.getVisibleName(receiver);
		_nameColor = player.getVisibleNameColor(receiver);

		if(player.isConnected() || player.isInOfflineMode() || player.isFakePlayer())
		{
			_title = player.getVisibleTitle(receiver);
			_title_color = player.getVisibleTitleColor(receiver);
		}
		else
		{
			_title = "NO CARRIER";
			_title_color = 255;
		}

		if(player.isPledgeVisible(receiver))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType().ordinal();
		}
		else
		{
			_enchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		_inv = new int[PcInventory.PAPERDOLL_MAX][4];
		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = player.getInventory().getPaperdollVisualId(PAPERDOLL_ID);
		}

		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		speed_move = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / speed_move);
		_walkSpd = (int) (player.getWalkSpeed() / speed_move);

		_flRunSpd = 0; // TODO
		_flWalkSpd = 0; // TODO

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

		_swimRunSpd = (int) player.getSwimRunSpeed();
		_swimWalkSpd = (int) player.getSwimWalkSpeed();
		_race = player.getRace().ordinal();
		_sex = player.getSex().ordinal();
		base_class = player.getBaseClassId();
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();

		speed_atack = player.getAttackSpeedMultiplier();
		col_radius = player.getCurrentCollisionRadius();
		col_height = player.getCurrentCollisionHeight();
		hair_style = player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? _sex : (player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle());
		hair_color = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		if(clan_id > 0 && player.getClan() != null)
			clan_rep_score = player.getClan().getReputationScore();
		else
			clan_rep_score = 0;
		_sit = player.isSitting() ? 0 : 1; // standing = 1 sitting = 0
		_run = player.isRunning() ? 1 : 0; // running = 1 walking = 0
		_combat = player.isInCombat() ? 1 : 0;
		_dead = player.isAlikeDead() ? 1 : 0;
		private_store = player.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : (player.isInBuffStore() ? 0 : player.getPrivateStoreType());
		cubics = player.getCubics().toArray(new Cubic[player.getCubics().size()]);
		abnormalVisualEffects = player.getAbnormalEffects();
		rec_have = player.isGM() ? 0 : player.getRecomHave();
		class_id = player.getClassId().getId();
		_team = player.getTeam();
		if (_team == TeamType.NONE) {
			if (player.isInSameParty(receiver)) {
				_team = TeamType.BLUE;
			}
		}
		_hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 2 : 0; // 0x01: Hero Aura
		if(_hero == 0)
			_hero = Hero.getInstance().isInactiveHero(_objId) ? 1 : 0;
		_noble = _hero > 0;
		_fishing = player.getFishing().isInProcess() ? 1 : 0;
		_fishLoc = player.getFishing().getHookLocation();
		plg_class = player.getPledgeRank().ordinal();
		pledge_type = player.getPledgeType();
		_transform = player.getVisualTransformId();
		_agathion = player.getAgathionNpcId();
		_isPartyRoomLeader = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_isFlying = player.isInFlyingTransform();
		_curHp = receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP) ? (int) player.getCurrentHp() : (int) player.getCurrentHpPercents();
		_maxHp = receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.MAX_HP) ? player.getMaxHp() : 100;
		_curMp = receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_MP) ? (int) player.getCurrentMp() : (int) player.getCurrentMpPercents();
		_maxMp = receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.MAX_MP) ? player.getMaxMp() : 100;
		_curCp = receiver.canReceiveStatusUpdate(player, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_CP) ? (int) player.getCurrentCp() : (int) player.getCurrentCpPercents();

		_showHeadAccessories = !player.hideHeadAccessories();

		_armorSetEnchant = player.getArmorSetEnchant();
		_canWrite = true;
	}

	@Override
	public boolean canBeWritten()
	{
		return _canWrite;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHAR_INFO.writeId(packetWriter);
		packetWriter.writeC(0x00); // [TODO]: Grand Crusade
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeD(0x00); // TODO _vehicleId
		packetWriter.writeD(_objId);
		packetWriter.writeS(_name);
		packetWriter.writeH(_race);
		packetWriter.writeC(_sex);
		packetWriter.writeD(base_class);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
			packetWriter.writeD(_inv[PAPERDOLL_ID][0]);

		packetWriter.writeD(_inv[Inventory.PAPERDOLL_RHAND][1]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_RHAND][2]);

		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LHAND][1]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LHAND][2]);

		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LRHAND][1]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LRHAND][2]);

		packetWriter.writeC(_armorSetEnchant);	// Armor Enchant Abnormal

		packetWriter.writeD(_inv[Inventory.PAPERDOLL_RHAND][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LHAND][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LRHAND][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_GLOVES][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_CHEST][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_LEGS][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_FEET][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_HAIR][3]);
		packetWriter.writeD(_inv[Inventory.PAPERDOLL_DHAIR][3]);

		packetWriter.writeC(pvp_flag);
		packetWriter.writeD(karma);

		packetWriter.writeD(_mAtkSpd);
		packetWriter.writeD(_pAtkSpd);

		packetWriter.writeH(_runSpd);
		packetWriter.writeH(_walkSpd);
		packetWriter.writeH(_swimRunSpd);
		packetWriter.writeH(_swimWalkSpd);
		packetWriter.writeH(_flRunSpd);
		packetWriter.writeH(_flWalkSpd);
		packetWriter.writeH(_flyRunSpd);
		packetWriter.writeH(_flyWalkSpd);

		packetWriter.writeF(speed_move); // _cha.getProperMultiplier()
		packetWriter.writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		packetWriter.writeF(col_radius);
		packetWriter.writeF(col_height);
		packetWriter.writeD(hair_style);
		packetWriter.writeD(hair_color);
		packetWriter.writeD(face);
		packetWriter.writeS(_title);
		packetWriter.writeD(clan_id);
		packetWriter.writeD(clan_crest_id);
		packetWriter.writeD(ally_id);
		packetWriter.writeD(ally_crest_id);

		packetWriter.writeC(_sit);
		packetWriter.writeC(_run);
		packetWriter.writeC(_combat);
		packetWriter.writeC(_dead);
		packetWriter.writeC(0x00); // Invis
		packetWriter.writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		packetWriter.writeC(private_store);
		packetWriter.writeH(cubics.length);
		for(Cubic cubic : cubics)
			packetWriter.writeH(cubic == null ? 0 : cubic.getId());
		packetWriter.writeC(_isPartyRoomLeader ? 0x01 : 0x00); // find party members
		packetWriter.writeC(_isFlying ? 0x02 : 0x00);
		packetWriter.writeH(rec_have);
		packetWriter.writeD(mount_id);
		packetWriter.writeD(class_id);
		packetWriter.writeD(0x00);
		packetWriter.writeC(_enchant);

		packetWriter.writeC(_team.ordinal()); // team circle around feet 1 = Blue, 2 = red

		packetWriter.writeD(large_clan_crest_id);
		packetWriter.writeC(_noble); // Is Noble
		packetWriter.writeC(_hero);

		packetWriter.writeC(_fishing);
		packetWriter.writeD(_fishLoc.x);
		packetWriter.writeD(_fishLoc.y);
		packetWriter.writeD(_fishLoc.z);

		packetWriter.writeD(_nameColor);
		packetWriter.writeD(_loc.h);
		packetWriter.writeC(plg_class);
		packetWriter.writeH(pledge_type);
		packetWriter.writeD(_title_color);
		packetWriter.writeC(0x00); // Cursed Weapon Level
		packetWriter.writeD(clan_rep_score);
		packetWriter.writeD(_transform);
		packetWriter.writeD(_agathion);

		packetWriter.writeC(0x01);	// UNK

		packetWriter.writeD(_curCp);
		packetWriter.writeD(_curHp);
		packetWriter.writeD(_maxHp);
		packetWriter.writeD(_curMp);
		packetWriter.writeD(_maxMp);

		packetWriter.writeC(0x00);	// UNK

		packetWriter.writeD(abnormalVisualEffects.size());
		for(AbnormalVisualEffect abnormal : abnormalVisualEffects)
			packetWriter.writeH(abnormal.getId());

		packetWriter.writeC(0); // Chaos Festival Winner
		packetWriter.writeC(_showHeadAccessories);
		packetWriter.writeC(0x00); //  Used Abilities Points
		
		return true;
	}

	public static final int[] PAPERDOLL_ORDER = {
			Inventory.PAPERDOLL_PENDANT,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR };
}