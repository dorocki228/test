package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.AcquireSkillInfoPacket;
import l2s.gameserver.network.l2.s2c.ExAcquireSkillInfo;

/**
 * Reworked: VISTALL
 */
public class RequestAcquireSkillInfo implements IClientIncomingPacket
{
	private int _id;
	private int _level;
	private AcquireType _type;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_id = packet.readD();
		_level = packet.readD();
		_type = AcquireType.getById(packet.readD());
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null || player.isTransformed() || SkillHolder.getInstance().getSkill(_id, _level) == null || _type == null)
			return;

		if(_type != AcquireType.NORMAL && _type != AcquireType.MULTICLASS && _type != AcquireType.CUSTOM)
		{
			NpcInstance trainer = player.getLastNpc();
			if((trainer == null || !player.checkInteractionDistance(trainer)) && !player.isGM())
				return;
		}

		ClassId selectedMultiClassId = player.getSelectedMultiClassId();
		if(_type == AcquireType.MULTICLASS)
		{
			if(selectedMultiClassId == null)
				return;
		}
		else
			selectedMultiClassId = null;

		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, selectedMultiClassId, _id, _level, _type);
		if(skillLearn == null)
			return;

		if(_type == AcquireType.NORMAL)
			client.sendPacket(new ExAcquireSkillInfo(player, _type, skillLearn));
		else
			client.sendPacket(new AcquireSkillInfoPacket(_type, skillLearn));
	}
}