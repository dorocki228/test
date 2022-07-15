package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestExEnchantSkillInfo implements IClientIncomingPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_skillId = packet.readD();
		_skillLvl = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if ((_skillId <= 0) || (_skillLvl <= 0)) {
			return;
		}

		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		/*
		TODO
		if (!activeChar.isInCategory(CategoryType.SIXTH_CLASS_GROUP)) {
			return;
		}

		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLvl, _skillSubLvl);
		if ((skill == null) || (skill.getId() != _skillId)) {
			return;
		}
		final Set<Integer> route = EnchantSkillGroupsData.getInstance().getRouteForSkill(_skillId, _skillLvl);
		if (route.isEmpty()) {
			return;
		}

		final Skill playerSkill = activeChar.getKnownSkill(_skillId);
		if ((playerSkill.getLevel() != _skillLvl) || (playerSkill.getSubLevel() != _skillSubLvl)) {
			return;
		}

		client.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl, _skillSubLvl, playerSkill.getSubLevel()));*/
		// ExEnchantSkillInfoDetail - not really necessary I think
		// client.sendPacket(new ExEnchantSkillInfoDetail(SkillEnchantType.NORMAL, _skillId, _skillLvl, _skillSubLvl, activeChar));
	}
}