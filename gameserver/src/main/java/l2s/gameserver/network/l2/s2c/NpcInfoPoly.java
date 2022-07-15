package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.skills.AbnormalVisualEffect;
import l2s.gameserver.templates.npc.NpcTemplate;

public class NpcInfoPoly extends AbstractMaskPacket<NpcInfoType>
{
	// Flags
	private static final int IS_IN_COMBAT = 1 << 0;
	private static final int IS_ALIKE_DEAD = 1 << 1;
	private static final int IS_TARGETABLE = 1 << 2;
	private static final int IS_SHOW_NAME = 1 << 3;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x00
	};

	private final int _npcId;
	private final boolean _isAttackable;
	private final int _rHand, _lHand;
	private final String _name, _title;

	private int _initSize = 0;
	private int _blockSize = 0;

	private int _statusMask = 0;

	private int _showSpawnAnimation;
	private int _npcObjId;
	private Location _loc;
	private int _pAtkSpd, _mAtkSpd;
	private double _atkSpdMul, _runSpdMul;
	private int _pvpFlag, _karma;
	private boolean _alive, _running, _flying, _inWater;
	private TeamType _team;
	private int _currentHP, _currentMP;
	private int _maxHP, _maxMP;
	private int _enchantEffect;
	private int _transformId;
	private AbnormalVisualEffect[] abnormalVisualEffects;
	private int _clanId, _clanCrestId, _largeClanCrestId;
	private int _allyId, _allyCrestId;

	public NpcInfoPoly(Player player, Creature attacker)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(player.getPolyId());
		_npcObjId = player.getObjectId();
		_name = player.getVisibleName(attacker.getPlayer());
		_title = player.getVisibleTitle(attacker.getPlayer());
		_npcId = template.displayId != 0 ? template.displayId : template.getId();
		_isAttackable = player.isAutoAttackable(attacker);
		_loc = player.getLoc();
		_rHand = template.rhand;
		_lHand = template.lhand;
		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		_atkSpdMul = player.getAttackSpeedMultiplier();
		_runSpdMul = player.getMovementSpeedMultiplier();
		_pvpFlag = player.getPvpFlag();
		_karma = player.getKarma();
		_alive = !player.isAlikeDead();
		_running = player.isRunning();
		_flying = player.isFlying();
		_inWater = player.isInWater();
		_team = player.getTeam();
		_currentHP = (int) player.getCurrentHp();
		_currentMP = (int) player.getCurrentMp();
		_maxHP = player.getMaxHp();
		_maxMP = player.getMaxMp();
		_enchantEffect = player.getEnchantEffect();
		_transformId = 0;
		abnormalVisualEffects = player.getAbnormalEffectsArray();

		for(NpcInfoType component : NpcInfoType.VALUES)
			addComponentType(component);

		if(player.isInCombat())
			_statusMask |= IS_IN_COMBAT;

		if(player.isAlikeDead())
			_statusMask |= IS_ALIKE_DEAD;

		if(player.isTargetable(attacker))
			_statusMask |= IS_TARGETABLE;

		_statusMask |= IS_SHOW_NAME;
	}

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		switch(component)
		{
			case ATTACKABLE:
			case UNKNOWN1:
			{
				_initSize += component.getBlockLength();
				break;
			}
			case TITLE:
			{
				_initSize += component.getBlockLength() + (_title.length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += component.getBlockLength() + (_name.length() * 2);
				break;
			}
			default:
			{
				_blockSize += component.getBlockLength();
				break;
			}
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.NPC_INFO.writeId(packetWriter);
		packetWriter.writeD(_npcObjId);
		packetWriter.writeC(0x00); // // 0=teleported 1=default 2=summoned
		packetWriter.writeH(37); // mask_bits_37
		packetWriter.writeB(_masks);
		
		// Block 1
		packetWriter.writeC(_initSize);
		packetWriter.writeC(_isAttackable);
		packetWriter.writeD(0x00); // unknown
		packetWriter.writeS(_title);
		
		// Block 2
		packetWriter.writeH(_blockSize);
		packetWriter.writeD(_npcId + 1000000); // npctype id
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeD(_loc.h);
		packetWriter.writeD(0x00);
		packetWriter.writeD(_mAtkSpd);
		packetWriter.writeD(_pAtkSpd);
		packetWriter.writeE((float) _runSpdMul);
		packetWriter.writeE((float) _atkSpdMul);
		packetWriter.writeD(_rHand); // right hand weapon
		packetWriter.writeD(0);
		packetWriter.writeD(_lHand); // left hand weapon
		packetWriter.writeC(_alive);
		packetWriter.writeC(_running);
		packetWriter.writeC(_inWater ? 1 : _flying ? 2 : 0);
		packetWriter.writeC(_team.ordinal());
		packetWriter.writeD(_enchantEffect);
		packetWriter.writeD(_flying);
		packetWriter.writeD(0x00); // Player ObjectId with Decoy
		packetWriter.writeD(0x00); // Unknown
		packetWriter.writeD(0x00);
		packetWriter.writeD(_transformId);
		packetWriter.writeD(_currentHP);
		packetWriter.writeD(_currentMP);
		packetWriter.writeD(_maxHP);
		packetWriter.writeD(_maxMP);
		packetWriter.writeC(0x00);	// тип клона 1 == приманка, 2 = клон у ножа
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeS(_name);
		packetWriter.writeD(-1); // NPCStringId for name
		packetWriter.writeD(-1); // NPCStringId for title
		packetWriter.writeC(_pvpFlag); // PVP flag
		packetWriter.writeD(_karma); // Karma
		packetWriter.writeD(_clanId);
		packetWriter.writeD(_clanCrestId);
		packetWriter.writeD(_largeClanCrestId);
		packetWriter.writeD(_allyId);
		packetWriter.writeD(_allyCrestId);
		packetWriter.writeC(_statusMask);
		packetWriter.writeH(abnormalVisualEffects.length);
		for(AbnormalVisualEffect abnormal : abnormalVisualEffects)
			packetWriter.writeH(abnormal.getId());

		return true;
	}
}