package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Location;

public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private final Location _loc;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	public RequestExMagicSkillUseGround()
	{
		_loc = new Location();
	}

	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		SkillEntry skillEntry = activeChar.getKnownSkill(_skillId);
		if(skillEntry != null)
		{
			if(skillEntry.isDisabled())
				return;

			Skill skill = skillEntry.getTemplate();
			if(skill.getAddedSkills().length == 0)
				return;

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.isTransformed() && !activeChar.getAllSkills().contains(skillEntry))
				return;

			if(!activeChar.isInRange(_loc, skill.getCastRange()))
			{
				activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
				activeChar.sendActionFailed();
				return;
			}

			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			if(skillEntry.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true))
			{
				activeChar.setGroundSkillLoc(_loc);
				activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
			}
			else
				activeChar.sendActionFailed();
		}
		else
			activeChar.sendActionFailed();
	}
}
