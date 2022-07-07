package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.PrivateBuffer;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CIPacket extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CIPacket.class);

	private Player player;

	private int[] _inv;
	private int[] augmentations;

	private int _mAtkSpd;
	private int _pAtkSpd;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private Location _loc;
	private String _name;
	private String _title;
	private int _objId;
	private int _race;
	private int _sex;
	private int base_class;
	private int pvp_flag;
	private int karma;
	private int rec_have;
	private double speed_move;
	private double speed_atack;
	private double col_radius;
	private double col_height;
	private int hair_style;
	private int hair_color;
	private int face;
	private int clan_id;
	private int clan_crest_id;
	private int large_clan_crest_id;
	private int ally_id;
	private int ally_crest_id;
	private int class_id;
	private int _sit;
	private int _run;
	private int _combat;
	private int _dead;
	private int private_store;
	private int _enchant;
	private int mount_type;
	private int plg_class;
	private int pledge_type;
	private int clan_rep_score;
	private int cw_level;
	private int mount_id;
	private int _nameColor;
	private int _title_color;
	private int _transform;
	private int _agathion;
	private EffectCubic[] cubics;
	private boolean _isPartyRoomLeader;
	private boolean _isFlying;
	private int _curHp;
	private int _maxHp;
	private int _curMp;
	private int _maxMp;
	private int _curCp;
	private TeamType _team;
	private AbnormalEffect[] _abnormalEffects;
	private final Player receiver;
	private boolean _showHeadAccessories;
	private int _armorSetEnchant;
	private int _hero;
	private int _fishing;
	private Location _fishLoc;

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
			Inventory.PAPERDOLL_DHAIR
	};
	public static final int[] PAPERDOLL_ORDER_AUGMENT = {
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_LRHAND
	};
	public static final int[] PAPERDOLL_ORDER_VISUAL_ID = {
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR
	};

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
		this.receiver = receiver;
		if(cha == null)
		{
			Thread.dumpStack();
			return;
		}
		if(this.receiver == null)
			return;
		if(cha.isInvisible(receiver))
			return;
		if(cha.isDeleted())
			return;
		_objId = cha.getObjectId();
		if(_objId == 0)
			return;
		if(this.receiver.getObjectId() == _objId)
		{
			_log.error("You cant send CIPacket about his character to active user!!!");
			return;
		}

		player = cha.getPlayer();
		if(player == null)
			return;
		if(player.isInBoat())
			_loc = player.getInBoatPosition();
		if(_loc == null)
			_loc = cha.getLoc();
		if(_loc == null)
			return;
		_name = player.getVisibleName(this.receiver);
		_nameColor = player.getVisibleNameColor(this.receiver);
		if(player.isConnected() || player.isInOfflineMode())
		{
			_title = player.getVisibleTitle(this.receiver);
			_title_color = player.isPrivateBuffer() ? PrivateBuffer.TITLE_COLOR : player.getVisibleTitleColor(this.receiver);
		}
		else
		{
			_title = "NO CARRIER";
			_title_color = 255;
		}

		_title += "[" + player.getReward(this.receiver) + "]";
		_title += player.isMercenary() ? " [Merc]" : "";

		if(_title.length() > 24)
			_title = _title.substring(_title.length() - 24);

		if(player.isPledgeVisible(this.receiver))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
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

		_inv = new int[PcInventory.PAPERDOLL_MAX];
		for(int PAPERDOLL_ID : PAPERDOLL_ORDER) {
			_inv[PAPERDOLL_ID] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
		}
		augmentations = new int[PAPERDOLL_ORDER_AUGMENT.length * 2];
		for (int i = 0; i < PAPERDOLL_ORDER_AUGMENT.length; i++) {
			int[] augmentationsTemp = player.getInventory().getPaperdollItemAugmentationId(PAPERDOLL_ORDER_AUGMENT[i]);
			augmentations[i * 2] = augmentationsTemp[0];
			augmentations[i * 2 + 1] = augmentationsTemp[1];
		}

		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		double multiplier = player.getMovementSpeedMultiplier();
		speed_move = Util.scaleValue(multiplier);
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
		_swimRunSpd = player.getSwimRunSpeed();
		_swimWalkSpd = player.getSwimWalkSpeed();
		_race = player.getRace().ordinal();
		_sex = player.getSex().ordinal();
		base_class = player.getBaseClassId();
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();
		speed_atack = player.getAttackSpeedMultiplier();
		col_radius = player.getColRadius();
		col_height = player.getColHeight();
		hair_style = player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle();
		hair_color = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		clan_rep_score = clan_id > 0 && player.getClan() != null ? player.getClan().getReputationScore() : 0;
		_sit = player.isSitting() ? 0 : 1;
		_run = player.isRunning() ? 1 : 0;
		_combat = player.isInCombat() ? 1 : 0;
		_dead = player.isAlikeDead() ? 1 : 0;
		private_store = player.isInObserverMode() ? 7 : player.getPrivateStoreType();
		cubics = player.getCubics().toArray(new EffectCubic[0]);
		_abnormalEffects = player.getAbnormalEffectsArray();
		rec_have = player.isGM() ? 0 : player.getRecomHave();
		class_id = player.getClassId().getId();
		_team = player.getTeam();
		_hero = player.isHero() || player.isCustomHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0;
		_fishing = player.getFishing().isInProcess() ? 1 : 0;
		_fishLoc = player.getFishing().getHookLocation();
		plg_class = player.getPledgeRank().ordinal();
		pledge_type = player.getPledgeType();
		_transform = player.getVisualTransformId();
		_agathion = player.getAgathionId();
		_isPartyRoomLeader = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_isFlying = player.isInFlyingTransform();
		_curHp = (int) player.getCurrentHp();
		_maxHp = player.getMaxHp();
		_curMp = (int) player.getCurrentMp();
		_maxMp = player.getMaxMp();
		_curCp = (int) player.getCurrentCp();
		_showHeadAccessories = !player.hideHeadAccessories();
		_armorSetEnchant = player.getArmorSetEnchant();
	}

	@Override
	protected final void writeImpl()
	{
		if(_loc == null)
			return;

		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(0);
		writeD(_objId);
		writeS(_name);
		writeH(_race);
		writeC(_sex);
		writeD(base_class);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID]);
		for (int augmentation : augmentations) {
			writeD(augmentation);
		}

		writeC(_armorSetEnchant);

		var showcostumesVar = receiver.getVarBoolean("showcostumes", true);
		if (showcostumesVar) {
			for (int slot : PAPERDOLL_ORDER_VISUAL_ID) {
				writeD(player.getInventory().getPaperdollItemVisualId(slot));
			}
		} else {
			for (int slot : PAPERDOLL_ORDER_VISUAL_ID) {
				writeD(0x00);
			}
		}

		writeC(pvp_flag);
		writeD(karma);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_swimRunSpd);
		writeH(_swimWalkSpd);
		writeH(_flRunSpd);
		writeH(_flWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);
		writeF(speed_move);
		writeF(speed_atack);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(0);
		writeC(mount_type);
		writeC(private_store);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(_isPartyRoomLeader ? 1 : 0);
		writeC(_isFlying ? 2 : 0);
		writeH(rec_have);
		writeD(mount_id);
		writeD(class_id);
		writeD(0);
		writeC(_enchant);
		writeC(_team.ordinal());
		writeD(large_clan_crest_id);
		writeC(0);
		writeC(_hero);

		writeC(_fishing);
		if(_fishLoc != null && _fishing == 1)
		{
			writeD(_fishLoc.x);
			writeD(_fishLoc.y);
			writeD(_fishLoc.z);
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
		}

		writeD(_nameColor);
		writeD(_loc.h);
		writeC(plg_class);
		writeH(pledge_type);
		writeD(_title_color);
		writeC(0);
		writeD(clan_rep_score);
		writeD(_transform);
		writeD(_agathion);
		writeC(1);
		writeD(_curCp);
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curMp);
		writeD(_maxMp);
		writeC(0);
		writeD(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getId());
		writeC(0);
		writeC(_showHeadAccessories);
		writeC(0);
	}
}
