package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.network.l2.OutgoingPackets;

public class AttackPacket implements IClientOutgoingPacket
{
	/*
	 * TODO: Aweking
	 * 0x00 >> обычный удар 
	 * 0x02 >> увернулся 
	 * 0x04 >> крит. удар 
	 * 0x06 >> заблокирован удар 
	 * 0x08 >> удар с соской 

	 * 0x0a >> обычный удар с соской 
	 * 0x0b >> промах 
	 * 0x0c >> критический удар с соской 
	 * 0x0d >> большая надпись, удара нет 
	 * 0x0e >> тоже, что и 0x0a, но есть большая надпись 
	 */
	public static final int HITFLAG_MISS = 0x01;
	public static final int HITFLAG_BLOCK = 0x02;
	public static final int HITFLAG_CRIT = 0x04;
	public static final int HITFLAG_USESS = 0x08;

	private class Hit
	{
		int _targetId, _damage, _flags;

		Hit(GameObject target, int damage, boolean miss, boolean crit, byte shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;

			if(miss)
			{
				_flags = HITFLAG_MISS;
				return;
			}

			if(_soulshot)
				_flags = HITFLAG_USESS;

			if(crit)
				_flags |= HITFLAG_CRIT;

			if(shld > 0 || target.isCreature() && ((Creature) target).isHpBlocked())
				_flags |= HITFLAG_BLOCK;
		}
	}

	public final int _attackerId;
	public final boolean _soulshot;
	private final int _grade;
	private final int _x, _y, _z, _tx, _ty, _tz;
	private Hit[] hits;
	private final int _addShotEffect;

	public AttackPacket(Creature attacker, Creature target, boolean ss, int grade)
	{
		_attackerId = attacker.getObjectId();
		_soulshot = ss;
		_grade = grade;
		_addShotEffect = attacker.getAdditionalVisualSSEffect();
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		hits = new Hit[0];
	}

	/**
	 * Add this hit (target, damage, miss, critical, shield) to the Server-Client packet Attack.<BR><BR>
	 */
	public void addHit(GameObject target, int damage, boolean miss, boolean crit, byte shld)
	{
		// Get the last position in the hits table
		int pos = hits.length;

		// Create a new Hit object
		Hit[] tmp = new Hit[pos + 1];

		// Add the new Hit object to hits table
		System.arraycopy(hits, 0, tmp, 0, hits.length);
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		hits = tmp;
	}

	/**
	 * Return True if the Server-Client packet Attack conatins at least 1 hit.<BR><BR>
	 */
	public boolean hasHits()
	{
		return hits.length > 0;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ATTACK.writeId(packetWriter);
		packetWriter.writeD(_attackerId);
		packetWriter.writeD(hits[0]._targetId);
		packetWriter.writeD(_soulshot ? _addShotEffect : 0x00);
		packetWriter.writeD(hits[0]._damage);
		packetWriter.writeD(hits[0]._flags);
		packetWriter.writeD(_soulshot ? _grade : 0x00);
		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);
		packetWriter.writeH(hits.length - 1);
		for(int i = 1; i < hits.length; i++)
		{
			packetWriter.writeD(hits[i]._targetId);
			packetWriter.writeD(hits[i]._damage);
			packetWriter.writeD(hits[i]._flags);
			packetWriter.writeD(_soulshot ? _grade : 0x00);
		}
		packetWriter.writeD(_tx);
		packetWriter.writeD(_ty);
		packetWriter.writeD(_tz);
		return true;
	}
}