package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Location;

public class PetInfoPacket extends L2GameServerPacket
{
	private static final int IS_UNK_FLAG_1 = 1;
	private static final int IS_UNK_FLAG_2 = 2;
	private static final int IS_RUNNING = 4;
	private static final int IS_IN_COMBAT = 8;
	private static final int IS_ALIKE_DEAD = 16;
	private static final int IS_RIDEABLE = 32;
	private final int _runSpd;
	private final int _walkSpd;
	private final int MAtkSpd;
	private final int PAtkSpd;
	private final int pvp_flag;
	private final int karma;
	private final int _type;
	private final int obj_id;
	private final int npc_id;
	private final int incombat;
	private final int dead;
	private final int _sp;
	private final int level;
	private final int curFed;
	private final int maxFed;
	private final int curHp;
	private final int maxHp;
	private final int curMp;
	private final int maxMp;
	private final int curLoad;
	private final int maxLoad;
	private final int PAtk;
	private final int PDef;
	private final int MAtk;
	private final int MDef;
	private final int sps;
	private final int ss;
	private final int type;
	private int _showSpawnAnimation;
	private final int _pAccuracy;
	private final int _pEvasion;
	private final int _pCrit;
	private final int _mAccuracy;
	private final int _mEvasion;
	private final int _mCrit;
	private final Location _loc;
	private final double col_redius;
	private final double col_height;
	private final long exp;
	private final long exp_this_lvl;
	private final long exp_next_lvl;
	private final String _name;
	private String title;
	private final TeamType _team;
	private final double _atkSpdMul;
	private final double _runSpdMul;
	private final int _usedSummonPoints;
	private final int _maxSummonPoints;
	private final int _transformId;
	private final AbnormalEffect[] _abnormalEffects;
	private final int _rhand;
	private final int _lhand;
	private int _flags;

	public PetInfoPacket(Servitor summon)
	{
		_type = summon.getServitorType();
		obj_id = summon.getObjectId();
		npc_id = summon.getNpcId();
		_loc = summon.getLoc();
		MAtkSpd = summon.getMAtkSpd();
		PAtkSpd = summon.getPAtkSpd();
		_runSpd = summon.getRunSpeed();
		_walkSpd = summon.getWalkSpeed();
		col_redius = summon.getColRadius();
		col_height = summon.getColHeight();
		incombat = summon.isInCombat() ? 1 : 0;
		dead = summon.isAlikeDead() ? 1 : 0;
		_name = summon.getName().equalsIgnoreCase(summon.getTemplate().name) ? "" : summon.getName();
		title = summon.getTitle();
		pvp_flag = summon.getPvpFlag();
		karma = summon.getKarma();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxFed();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		_sp = summon.getSp();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
		curLoad = summon.getCurrentLoad();
		maxLoad = summon.getMaxLoad();
		PAtk = summon.getPAtk(null);
		PDef = summon.getPDef(null);
		MAtk = summon.getMAtk(null, null);
		MDef = summon.getMDef(null, null);
		_pAccuracy = summon.getPAccuracy();
		_pEvasion = summon.getPEvasionRate(null);
		_pCrit = summon.getPCriticalHit(null);
		_mAccuracy = summon.getMAccuracy();
		_mEvasion = summon.getMEvasionRate(null);
		_mCrit = summon.getMCriticalHit(null, null);
		_abnormalEffects = summon.getAbnormalEffectsArray();
		_team = summon.getTeam();
		ss = summon.getSoulshotConsumeCount();
		sps = summon.getSpiritshotConsumeCount();
		_showSpawnAnimation = summon.getSpawnAnimation();
		type = summon.getFormId();
		_atkSpdMul = summon.getAttackSpeedMultiplier();
		_runSpdMul = summon.getMovementSpeedMultiplier();
		_transformId = summon.getVisualTransformId();
		boolean rideable = summon.isMountable();
		Player owner = summon.getPlayer();
		if(owner != null)
		{
			if(owner.isTransformed())
				rideable = false;
			_usedSummonPoints = owner.getUsedSummonPoints();
			_maxSummonPoints = owner.getMaxSummonPoints();
		}
		else
		{
			_usedSummonPoints = 0;
			_maxSummonPoints = 0;
		}
		_rhand = summon.getTemplate().rhand;
		_lhand = summon.getTemplate().lhand;
		_flags |= 0x2;
		if(summon.isRunning())
			_flags |= 0x4;
		if(summon.isInCombat())
			_flags |= 0x8;
		if(summon.isAlikeDead())
			_flags |= 0x10;
		if(rideable)
			_flags |= 0x20;
	}

	public PetInfoPacket update()
	{
		_showSpawnAnimation = 1;
		return this;
	}

	@Override
	protected final void writeImpl()
	{
        writeC(_type);
        writeD(obj_id);
        writeD(npc_id + 1000000);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_loc.h);
        writeD(MAtkSpd);
        writeD(PAtkSpd);
        writeH(_runSpd);
        writeH(_walkSpd);
        writeH(_runSpd);
        writeH(_walkSpd);
        writeH(_runSpd);
        writeH(_walkSpd);
        writeH(_runSpd);
        writeH(_walkSpd);
		writeF(_runSpdMul);
		writeF(_atkSpdMul);
		writeF(col_redius);
		writeF(col_height);
        writeD(_rhand);
        writeD(0);
        writeD(_lhand);
        writeC(_showSpawnAnimation);
        writeD(-1);
		writeS(_name);
        writeD(-1);
		writeS(title);
        writeC(pvp_flag);
        writeD(karma);
        writeD(curFed);
        writeD(maxFed);
        writeD(curHp);
        writeD(maxHp);
        writeD(curMp);
        writeD(maxMp);
		writeQ(_sp);
        writeC(level);
		writeQ(exp);
		writeQ(exp_this_lvl);
		writeQ(exp_next_lvl);
        writeD(curLoad);
        writeD(maxLoad);
        writeD(PAtk);
        writeD(PDef);
        writeD(_pAccuracy);
        writeD(_pEvasion);
        writeD(_pCrit);
        writeD(MAtk);
        writeD(MDef);
        writeD(_mAccuracy);
        writeD(_mEvasion);
        writeD(_mCrit);
        writeD(_runSpd);
        writeD(PAtkSpd);
        writeD(MAtkSpd);
        writeC(0);
        writeC(_team.ordinal());
        writeC(ss);
        writeC(sps);
        writeD(type);
        writeD(_transformId);
        writeC(_usedSummonPoints);
        writeC(_maxSummonPoints);
        writeH(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
            writeH(abnormal.getId());
        writeC(_flags);
	}
}
