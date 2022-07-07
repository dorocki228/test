package l2s.Phantoms.ai.tasks.other;

import java.util.List;

import l2s.Phantoms.enums.PhantomType;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.base.Race;

public class BuffTask extends RunnableImpl
{
	private Player _phantom;

	public BuffTask(Player phantom)
	{
		_phantom = phantom;
	}

	@Override
	public void runImpl()
	{
		if(_phantom == null)
			return;
		if(_phantom.isDead() || _phantom.getOlympiadGame() != null /*|| _phantom.isInPvPEvent()*/)
		{
			_phantom.phantom_params.getPhantomAI().startBuffTask(60000); // отложим на минуту 
			return;
		}
		if(_phantom.getPvpFlag() > 0)
		{
			_phantom.phantom_params.getPhantomAI().startBuffTask(20000); // отложим на минуту 
			return;
		}
		if(_phantom.isInCombat())
		{
			_phantom.phantom_params.getPhantomAI().startBuffTask(20000);// отложим
			return;
		}

		if(_phantom.phantom_params.getClassAI().getBuffList().size() > 1)
		{
			for(Skill buff : _phantom.phantom_params.getClassAI().getBuffList())
				buff.getEffects(_phantom, _phantom, Config.PHANTOM_BUFF_TIME, 1.0);
		}
		else
		{
			// баф воинов
			if(!_phantom.getClassId().isMage() || _phantom.getRace() == Race.ORC)
			{
				for(int[] buff : Config.PHANTOM_TOP_BUFF)
				{
					Skill skill = SkillHolder.getInstance().getSkillEntry(buff[0], buff[1]).getTemplate();
					if(skill == null)
						continue;
					skill.getEffects(_phantom, _phantom, Config.PHANTOM_BUFF_TIME, 1.0);
				}
			}
			// баф магов
			if(_phantom.getClassId().isMage() && _phantom.getRace() != Race.ORC)
			{
				for(int[] buff : Config.PHANTOM_TOP_BUFF_MAGE)
				{
					Skill skill = SkillHolder.getInstance().getSkillEntry(buff[0], buff[1]).getTemplate();
					if(skill == null)
						continue;
					skill.getEffects(_phantom, _phantom, Config.PHANTOM_BUFF_TIME, 1.0);
				}
			}
		}
		_phantom.phantom_params.setNeedRebuff(false);
	}
}