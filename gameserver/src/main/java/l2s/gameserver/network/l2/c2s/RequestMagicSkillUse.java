package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

public class RequestMagicSkillUse implements IClientIncomingPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0x39
	 * format:		cddc
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_magicId = packet.readD();
		_ctrlPressed = packet.readD() != 0;
		_shiftPressed = packet.readC() != 0;
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		activeChar.setActive();

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, _magicId, activeChar.getSkillLevel(_magicId));
		if(skillEntry == null) {
			if(activeChar.getCostumeList().useCostume(_magicId))
				return;
		}

		if(skillEntry != null)
		{
			Skill skill = skillEntry.getTemplate();
			if(!(skill.isActive() || skill.isToggle()))
			{
				activeChar.sendActionFailed();
				return;
			}

			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if(attachment != null && !attachment.canCast(activeChar, skill))
			{
				activeChar.sendActionFailed();
				return;
			}

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.isTransformed() && !activeChar.getAllSkills().contains(skillEntry))
			{
				activeChar.sendActionFailed();
				return;
			}

			if(skill.isToggle())
			{
				if(activeChar.getAbnormalList().contains(skill))
				{
					if(!skill.isNecessaryToggle())
					{
						if(activeChar.isSitting())
						{
							activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
							return;
						}
						activeChar.getAbnormalList().stop(skill.getId());
						activeChar.sendActionFailed();
					}
					activeChar.sendActionFailed();
					return;
				}
			}

			final GameObject playerTarget = activeChar.getTarget();

			// Java-man: костыль чтобы если моб умер между кастом последнего скилла и посылом ExAutoplayDoMacro
			boolean sendMessage;
			final Creature autoplayAttackTarget = activeChar.getAI().getAutoplayAttackTarget();
			if (autoplayAttackTarget != null && autoplayAttackTarget == playerTarget && playerTarget.isCreature() && ((Creature) playerTarget).isDead()) {
				sendMessage = false;
			} else {
				sendMessage = true;
			}
			Creature target = skill.getAimingTarget(activeChar, playerTarget, skill, _ctrlPressed, _shiftPressed, sendMessage);
			if (target == null) {
				activeChar.sendActionFailed();
				return;
			}

			activeChar.setGroundSkillLoc(null);
			activeChar.getAI().Cast(skillEntry, target, _ctrlPressed, _shiftPressed);
		}
		else
			activeChar.sendActionFailed();
	}
}