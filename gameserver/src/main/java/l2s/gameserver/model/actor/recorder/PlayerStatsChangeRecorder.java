package l2s.gameserver.model.actor.recorder;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.s2c.ExChangeMPCost;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.ExUserInfoAbnormalVisualEffect;
import l2s.gameserver.network.l2.s2c.ExUserInfoInvenWeight;

import java.util.Objects;

public final class PlayerStatsChangeRecorder extends CharStatsChangeRecorder<Player>
{
	public static final int BROADCAST_KARMA = 32;
	public static final int SEND_STORAGE_INFO = 64;
	public static final int SEND_INVENTORY_LOAD = 128;
	public static final int BROADCAST_CHAR_INFO2 = 256;
	public static final int FORCE_BROADCAST_CHAR_INFO = 512;
	public static final int FORCE_SEND_CHAR_INFO = 1024;
	public static final int CHAGE_MP_COST_PHYSIC = 2048;
	public static final int CHAGE_MP_COST_MAGIC = 4096;
	public static final int CHAGE_MP_COST_MUSIC = 8192;
	private int _maxCp;
	private int _maxLoad;
	private int _curLoad;
	private final int[] _attackElement;
	private final int[] _defenceElement;
	private long _exp;
	private long _sp;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _fame;
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _trade;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _partyRoom;
	private double _physicMPCost;
	private double _magicMPCost;
	private double _musicMPCost;
	private String _title;
	private int _cubicsHash;

	public PlayerStatsChangeRecorder(Player activeChar)
	{
		super(activeChar);
		_attackElement = new int[6];
		_defenceElement = new int[6];
		_title = "";
	}

	@Override
	protected void refreshStats()
	{
		_maxCp = set(4, _maxCp, _activeChar.getMaxCp());
		super.refreshStats();
		_maxLoad = set(128, _maxLoad, _activeChar.getMaxLoad());
		_curLoad = set(128, _curLoad, _activeChar.getCurrentLoad());
		for(Element e : Element.VALUES)
		{
			_attackElement[e.getId()] = set(2, _attackElement[e.getId()], _activeChar.getAttack(e));
			_defenceElement[e.getId()] = set(2, _defenceElement[e.getId()], _activeChar.getDefence(e));
		}
		_exp = set(2, _exp, _activeChar.getExp());
		_sp = set(2, _sp, _activeChar.getSp());
		_pk = set(2, _pk, _activeChar.getPkKills());
		_pvp = set(2, _pvp, _activeChar.getPvpKills());
		_fame = set(2, _fame, _activeChar.getFame());
		_karma = set(32, _karma, _activeChar.getKarma());
		_inventory = set(64, _inventory, _activeChar.getInventoryLimit());
		_warehouse = set(64, _warehouse, _activeChar.getWarehouseLimit());
		_clan = set(64, _clan, Config.WAREHOUSE_SLOTS_CLAN);
		_trade = set(64, _trade, _activeChar.getTradeLimit());
		_recipeDwarven = set(64, _recipeDwarven, _activeChar.getDwarvenRecipeLimit());
		_recipeCommon = set(64, _recipeCommon, _activeChar.getCommonRecipeLimit());
		_cubicsHash = set(1, _cubicsHash, Objects.hash(_activeChar.getCubics()));
		_partyRoom = set(1, _partyRoom, _activeChar.getMatchingRoom() != null && _activeChar.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && _activeChar.getMatchingRoom().getLeader() == _activeChar ? _activeChar.getMatchingRoom().getId() : 0);
		_team = set(256, _team, _activeChar.getTeam());
		_title = set(1, _title, _activeChar.getTitle());
		_physicMPCost = set(2048, _physicMPCost, _activeChar.getMPCostDiff(Skill.SkillMagicType.PHYSIC));
		_magicMPCost = set(4096, _magicMPCost, _activeChar.getMPCostDiff(Skill.SkillMagicType.MAGIC));
		_musicMPCost = set(8192, _musicMPCost, _activeChar.getMPCostDiff(Skill.SkillMagicType.MUSIC));
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();
		if((_changes & 0x100) == 0x100)
		{
			_activeChar.broadcastCharInfo();
			for(Servitor servitor : _activeChar.getServitors())
				servitor.broadcastCharInfo();
		}
		if((_changes & 0x10) == 0x10)
		{
			_activeChar.sendUserInfo(true);
			_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
			_activeChar.broadcastUserInfo(true);
		}
		else
		{
			if((_changes & 0x200) == 0x200)
				_activeChar.broadcastUserInfo(true);
			else if((_changes & 0x1) == 0x1 || (_changes & 0x8) == 0x8)
			{
				if((_changes & 0x400) == 0x400)
					_activeChar.broadcastUserInfo(true);
				else
					_activeChar.broadcastCharInfo();
			}
			else if((_changes & 0x400) == 0x400)
				_activeChar.sendUserInfo(true);
			else if((_changes & 0x2) == 0x2)
				_activeChar.sendUserInfo();
			if((_changes & 0x8) == 0x8)
				_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
		}
		if((_changes & 0x80) == 0x80)
			_activeChar.sendPacket(new ExUserInfoInvenWeight(_activeChar));
		if((_changes & 0x20) == 0x20)
			_activeChar.sendStatusUpdate(true, false, 27);
		if((_changes & 0x40) == 0x40)
			_activeChar.sendPacket(new ExStorageMaxCountPacket(_activeChar));
		if((_changes & 0x800) == 0x800)
			_activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.PHYSIC, _physicMPCost));
		if((_changes & 0x1000) == 0x1000)
			_activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MAGIC, _magicMPCost));
		if((_changes & 0x2000) == 0x2000)
			_activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MUSIC, _musicMPCost));
	}
}
