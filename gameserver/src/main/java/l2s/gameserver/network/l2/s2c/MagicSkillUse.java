package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.skills.SkillCastingType;

/**
 * Format:   dddddddddh [h] h [ddd]
 * Пример пакета:
 * 48
 * 86 99 00 4F  86 99 00 4F
 * EF 08 00 00  01 00 00 00
 * 00 00 00 00  00 00 00 00
 * F9 B5 FF FF  7D E0 01 00  68 F3 FF FF
 * 00 00 00 00
 */
public class MagicSkillUse implements IClientOutgoingPacket
{
	public static final int NONE = -1;

	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _chaId, _x, _y, _z, _tx, _ty, _tz;
	private final SkillCastingType _castingType;
	private int _reuseGroup;
	private boolean _isServitorSkill;
	private int _actionId;
	private Location _groundLoc = null;
	private boolean _criticalBlow = false;

	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay, SkillCastingType castingType, int reuseGroup, boolean isServitorSkill, int actionId)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		_reuseGroup = reuseGroup;
		_isServitorSkill = isServitorSkill;
		_actionId = actionId;
		_castingType = castingType;
	}

	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay, SkillCastingType castingType)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, castingType, -1, false, 0);
	}

	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, SkillCastingType.NORMAL, -1, false, 0);
	}

	public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, hitTime, reuseDelay, SkillCastingType.NORMAL, -1, false, 0);
	}

	public MagicSkillUse setReuseSkillId(int id)
	{
		_reuseGroup = id;
		return this;
	}

	public MagicSkillUse setServitorSkillInfo(int actionId)
	{
		_isServitorSkill = true;
		_actionId = actionId;
		return this;
	}

	public MagicSkillUse setGroundLoc(Location loc)
	{
		_groundLoc = loc;
		return this;
	}

	public MagicSkillUse setCriticalBlow(boolean value)
	{
		_criticalBlow = value;
		return this;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MAGIC_SKILL_USE.writeId(packetWriter);
		packetWriter.writeD(_castingType.getClientBarId()); // Casting bar type: 0 - default, 1 - default up, 2 - blue, 3 - green, 4 - red.
		packetWriter.writeD(_chaId);
		packetWriter.writeD(_targetId);
		packetWriter.writeD(_skillId);
		packetWriter.writeD(_skillLevel);
		packetWriter.writeD(_hitTime);
		packetWriter.writeD(_reuseGroup);
		packetWriter.writeD(_reuseDelay);
		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);

		if(_criticalBlow) // TODO: Реализовать.
		{
			packetWriter.writeH(0x02);
			for(int i = 0; i < 2; i++)
			{
				packetWriter.writeH(0); //???
			}
		}
		else
			packetWriter.writeH(0x00);

		if(_groundLoc != null)
		{
			packetWriter.writeH(0x01);
			packetWriter.writeD(_groundLoc.x);
			packetWriter.writeD(_groundLoc.y);
			packetWriter.writeD(_groundLoc.z);
		}
		else
			packetWriter.writeH(0x00);

		packetWriter.writeD(_tx);
		packetWriter.writeD(_ty);
		packetWriter.writeD(_tz);
		packetWriter.writeD(_isServitorSkill ? 0x01 : 0x00); // is Pet Skill
		packetWriter.writeD(_actionId); // Social Action ID

		return true;
	}

	@Override
	public IClientOutgoingPacket packet(Player player)
	{
		if(player != null)
		{
			if(player.isNotShowBuffAnim())
				return _chaId == player.getObjectId() ? this : null;
		}

		return this;
	}
}